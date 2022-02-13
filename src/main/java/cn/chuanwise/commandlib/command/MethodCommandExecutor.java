package cn.chuanwise.commandlib.command;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.annotation.Reference;
import cn.chuanwise.commandlib.completer.Completer;
import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.commandlib.event.*;
import cn.chuanwise.commandlib.object.CommandLibObject;
import cn.chuanwise.commandlib.parser.Parser;
import cn.chuanwise.commandlib.provider.ParserReferenceProvider;
import cn.chuanwise.commandlib.provider.Provider;
import cn.chuanwise.commandlib.provider.StringReferenceProvider;
import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.Reflects;
import cn.chuanwise.util.Strings;
import lombok.Data;

import java.lang.reflect.*;
import java.util.*;

@Data
public class MethodCommandExecutor
        implements CommandExecutor, CommandLibObject {

    protected final Command command;

    protected final Object source;
    protected final Method method;

    protected final Provider<?>[] providers;

    public MethodCommandExecutor(Command command, Object source, Method method) {
        Preconditions.argumentNonNull(command, "command");
        Preconditions.argumentNonNull(method, "method");

        this.command = command;
        final CommandLib commandLib = command.getCommandLib();

        final Class<?> declaringClass = method.getDeclaringClass();
        if (Modifier.isStatic(method.getModifiers())) {
            this.source = declaringClass;
        } else {
            Preconditions.argument(declaringClass.isInstance(source), "method source should be instance of " + declaringClass.getName());
            this.source = source;
        }
        this.method = method;

        // fill providers
        final Parameter[] parameters = method.getParameters();
        final List<Provider<?>> providerList = new ArrayList<>(parameters.length);

        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            final Class<?> parameterClass = parameter.getType();

            // 检查标签
            final Reference reference = parameter.getAnnotation(Reference.class);
            final Provider<?> provider;
            if (Objects.isNull(reference)) {
                // 查找默认 parser
                final Optional<? extends Provider<?>> optionalProvider = commandLib.getProvider(parameterClass);
                check(optionalProvider.isPresent(), i, "参数不具备 @Reference 注解，也无针对其类型 " + parameterClass.getName() + " 的默认填充器");
                provider = optionalProvider.get();
            } else {
                // 根据注解解析
                final String referenceName = reference.value();
                final ParameterInfo parameterInfo = command.parameterInfo.get(referenceName);
                check(Objects.nonNull(parameterInfo), i, "参数引用了 " + referenceName + "，但指令中并没有该变量。指令格式：" + command.getUsage());

                // 解析 Provider
                if (Objects.equals(parameterClass, String.class)) {
                    provider = new StringReferenceProvider(parameterInfo);
                } else {
                    // 检查是否有指定的 Parser
                    final Class<? extends Parser> parserClass = reference.parser();
                    final Parser<?> parser;
                    if (Objects.equals(parserClass, Parser.class)) {
                        // 寻找解析器
                        final Optional<? extends Parser<?>> optionalParser = commandLib.getParser(parameterClass);
                        check(optionalParser.isPresent(), i, "无法找到对参数类型 " + parameterClass.getName() + " 的默认解析器");
                        parser = optionalParser.get();
                    } else {
                        // 尝试构造解析器
                        check(!Modifier.isAbstract(parameterClass.getModifiers()), i, "参数指定使用的解析器类型 " + parserClass.getName() + " 是抽象的，无法构造");

                        // 寻找 static INSTANCE
                        final Optional<Object> optionalStaticInstance = Reflects.getDeclaredStaticFieldValue(parserClass, "INSTANCE");
                        if (optionalStaticInstance.isPresent()) {
                            final Object instance = optionalStaticInstance.get();
                            check(parserClass.isInstance(instance.getClass()), i,
                                    "参数指定使用的解析器类型 " + parserClass.getName() + " 具备静态 INSTANCE 属性，但其值并非解析器类型");
                            parser = (Parser<?>) instance;
                        } else {
                            // 尝试无参构造
                            Parser<?> tempParser = null;
                            try {
                                final Constructor<? extends Parser> constructor = parserClass.getConstructor();
                                synchronized (constructor) {
                                    final boolean accessible = constructor.isAccessible();
                                    try {
                                        constructor.setAccessible(true);

                                        tempParser = constructor.newInstance();
                                    } finally {
                                        constructor.setAccessible(accessible);
                                    }
                                }
                            } catch (InstantiationException | InvocationTargetException e) {
                                report(e.getCause(), i, "通过默认的无参构造方法构造解析器类型 " + parserClass.getName() + " 的实例时出现异常");
                            } catch (IllegalAccessException e) {
                                report(e, i, "无法访问解析器类型 " + parserClass.getName() + " 的默认的无参构造方法");
                            } catch (NoSuchMethodException e) {
                                report(e, i, "无法找到解析器类型 " + parserClass.getName() + " 的默认的无参构造方法");
                            }
                            parser = tempParser;
                        }
                    }
                    check(parameterClass.isAssignableFrom(parser.getParsedClass()), i,
                            "指定解析器类型 " + parserClass.getName() + " 不匹配参数类型 " + parameterClass.getName());
                    provider = new ParserReferenceProvider<>(parser, parameterInfo);
                }

                // 解析 Completer
                final Class<? extends Completer> completerClass = reference.completer();
                final Completer completer;
                if (Objects.equals(completerClass, Completer.class)) {
                    completer = commandLib.getCompleter(parameterClass).orElse(null);
                } else {
                    check(!Modifier.isAbstract(completerClass.getModifiers()), i, "参数指定使用的补全器类型 " + completerClass.getName() + " 是抽象的，无法构造");

                    // 寻找 static INSTANCE
                    final Optional<Object> optionalStaticInstance = Reflects.getDeclaredStaticFieldValue(completerClass, "INSTANCE");
                    if (optionalStaticInstance.isPresent()) {
                        final Object instance = optionalStaticInstance.get();
                        check(completerClass.isInstance(instance.getClass()), i,
                                "参数指定使用的补全器类型 " + completerClass.getName() + " 具备静态 INSTANCE 属性，但其值并非解析器类型");
                        completer = (Completer) instance;
                    } else {
                        // 尝试无参构造
                        Completer tempCompleter = null;
                        try {
                            final Constructor<? extends Completer> constructor = completerClass.getConstructor();
                            synchronized (constructor) {
                                final boolean accessible = constructor.isAccessible();
                                try {
                                    constructor.setAccessible(true);

                                    tempCompleter = constructor.newInstance();
                                } finally {
                                    constructor.setAccessible(accessible);
                                }
                            }
                        } catch (InstantiationException | InvocationTargetException e) {
                            report(e.getCause(), i, "通过默认的无参构造方法构造补全器类型 " + completerClass.getName() + " 的实例时出现异常");
                        } catch (IllegalAccessException e) {
                            report(e, i, "无法访问补全器类型 " + completerClass.getName() + " 的默认的无参构造方法");
                        } catch (NoSuchMethodException e) {
                            report(e, i, "无法找到补全器类型 " + completerClass.getName() + " 的默认的无参构造方法");
                        }
                        completer = tempCompleter;
                    }
                }
                if (Objects.nonNull(completer)) {
                    parameterInfo.completers.add(completer);
                }
            }

            providerList.add(provider);
        }
        this.providers = providerList.toArray(new Provider[0]);
    }

    private void check(boolean legal, int parameterIndex, String message) {
        Preconditions.state(legal, "解析指令处理方法的第 " + (parameterIndex + 1) + " 个参数时出现错误：" + message + "（位于 " + method + "）");
    }

    private void report(Throwable cause, int parameterIndex, String message) {
        throw new IllegalStateException("解析指令处理方法的第 " + (parameterIndex + 1) + " 个参数时出现错误：" + message + "（位于 " + method + "）", cause);
    }

    @Override
    public boolean execute(CommandContext context) throws Exception {
        final CommandLib commandLib = getCommandLib();
        if (Strings.nonEmpty(command.getPermission())) {
            final PermissionVerifyEvent permissionVerifyEvent = new PermissionVerifyEvent(context, command.getPermission());
            commandLib.handleEvent(permissionVerifyEvent);

            if (!permissionVerifyEvent.isAuthorized()) {
                final PermissionDeniedEvent permissionDeniedEvent = new PermissionDeniedEvent(context, command.getPermission());
                commandLib.handleEvent(permissionDeniedEvent);
                return false;
            }
        }

        final CommandExecutePreEvent commandHandlePreEvent = new CommandExecutePreEvent(context);
        commandLib.handleEvent(commandHandlePreEvent);
        if (commandHandlePreEvent.isCancelled()) {
            return false;
        }

        final Object[] arguments = new Object[providers.length];
        final Parameter[] parameters = method.getParameters();
        for (int i = 0; i < providers.length; i++) {
            final Provider<?> provider = providers[i];
            final Container<?> container = provider.provide(context);

            if (Objects.isNull(container) || container.isEmpty()) {
                commandLib.handleEvent(new ParseFailedEvent(context, i, provider));
                return false;
            }

            // 检查类型
            final Class<?> parameterClass = parameters[i].getType();
            final Object argument = container.get();
            if (Objects.nonNull(argument) && !parameterClass.isInstance(argument)) {
                commandLib.handleEvent(new ParseErrorEvent(context, i, parameterClass, argument));
                return false;
            }

            arguments[i] = argument;
        }

        try {
            Reflects.invoke(source, method, arguments);
        } catch (InvocationTargetException e) {
            final CommandExecuteErrorEvent commandExecuteErrorEvent = new CommandExecuteErrorEvent(context, e.getCause());
            commandLib.handleEvent(commandExecuteErrorEvent);
            return false;
        }

        final CommandExecutePostEvent commandHandlePostEvent = new CommandExecutePostEvent(context);
        commandLib.handleEvent(commandHandlePostEvent);
        return true;
    }

    @Override
    public CommandLib getCommandLib() {
        return command.getCommandLib();
    }
}
