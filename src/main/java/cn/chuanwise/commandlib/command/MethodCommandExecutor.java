package cn.chuanwise.commandlib.command;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.annotation.Format;
import cn.chuanwise.commandlib.annotation.Refer;
import cn.chuanwise.commandlib.completer.Completer;
import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.commandlib.context.ProvideContext;
import cn.chuanwise.commandlib.event.*;
import cn.chuanwise.commandlib.object.CommandLibObject;
import cn.chuanwise.commandlib.parser.Parser;
import cn.chuanwise.commandlib.provider.*;
import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.Reflects;
import lombok.Data;

import java.lang.reflect.*;
import java.util.*;

@Data
public class MethodCommandExecutor
        implements CommandExecutor, CommandLibObject {

    protected final Command command;

    protected final Object source;
    protected final Method method;

    protected final Provider[] providers;

    public MethodCommandExecutor(Command command, Object source, Method method) {
        Preconditions.argumentNonNull(command, "command");
        Preconditions.argumentNonNull(method, "method");
        Preconditions.argument(method.getAnnotationsByType(Format.class).length != 0, "method without @Format(...) annotation");

        this.command = command;

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
        final List<Provider> providerList = new ArrayList<>(parameters.length);

        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            final Class<?> parameterClass = parameter.getType();

            // 检查标签
            final Refer refer = parameter.getAnnotation(Refer.class);
            final Provider provider;
            if (Objects.isNull(refer)) {
                // 运行时 Provide
                provider = new RuntimeProvider<>(parameterClass);
            } else {
                // 根据注解解析
                final String referenceName = refer.value();
                final ParameterInfo parameterInfo;

                // 先当作普通参数，不行再查找
                ParameterInfo tempParameterInfo = command.parameterInfo.get(referenceName);
                if (Objects.isNull(tempParameterInfo)) {
                    for (ParameterInfo info : command.parameterInfo.values()) {
                        if (info instanceof OptionInfo) {
                            final OptionInfo optionInfo = (OptionInfo) info;
                            if (optionInfo.getAliases().contains(referenceName)) {
                                tempParameterInfo = optionInfo;
                                break;
                            }
                        }
                    }
                }
                parameterInfo = tempParameterInfo;

                check(Objects.nonNull(parameterInfo), i, "参数引用了 " + referenceName + "，但指令中并没有该变量。指令格式：" + command.getFormat());

                // 解析 Provider
                if (Objects.equals(parameterClass, String.class)) {
                    provider = new StringReferenceProvider(parameterInfo);
                } else {
                    // 检查是否有指定的 Parser
                    final Class<? extends Parser> parserClass = refer.parser();
                    if (Objects.equals(parserClass, Parser.class)) {
                        // 运行时解析
                        provider = new RuntimeParseProvider<>(parameterClass, parameterInfo);
                    } else {
                        final Parser parser;

                        // 尝试构造解析器
                        check(!Modifier.isAbstract(parameterClass.getModifiers()), i, "参数指定使用的解析器类型 " + parserClass.getName() + " 是抽象的，无法构造");

                        // 寻找 static INSTANCE
                        final Optional<Object> optionalStaticInstance = Reflects.getDeclaredStaticFieldValue(parserClass, "INSTANCE");
                        if (optionalStaticInstance.isPresent()) {
                            final Object instance = optionalStaticInstance.get();
                            check(parserClass.isInstance(instance.getClass()), i,
                                    "参数指定使用的解析器类型 " + parserClass.getName() + " 具备静态 INSTANCE 属性，但其值并非解析器类型");
                            parser = (Parser) instance;
                        } else {
                            // 尝试无参构造
                            Parser tempParser = null;
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
                        provider = new SpecialParserReferenceProvider<>(parser, parameterClass, parameterInfo);
                    }
                }

                // 解析 Completer
                final Class<? extends Completer> completerClass = refer.completer();
                if (!Objects.equals(completerClass, Completer.class)) {
                    final Completer completer;
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
                    parameterInfo.specialCompleters.add(completer);
                }

                // 添加参数类型
                parameterInfo.parameterClasses.add(parameterClass);
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
        final CommandExecutePreEvent commandHandlePreEvent = new CommandExecutePreEvent(context);
        commandLib.handleEvent(commandHandlePreEvent);
        if (commandHandlePreEvent.isCancelled()) {
            return false;
        }

        final Object[] arguments = new Object[providers.length];
        final Parameter[] parameters = method.getParameters();
        for (int i = 0; i < providers.length; i++) {
            final Provider provider = providers[i];
            final Container<?> container = provider.provide(
                    new ProvideContext(
                            context.getCommandSender(),
                            context.getReferenceInfo(),
                            context.getCommand(),
                            parameters[i]
                    )
            );

            if (Objects.isNull(container) || container.isEmpty()) {
                commandLib.handleEvent(new ParseFailedEvent(context, i, provider));
                return false;
            }

            final Object argument = container.get();
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
