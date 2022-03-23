package cn.chuanwise.command;

import cn.chuanwise.command.annotation.Format;
import cn.chuanwise.command.annotation.Name;
import cn.chuanwise.command.command.Command;
import cn.chuanwise.command.command.MethodCommandExecutor;
import cn.chuanwise.command.completer.Completer;
import cn.chuanwise.command.completer.MethodCompleter;
import cn.chuanwise.command.completer.SimpleCompleter;
import cn.chuanwise.command.configuration.CommanderConfiguration;
import cn.chuanwise.command.context.CompleteContext;
import cn.chuanwise.command.context.DispatchContext;
import cn.chuanwise.command.context.ParseContext;
import cn.chuanwise.command.context.WireContext;
import cn.chuanwise.command.event.EventHandler;
import cn.chuanwise.command.event.MethodRegisterEvent;
import cn.chuanwise.command.event.SimpleEventHandler;
import cn.chuanwise.command.exception.ExceptionHandler;
import cn.chuanwise.command.exception.MethodExceptionHandler;
import cn.chuanwise.command.exception.SimpleExceptionHandler;
import cn.chuanwise.command.format.FormatInfo;
import cn.chuanwise.command.parser.MethodParser;
import cn.chuanwise.command.parser.Parser;
import cn.chuanwise.command.parser.SimpleParser;
import cn.chuanwise.command.wirer.MethodWirer;
import cn.chuanwise.command.wirer.SimpleWirer;
import cn.chuanwise.command.wirer.Wirer;
import cn.chuanwise.command.handler.*;
import cn.chuanwise.command.tree.CommandTree;
import cn.chuanwise.command.tree.CommandTreeFork;
import cn.chuanwise.common.api.ExceptionConsumer;
import cn.chuanwise.common.api.ExceptionFunction;
import cn.chuanwise.common.api.ExceptionRunnable;
import cn.chuanwise.common.api.ExceptionSupplier;
import cn.chuanwise.common.space.Container;
import cn.chuanwise.common.util.Maps;
import cn.chuanwise.common.util.Preconditions;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 指令工具
 *
 * @author Chuanwise
 */
@Data
public class Commander {
    
    /**
     * 指令树
     */
    @Getter(AccessLevel.NONE)
    protected final CommandTree commandTree = new CommandTree(this);
    
    /**
     * 相关配置
     */
    protected CommanderConfiguration commanderConfiguration;
    
    /**
     * 补全器表
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected final Map<Priority, List<Completer>> completers = new HashMap<>();
    
    /**
     * 监听器表
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected final Map<Priority, List<EventHandler>> eventHandlers = new HashMap<>();
    
    /**
     * 装配器
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected final Map<Priority, List<Wirer>> wirers = new HashMap<>();
    
    /**
     * 异常处理器
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected final Map<Priority, List<ExceptionHandler>> exceptionHandlers = new HashMap<>();
    
    /**
     * 解析器
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected final Map<Priority, List<Parser>> parsers = new HashMap<>();
    
    /**
     * 通过指定配置构造一个 {@link Commander}
     *
     * @param commanderConfiguration 配置
     */
    public Commander(CommanderConfiguration commanderConfiguration) {
        Preconditions.namedArgumentNonNull(commanderConfiguration, "configuration");
    
        this.commanderConfiguration = commanderConfiguration;
    }
    
    /**
     * 构造一个 {@link Commander}
     */
    public Commander() {
        this(new CommanderConfiguration());
    }
    
    /**
     * 处理某个事件
     *
     * @param event 事件
     * @return 该事件是否被任何监听器处理
     */
    public boolean broadcastEvent(Object event) {
        Preconditions.namedArgumentNonNull(event, "event");
    
        return catching(() -> {
            final boolean highest = broadcastEvent0(Priority.HIGHEST, event);
            final boolean high = broadcastEvent0(Priority.HIGH, event);
            final boolean normal = broadcastEvent0(Priority.NORMAL, event);
            final boolean low = broadcastEvent0(Priority.LOW, event);
            final boolean lowest = broadcastEvent0(Priority.LOWEST, event);
            
            return highest || high || normal || low || lowest;
        }, false);
    }
    
    /**
     * 调用特定级别的事件处理器处理某个事件
     *
     * @param priority 事件处理器优先级
     * @param event    事件
     * @return 事件是否被处理
     * @throws Exception 监听事件时抛出的异常
     */
    private boolean broadcastEvent0(Priority priority, Object event) throws Exception {
        final List<EventHandler> eventHandlers = this.eventHandlers.get(priority);
        boolean handled = false;
        
        if (cn.chuanwise.common.util.Collections.nonEmpty(eventHandlers)) {
            for (EventHandler eventHandler : eventHandlers) {
                if (eventHandler.handleEvent(event)) {
                    handled = true;
                }
            }
        }
        
        return handled;
    }
    
    /**
     * 注册一个指定的事件监听器
     *
     * @param eventHandler 事件监听器
     */
    public void registerEventHandler(EventHandler eventHandler) {
        Preconditions.namedArgumentNonNull(eventHandler, "event handler");
        
        Maps.getOrPutGet(eventHandlers, eventHandler.getPriority(), CopyOnWriteArrayList::new).add(eventHandler);
    }
    
    /**
     * 注册一个事件监听器
     *
     * @param eventClass  事件类
     * @param priority    优先级
     * @param alwaysValid 监听器是否总是有效
     * @param action      监听行为
     * @param <T>         事件类型
     * @return 被注册的监听器
     */
    public <T> EventHandler registerEventHandler(Class<T> eventClass, Priority priority, boolean alwaysValid, ExceptionConsumer<T> action) {
        Preconditions.namedArgumentNonNull(eventClass, "event class");
        Preconditions.namedArgumentNonNull(action, "action");
        Preconditions.namedArgumentNonNull(priority, "priority");
    
        final List<EventHandler> eventHandlers = Maps.getOrPutGet(this.eventHandlers, priority, CopyOnWriteArrayList::new);
        final SimpleEventHandler<T> eventHandler = new SimpleEventHandler<>(eventClass, priority, alwaysValid, action);
        eventHandlers.add(eventHandler);
        
        return eventHandler;
    }
    
    /**
     * 注册一个事件监听器
     *
     * @param eventClass  事件类
     * @param alwaysValid 监听器是否总是有效
     * @param action      监听行为
     * @param <T>         事件类型
     * @return 被注册的监听器
     */
    public <T> EventHandler registerEventHandler(Class<T> eventClass, boolean alwaysValid, ExceptionConsumer<T> action) {
        return registerEventHandler(eventClass, Priority.NORMAL, alwaysValid, action);
    }
    
    /**
     * 注册一个事件监听器
     *
     * @param eventClass 事件类
     * @param priority   优先级
     * @param action     监听行为
     * @param <T>        事件类型
     * @return 被注册的监听器
     */
    public <T> EventHandler registerEventHandler(Class<T> eventClass, Priority priority, ExceptionConsumer<T> action) {
        return registerEventHandler(eventClass, priority, false, action);
    }
    
    /**
     * 注册一个事件监听器
     *
     * @param eventClass 事件类
     * @param action     监听行为
     * @param <T>        事件类型
     * @return 被注册的监听器
     */
    public <T> EventHandler registerEventHandler(Class<T> eventClass, ExceptionConsumer<T> action) {
        return registerEventHandler(eventClass, Priority.NORMAL, false, action);
    }
    
    /**
     * 卸载事件监听器
     *
     * @param eventHandler 事件监听器
     * @return 是否卸载了该事件监听器
     */
    public boolean unregisterEventHandler(EventHandler eventHandler) {
        Preconditions.namedArgumentNonNull(eventHandler, "event handler");
        
        return unregisterEventHandler(eventHandler.getPriority(), eventHandler);
    }
    
    /**
     * 卸载某个事件监听器
     *
     * @param priority     监听器优先级
     * @param eventHandler 事件监听器
     * @return 是否卸载了该事件监听器
     */
    private boolean unregisterEventHandler(Priority priority, EventHandler eventHandler) {
        final List<EventHandler> eventHandlers = this.eventHandlers.get(priority);
        boolean removed;
        if (cn.chuanwise.common.util.Collections.nonEmpty(eventHandlers)) {
            removed = eventHandlers.remove(eventHandler);
            if (eventHandlers.isEmpty()) {
                this.eventHandlers.remove(priority);
            }
        } else {
            removed = false;
        }
        
        return removed;
    }
    
    /**
     * 处理某个异常
     *
     * @param cause 异常
     * @return 该异常是否被处理
     */
    public boolean handleException(Throwable cause) {
        Preconditions.namedArgumentNonNull(cause, "cause");
    
        final boolean highest = handleException0(Priority.HIGHEST, cause);
        final boolean high = handleException0(Priority.HIGH, cause);
        final boolean normal = handleException0(Priority.NORMAL, cause);
        final boolean low = handleException0(Priority.LOW, cause);
        final boolean lowest = handleException0(Priority.LOWEST, cause);
        
        return highest || high || normal || low || lowest;
    }
    
    /**
     * 调用特定优先级的异常处理器处理某个异常
     *
     * @param priority 优先级
     * @param cause    异常
     * @return 该异常是否被处理
     */
    private boolean handleException0(Priority priority, Throwable cause) {
        final List<ExceptionHandler> exceptionHandlers = this.exceptionHandlers.get(priority);
        
        if (cn.chuanwise.common.util.Collections.nonEmpty(exceptionHandlers)) {
            for (ExceptionHandler exceptionHandler : exceptionHandlers) {
                try {
                    if (exceptionHandler.handleException(cause)) {
                        return true;
                    }
                } catch (Throwable throwable) {
                    cause = throwable;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 注册一个异常处理器
     *
     * @param exceptionHandler 异常处理器
     */
    public void registerExceptionHandler(ExceptionHandler exceptionHandler) {
        Preconditions.namedArgumentNonNull(exceptionHandler, "exception handler");
    
        final List<ExceptionHandler> exceptionHandlers = Maps.getOrPutGet(this.exceptionHandlers, exceptionHandler.getPriority(), CopyOnWriteArrayList::new);
        exceptionHandlers.add(exceptionHandler);
    }
    
    /**
     * 注册一个异常处理器
     *
     * @param exceptionClass 异常类型
     * @param priority       优先级
     * @param action         处理行为
     * @param <T>            异常类型
     * @return 异常处理器
     */
    public <T extends Throwable> ExceptionHandler registerExceptionHandler(Class<T> exceptionClass, Priority priority, ExceptionConsumer<T> action) {
        Preconditions.namedArgumentNonNull(exceptionClass, "exception class");
        Preconditions.namedArgumentNonNull(priority, "priority");
        Preconditions.namedArgumentNonNull(action, "action");
    
        final List<ExceptionHandler> exceptionHandlers = Maps.getOrPutGet(this.exceptionHandlers, priority, CopyOnWriteArrayList::new);
        final SimpleExceptionHandler<T> exceptionHandler = new SimpleExceptionHandler<>(exceptionClass, priority, action);
        exceptionHandlers.add(exceptionHandler);
        
        return exceptionHandler;
    }
    
    /**
     * 注册一个异常处理器
     *
     * @param exceptionClass 异常类型
     * @param action         处理行为
     * @param <T>            异常类型
     * @return 异常处理器
     */
    public <T extends Throwable> ExceptionHandler registerExceptionHandler(Class<T> exceptionClass, ExceptionConsumer<T> action) {
        return registerExceptionHandler(exceptionClass, Priority.NORMAL, action);
    }
    
    /**
     * 注销一个异常处理器
     *
     * @param exceptionHandler 异常处理器
     * @return 是否注销该异常处理器
     */
    public boolean unregisterExceptionHandler(ExceptionHandler exceptionHandler) {
        Preconditions.namedArgumentNonNull(exceptionHandler, "exception handler");
    
        return unregisterExceptionHandler(exceptionHandler.getPriority(), exceptionHandler);
    }
    
    /**
     * 注销一个指定优先级的异常处理器
     *
     * @param priority         优先级
     * @param exceptionHandler 异常处理器
     * @return 是否注销该异常处理器
     */
    private boolean unregisterExceptionHandler(Priority priority, ExceptionHandler exceptionHandler) {
        final List<ExceptionHandler> exceptionHandlers = this.exceptionHandlers.get(priority);
    
        boolean removed;
        if (cn.chuanwise.common.util.Collections.nonEmpty(exceptionHandlers)) {
            removed = exceptionHandlers.remove(exceptionHandler);
            if (exceptionHandlers.isEmpty()) {
                this.exceptionHandlers.remove(priority);
            }
        } else {
            removed = false;
        }
        
        return removed;
    }
    
    /**
     * 指令树
     */
    public CommandTree commandTree() {
        return commandTree;
    }
    
    /**
     * 注册所有方法
     */
    public Commander register(Object source) {
        Preconditions.namedArgumentNonNull(source, "source object");
    
        final Method[] methods = source.getClass().getDeclaredMethods();
        for (Method method : methods) {
            // call event
            final MethodRegisterEvent methodRegisterEvent = new MethodRegisterEvent(source, method);
            broadcastEvent(methodRegisterEvent);
            if (methodRegisterEvent.isCancelled()) {
                continue;
            }
    
            final cn.chuanwise.command.annotation.Parser parser = method.getAnnotation(cn.chuanwise.command.annotation.Parser.class);
            if (Objects.nonNull(parser)) {
                registerParser(MethodParser.of(source, method));
            }
    
            final cn.chuanwise.command.annotation.Wirer wirer = method.getAnnotation(cn.chuanwise.command.annotation.Wirer.class);
            if (Objects.nonNull(wirer)) {
                registerWirer(MethodWirer.of(source, method));
            }
    
            final cn.chuanwise.command.annotation.Completer completer = method.getAnnotation(cn.chuanwise.command.annotation.Completer.class);
            if (Objects.nonNull(completer)) {
                registerCompleter(new MethodCompleter(completer.priority(), source, method));
            }
    
            final cn.chuanwise.command.annotation.ExceptionHandler exceptionHandler = method.getAnnotation(cn.chuanwise.command.annotation.ExceptionHandler.class);
            if (Objects.nonNull(exceptionHandler)) {
                registerExceptionHandler(MethodExceptionHandler.of(source, method));
            }
    
            final Format[] formats = method.getAnnotationsByType(Format.class);
            if (formats.length > 0) {
                final List<FormatInfo> formatInfo = new ArrayList<>(formats.length);
                for (Format format : formats) {
                    formatInfo.add(commanderConfiguration.getFormatCompiler().compile(format.value()));
                }
                
                // get command name
                final String name;
                final Name annotation = method.getAnnotation(Name.class);
                if (Objects.isNull(annotation)) {
                    name = method.getName();
                } else {
                    name = annotation.value();
                }
    
                // register command
                final Command command = new Command(name, this, formatInfo);
                command.setExecutor(new MethodCommandExecutor(command, source, method));
                commandTree.registerCommand(command);
            }
        }
    
        return this;
    }
    
    /**
     * 执行某个指令
     *
     * @param context 指令上下文
     * @return 指令是否被捕捉
     */
    public boolean execute(DispatchContext context) {
        return catching(() -> commandTree.execute(context), false);
    }
    
    /**
     * 串行调度，只产生一个结果
     *
     * @param context 调度上下文
     * @return 调度出的分支，或 null
     */
    public CommandTreeFork dispatchSerially(DispatchContext context) {
        return catching(() -> commandTree.dispatchSerially(context));
    }
    
    /**
     * 并行调度，产生所有可执行分支
     *
     * @param context 调度上下文
     * @return 调度出的分支
     */
    public List<CommandTreeFork> dispatch(DispatchContext context) {
        return catching(() -> commandTree.dispatch(context), Collections.emptyList());
    }
    
    /**
     * 补全某个指令
     *
     * @param context 补全上下文
     * @return 补全出的选项
     */
    public Set<String> complete(CompleteContext context) {
        Preconditions.namedArgumentNonNull(context, "complete context");
        
        final Set<String> set = new HashSet<>();
    
        set.addAll(complete(Priority.HIGHEST, context));
        set.addAll(complete(Priority.HIGH, context));
        set.addAll(complete(Priority.NORMAL, context));
        set.addAll(complete(Priority.LOW, context));
        set.addAll(complete(Priority.LOWEST, context));
        
        return Collections.unmodifiableSet(set);
    }
    
    /**
     * 调用某个优先级的补全工具补全某个指令
     *
     * @param priority 优先级
     * @param context  补全上下文
     * @return 补全出的选项
     */
    private Set<String> complete(Priority priority, CompleteContext context) {
        final List<Completer> completers = this.completers.get(priority);
    
        if (cn.chuanwise.common.util.Collections.isEmpty(completers)) {
            return Collections.emptySet();
        }
        
        final Set<String> set = new HashSet<>();
        for (Completer completer : completers) {
            set.addAll(catching(() -> completer.complete(context), Collections.emptySet()));
        }
        
        return set;
    }
    
    /**
     * 调用某个优先级的补全工具补全某个指令
     *
     * @param context     调度上下文
     * @param uncompleted 最后一个词是否输入完成
     * @return 补全出的选项
     */
    public Set<String> complete(DispatchContext context, boolean uncompleted) {
        return catching(() -> commandTree.complete(context, uncompleted), Collections.emptySet());
    }
    
    /**
     * 调用某个优先级的补全工具补全某个指令，并排序
     *
     * @param context     补全上下文
     * @param uncompleted 最后一个词是否输入完成
     * @return 补全出的选项
     */
    public List<String> sortedComplete(DispatchContext context, boolean uncompleted) {
        return catching(() -> commandTree.sortedComplete(context, uncompleted), Collections.emptyList());
    }
    
    /**
     * 注册一个指定的装配器
     *
     * @param wirer 装配器
     */
    public void registerWirer(Wirer wirer) {
        Preconditions.namedArgumentNonNull(wirer, "wirer");
        
        Maps.getOrPutGet(wirers, wirer.getPriority(), CopyOnWriteArrayList::new).add(wirer);
    }
    
    /**
     * 注册一个装配器
     *
     * @param wiredClass 事件类
     * @param priority   优先级
     * @param action     监听行为
     * @param <T>        事件类型
     * @return 被注册的装配器
     */
    public <T> Wirer registerWirer(Class<T> wiredClass, Priority priority, ExceptionFunction<WireContext, Container<T>> action) {
        Preconditions.namedArgumentNonNull(wiredClass, "event class");
        Preconditions.namedArgumentNonNull(action, "action");
        Preconditions.namedArgumentNonNull(priority, "priority");
        
        final List<Wirer> wirers = Maps.getOrPutGet(this.wirers, priority, CopyOnWriteArrayList::new);
        final SimpleWirer<T> wirer = new SimpleWirer<>(wiredClass, priority, action);
        wirers.add(wirer);
        
        return wirer;
    }
    
    /**
     * 注册一个装配器
     *
     * @param wiredClass 事件类
     * @param action     监听行为
     * @param <T>        事件类型
     * @return 被注册的装配器
     */
    public <T> Wirer registerWirer(Class<T> wiredClass, ExceptionFunction<WireContext, Container<T>> action) {
        return registerWirer(wiredClass, Priority.NORMAL, action);
    }
    
    /**
     * 卸载装配器
     *
     * @param wirer 装配器
     * @return 是否卸载了该装配器
     */
    public boolean unregisterWirer(Wirer wirer) {
        Preconditions.namedArgumentNonNull(wirer, "wirer");
        
        return unregisterWirer(wirer.getPriority(), wirer);
    }
    
    /**
     * 装配某个对象
     *
     * @param context 装配上下文
     * @return 装配结果
     */
    public Container<?> wire(WireContext context) {
        Preconditions.namedArgumentNonNull(context, "context");
    
        final Container<?> highest = wire0(Priority.HIGHEST, context);
        if (!highest.isEmpty()) {
            return highest;
        }
        final Container<?> high = wire0(Priority.HIGH, context);
        if (!high.isEmpty()) {
            return high;
        }
        final Container<?> normal = wire0(Priority.NORMAL, context);
        if (!normal.isEmpty()) {
            return normal;
        }
        final Container<?> low = wire0(Priority.LOW, context);
        if (!low.isEmpty()) {
            return low;
        }
        final Container<?> lowest = wire0(Priority.LOWEST, context);
        if (!lowest.isEmpty()) {
            return lowest;
        }
    
        return Container.empty();
    }
    
    private Container<?> wire0(Priority priority, WireContext context) {
        final List<Wirer> wirers = this.wirers.get(priority);
        if (cn.chuanwise.common.util.Collections.nonEmpty(wirers)) {
            for (Wirer wirer : wirers) {
                final Container<?> container = catching(() -> wirer.wire(context), Container.empty());
                if (!container.isEmpty()) {
                    return container;
                }
            }
        }
        return Container.empty();
    }
    
    /**
     * 卸载某个装配器
     *
     * @param priority 装配器优先级
     * @param wirer    装配器
     * @return 是否卸载了该装配器
     */
    private boolean unregisterWirer(Priority priority, Wirer wirer) {
        final List<Wirer> wirers = this.wirers.get(priority);
        boolean removed;
        if (cn.chuanwise.common.util.Collections.nonEmpty(wirers)) {
            removed = wirers.remove(wirer);
            if (wirers.isEmpty()) {
                this.wirers.remove(priority);
            }
        } else {
            removed = false;
        }
        
        return removed;
    }
    
    /**
     * 注册一个指定的解析器
     *
     * @param parser 解析器
     */
    public void registerParser(Parser parser) {
        Preconditions.namedArgumentNonNull(parser, "parser");
        
        Maps.getOrPutGet(parsers, parser.getPriority(), CopyOnWriteArrayList::new).add(parser);
    }
    
    /**
     * 注册一个解析器
     *
     * @param parsedClass 事件类
     * @param priority    优先级
     * @param action      监听行为
     * @param <T>         事件类型
     * @return 被注册的解析器
     */
    public <T> Parser registerParser(Class<T> parsedClass, Priority priority, ExceptionFunction<ParseContext, Container<T>> action) {
        Preconditions.namedArgumentNonNull(parsedClass, "parsed class");
        Preconditions.namedArgumentNonNull(action, "action");
        Preconditions.namedArgumentNonNull(priority, "priority");
        
        final List<Parser> parsers = Maps.getOrPutGet(this.parsers, priority, CopyOnWriteArrayList::new);
        final SimpleParser<T> parser = new SimpleParser<>(parsedClass, priority, action);
        parsers.add(parser);
        
        return parser;
    }
    
    /**
     * 注册一个解析器
     *
     * @param parsedClass 事件类
     * @param action      监听行为
     * @param <T>         事件类型
     * @return 被注册的解析器
     */
    public <T> Parser registerParser(Class<T> parsedClass, ExceptionFunction<ParseContext, Container<T>> action) {
        return registerParser(parsedClass, Priority.NORMAL, action);
    }
    
    /**
     * 卸载解析器
     *
     * @param parser 解析器
     * @return 是否卸载了该解析器
     */
    public boolean unregisterParser(Parser parser) {
        Preconditions.namedArgumentNonNull(parser, "parser");
        
        return unregisterParser(parser.getPriority(), parser);
    }
    
    /**
     * 卸载某个解析器
     *
     * @param priority 解析器优先级
     * @param parser   解析器
     * @return 是否卸载了该解析器
     */
    private boolean unregisterParser(Priority priority, Parser parser) {
        final List<Parser> parsers = this.parsers.get(priority);
        boolean removed;
        if (cn.chuanwise.common.util.Collections.nonEmpty(parsers)) {
            removed = parsers.remove(parser);
            if (parsers.isEmpty()) {
                this.parsers.remove(priority);
            }
        } else {
            removed = false;
        }
        
        return removed;
    }
    
    /**
     * 解析一个对象
     *
     * @param context 解析上下文
     * @return 解析结果
     */
    public Container<?> parse(ParseContext context) {
        Preconditions.namedArgumentNonNull(context, "context");
    
        final Container<?> highest = parse0(Priority.HIGHEST, context);
        if (!highest.isEmpty()) {
            return highest;
        }
        final Container<?> high = parse0(Priority.HIGH, context);
        if (!high.isEmpty()) {
            return high;
        }
        final Container<?> normal = parse0(Priority.NORMAL, context);
        if (!normal.isEmpty()) {
            return normal;
        }
        final Container<?> low = parse0(Priority.LOW, context);
        if (!low.isEmpty()) {
            return low;
        }
        final Container<?> lowest = parse0(Priority.LOWEST, context);
        if (!lowest.isEmpty()) {
            return lowest;
        }
        
        return Container.empty();
    }
    
    private Container<?> parse0(Priority priority, ParseContext context) {
        final List<Parser> parsers = this.parsers.get(priority);
        if (cn.chuanwise.common.util.Collections.nonEmpty(parsers)) {
            for (Parser parser : parsers) {
                final Container<?> container = catching(() -> parser.parse(context), Container.empty());
                if (!container.isEmpty()) {
                    return container;
                }
            }
        }
        return Container.empty();
    }
    
    /**
     * 注册一个指定的补全器
     *
     * @param completer 补全器
     */
    public void registerCompleter(Completer completer) {
        Preconditions.namedArgumentNonNull(completer, "completer");
        
        Maps.getOrPutGet(completers, completer.getPriority(), CopyOnWriteArrayList::new).add(completer);
    }
    
    /**
     * 注册一个补全器
     *
     * @param completedClass 事件类
     * @param priority 优先级
     * @param action 监听行为
     * @return 被注册的补全器
     */
    public Completer registerCompleter(Class<?> completedClass, Priority priority, ExceptionFunction<CompleteContext, Set<String>> action) {
        Preconditions.namedArgumentNonNull(completedClass, "completed class");
        Preconditions.namedArgumentNonNull(action, "action");
        Preconditions.namedArgumentNonNull(priority, "priority");
        
        final List<Completer> completers = Maps.getOrPutGet(this.completers, priority, CopyOnWriteArrayList::new);
        final SimpleCompleter completer = new SimpleCompleter(completedClass, priority, action);
        completers.add(completer);
        
        return completer;
    }
    
    /**
     * 注册一个补全器
     *
     * @param completedClass 事件类
     * @param action 监听行为
     * @return 被注册的补全器
     */
    public Completer registerCompleter(Class<?> completedClass, ExceptionFunction<CompleteContext, Set<String>> action) {
        return registerCompleter(completedClass, Priority.NORMAL, action);
    }
    
    /**
     * 卸载补全器
     *
     * @param completer 补全器
     * @return 是否卸载了该补全器
     */
    public boolean unregisterCompleter(Completer completer) {
        Preconditions.namedArgumentNonNull(completer, "completer");
        
        return unregisterCompleter(completer.getPriority(), completer);
    }
    
    /**
     * 卸载某个补全器
     *
     * @param priority 补全器优先级
     * @param completer 补全器
     * @return 是否卸载了该补全器
     */
    private boolean unregisterCompleter(Priority priority, Completer completer) {
        final List<Completer> completers = this.completers.get(priority);
        boolean removed;
        if (cn.chuanwise.common.util.Collections.nonEmpty(completers)) {
            removed = completers.remove(completer);
            if (completers.isEmpty()) {
                this.completers.remove(priority);
            }
        } else {
            removed = false;
        }
        
        return removed;
    }
    
    /**
     * 运行一段可能抛出异常的代码，并用已注册的异常处理器处理。
     *
     * @param action 可能抛出异常的代码
     */
    public void catching(ExceptionRunnable action) {
        Preconditions.namedArgumentNonNull(action, "action");
    
        try {
            action.exceptRun();
        } catch (Throwable cause) {
            handleException(cause);
        }
    }
    
    /**
     * 运行一段可能抛出异常的代码，并用已注册的异常处理器处理。
     *
     * @param action       可能抛出异常的代码
     * @param defaultValue 抛出异常后，代码的返回值
     * @return 代码的返回值
     */
    public <T> T catching(ExceptionSupplier<T> action, T defaultValue) {
        Preconditions.namedArgumentNonNull(action, "action");
    
        try {
            return action.exceptGet();
        } catch (Throwable cause) {
            handleException(cause);
            return defaultValue;
        }
    }
    
    /**
     * 运行一段可能抛出异常的代码，并用已注册的异常处理器处理。
     *
     * @param action 可能抛出异常的代码
     * @return 代码的返回值。抛出异常时返回 null
     */
    public <T> T catching(ExceptionSupplier<T> action) {
        return catching(action, null);
    }
}