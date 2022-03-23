package cn.chuanwise.command.command;

import cn.chuanwise.command.Commander;
import cn.chuanwise.command.annotation.Format;
import cn.chuanwise.command.annotation.Reference;
import cn.chuanwise.command.completer.Completer;
import cn.chuanwise.command.context.CommandContext;
import cn.chuanwise.command.context.WireContext;
import cn.chuanwise.command.event.*;
import cn.chuanwise.command.handler.Priority;
import cn.chuanwise.command.object.CommanderObject;
import cn.chuanwise.command.parser.Parser;
import cn.chuanwise.command.wirer.*;
import cn.chuanwise.common.space.Container;
import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.Reflections;
import cn.chuanwise.common.util.Strings;
import lombok.Data;

import java.lang.reflect.*;
import java.util.*;

/**
 * 方法指令执行器
 *
 * @author Chuanwise
 */
@Data
public class MethodCommandExecutor
        implements CommandExecutor, CommanderObject {
    
    /**
     * 指令
     */
    protected final Command command;
    
    /**
     * 方法所属对象
     */
    protected final Object source;
    
    /**
     * 方法
     */
    protected final Method method;
    
    /**
     * 参数装载器
     */
    protected final Wirer[] wires;

    public MethodCommandExecutor(Command command, Object source, Method method) {
        Preconditions.namedArgumentNonNull(command, "command");
        Preconditions.namedArgumentNonNull(method, "method");
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
        final List<Wirer> wirerList = new ArrayList<>(parameters.length);

        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            final Class<?> parameterClass = parameter.getType();

            // 检查标签
            final Reference reference = parameter.getAnnotation(Reference.class);
            final Wirer wirer;
            if (Objects.isNull(reference)) {
                // 运行时 Provide
                wirer = new RuntimeWirer<>(parameterClass, Priority.NORMAL);
            } else {
                // 根据注解解析
                final String referenceName = reference.value();
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

                final String description = reference.description();
                if (Strings.nonEmpty(description)) {
                    parameterInfo.getDescriptions().add(description);
                }

                // 解析 Parser
                // 检查是否有指定的 Parser
                final Class<? extends Parser> parserClass = reference.parser();
                if (Objects.equals(parserClass, Parser.class)) {
                    // 运行时解析
                    if (Objects.equals(parameterClass, String.class)) {
                        wirer = new StringReferenceWirer(parameterInfo);
                    } else {
                        wirer = new RuntimeParseWirer<>(parameterClass, parameterInfo);
                    }
                } else {
                    final Parser parser;

                    do {
                        // 尝试构造解析器
                        check(!Modifier.isAbstract(parameterClass.getModifiers()), i, "参数指定使用的解析器类型 " + parserClass.getName() + " 是抽象的，无法构造");
    
                        // 寻找 static INSTANCE
                        final Field instanceField = Reflections.getDeclaredStaticField(parserClass, "INSTANCE");
                        if (Objects.nonNull(instanceField)) {
                            final Object value = Reflections.getStaticFieldValue(instanceField);
                            if (Objects.nonNull(value)) {
                                check(parserClass.isInstance(value), i,
                                    "参数指定使用的解析器类型 " + parserClass.getName() + " 具备静态 INSTANCE 属性，但其值并非解析器类型");
                                parser = (Parser) value;
                                break;
                            }
                        }
    
                        // 尝试无参构造
                        Parser tempParser = null;
                        try {
                            try {
                                // 尝试使用默认无参构造函数构造
                                final Constructor<? extends Parser> defaultConstructor = parserClass.getDeclaredConstructor();
                                synchronized (defaultConstructor) {
                                    final boolean accessible = defaultConstructor.isAccessible();
                                    try {
                                        defaultConstructor.setAccessible(true);
                    
                                        tempParser = defaultConstructor.newInstance();
                                    } finally {
                                        defaultConstructor.setAccessible(accessible);
                                    }
                                }
                            } catch (NoSuchMethodException exception) {
                                // 如果不行，就当作内部类构造
                                if (Modifier.isStatic(parserClass.getModifiers())) {
                                    throw exception;
                                }
            
                                final Constructor<? extends Parser> internalConstructor = parserClass.getDeclaredConstructor(declaringClass);
                                synchronized (internalConstructor) {
                                    final boolean accessible = internalConstructor.isAccessible();
                                    try {
                                        internalConstructor.setAccessible(true);
                    
                                        tempParser = internalConstructor.newInstance(source);
                                    } finally {
                                        internalConstructor.setAccessible(accessible);
                                    }
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
                        
                    } while (false);
                    
                    wirer = new SpecialParserReferenceWirer<>(parser, parameterClass, parameterInfo);
                }

                // 解析 Completer
                final Class<? extends Completer> completerClass = reference.completer();
                if (!Objects.equals(completerClass, Completer.class)) {
                    final Completer completer;
                    check(!Modifier.isAbstract(completerClass.getModifiers()), i, "参数指定使用的补全器类型 " + completerClass.getName() + " 是抽象的，无法构造");

                    do {
                        // 寻找 static INSTANCE
                        final Field instanceField = Reflections.getDeclaredStaticField(completerClass, "INSTANCE");
    
                        if (Objects.nonNull(instanceField)) {
                            final Object instance = Reflections.getStaticFieldValue(instanceField);
                            if (Objects.nonNull(instance)) {
                                check(completerClass.isInstance(instance), i,
                                    "参数指定使用的补全器类型 " + completerClass.getName() + " 具备静态 INSTANCE 属性，但其值并非解析器类型");
                                completer = (Completer) instance;
                                break;
                            }
                        }
    
                        // 尝试无参构造
                        Completer tempCompleter = null;
                        try {
                            try {
                                // 尝试使用默认无参构造函数构造
                                final Constructor<? extends Completer> defaultConstructor = completerClass.getDeclaredConstructor();
                                synchronized (defaultConstructor) {
                                    final boolean accessible = defaultConstructor.isAccessible();
                                    try {
                                        defaultConstructor.setAccessible(true);
                    
                                        tempCompleter = defaultConstructor.newInstance();
                                    } finally {
                                        defaultConstructor.setAccessible(accessible);
                                    }
                                }
                            } catch (NoSuchMethodException exception) {
                                // 如果不行，就当作内部类构造
                                if (Modifier.isStatic(completerClass.getModifiers())) {
                                    throw exception;
                                }
            
                                final Constructor<? extends Completer> internalConstructor = completerClass.getDeclaredConstructor(declaringClass);
                                synchronized (internalConstructor) {
                                    final boolean accessible = internalConstructor.isAccessible();
                                    try {
                                        internalConstructor.setAccessible(true);
                    
                                        tempCompleter = internalConstructor.newInstance(source);
                                    } finally {
                                        internalConstructor.setAccessible(accessible);
                                    }
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
                    } while (false);
                    
                    parameterInfo.specialCompleters.add(completer);
                }

                // 添加参数类型
                parameterInfo.requiredClass.add(parameterClass);
            }

            wirerList.add(wirer);
        }
        this.wires = wirerList.toArray(new Wirer[0]);
    }

    private void check(boolean legal, int parameterIndex, String message) {
        Preconditions.state(legal, "解析指令处理方法的第 " + (parameterIndex + 1) + " 个参数时出现错误：" + message + "（位于 " + method + "）");
    }

    private void report(Throwable cause, int parameterIndex, String message) {
        throw new IllegalStateException("解析指令处理方法的第 " + (parameterIndex + 1) + " 个参数时出现错误：" + message + "（位于 " + method + "）", cause);
    }

    @Override
    public boolean execute(CommandContext context) throws Exception {
        final Commander commander = getCommander();
        final CommandExecutePreEvent commandHandlePreEvent = new CommandExecutePreEvent(context);
        commander.broadcastEvent(commandHandlePreEvent);
        if (commandHandlePreEvent.isCancelled()) {
            return false;
        }

        final Object[] arguments = new Object[wires.length];
        final Parameter[] parameters = method.getParameters();
        for (int i = 0; i < wires.length; i++) {
            final Wirer wirer = wires[i];
            final Parameter parameter = parameters[i];
            final Container<?> container = wirer.wire(
                    new WireContext(
                            context.getCommandSender(),
                            context.getReferenceInfo(),
                            context.getCommand(),
                            parameter
                    )
            );

            if (Objects.isNull(container) || container.isEmpty()) {
                commander.broadcastEvent(new WireFailedEvent(context, i, parameter, wirer));
                return false;
            }

            final Object argument = container.get();
            if (Objects.nonNull(argument)) {
                if (!parameter.getType().isInstance(argument)) {
                    commander.broadcastEvent(
                            new WireMismatchedEvent(context, i, parameter, wirer, argument)
                    );
                }
            }

            arguments[i] = argument;
        }

        try {
            Reflections.invokeMethod(source, method, arguments);
        } catch (InvocationTargetException e) {
            final CommandExecuteErrorEvent commandExecuteErrorEvent = new CommandExecuteErrorEvent(context, e.getCause());
            commander.broadcastEvent(commandExecuteErrorEvent);
            return false;
        }

        final CommandExecutePostEvent commandHandlePostEvent = new CommandExecutePostEvent(context);
        commander.broadcastEvent(commandHandlePostEvent);
        return true;
    }

    @Override
    public Commander getCommander() {
        return command.getCommander();
    }
}
