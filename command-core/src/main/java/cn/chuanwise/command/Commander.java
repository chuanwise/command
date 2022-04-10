package cn.chuanwise.command;

import cn.chuanwise.command.annotation.Command;
import cn.chuanwise.command.annotation.Name;
import cn.chuanwise.command.command.MethodCommandExecutor;
import cn.chuanwise.command.completer.MethodCompleter;
import cn.chuanwise.command.configuration.CommanderConfiguration;
import cn.chuanwise.command.context.DispatchContext;
import cn.chuanwise.command.event.MethodRegisterEvent;
import cn.chuanwise.command.exception.MethodExceptionHandler;
import cn.chuanwise.command.format.FormatInfo;
import cn.chuanwise.command.parser.MethodParser;
import cn.chuanwise.command.wirer.MethodWirer;
import cn.chuanwise.command.tree.CommandTree;
import cn.chuanwise.command.tree.CommandTreeFork;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.*;

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
    protected final CommandTree commandTree = new CommandTree(this);
    
    /**
     * 相关配置
     */
    protected CommanderConfiguration commanderConfiguration;
    
    /**
     * 补全服务
     */
    protected final CompleteService completeService = new CompleteService(this);
    
    /**
     * 事件服务
     */
    protected final EventService eventService = new EventService(this);
    
    /**
     * 装配服务
     */
    protected final WireService wireService = new WireService(this);
    
    /**
     * 解析服务
     */
    protected final ParseService parseService = new ParseService(this);
    
    /**
     * 异常服务
     */
    protected final ExceptionService exceptionService = new ExceptionService(this);
    
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
     * 注册所有方法
     */
    public Commander register(Object source) {
        Preconditions.namedArgumentNonNull(source, "source object");
    
        final Method[] methods = source.getClass().getDeclaredMethods();
        for (Method method : methods) {
            // call event
            final MethodRegisterEvent methodRegisterEvent = new MethodRegisterEvent(source, method);
            eventService.broadcastEvent(methodRegisterEvent);
            if (methodRegisterEvent.isCancelled()) {
                continue;
            }
    
            final cn.chuanwise.command.annotation.Parser parser = method.getAnnotation(cn.chuanwise.command.annotation.Parser.class);
            if (Objects.nonNull(parser)) {
                parseService.registerParser(MethodParser.of(source, method), parser.priority());
            }
    
            final cn.chuanwise.command.annotation.Wirer wirer = method.getAnnotation(cn.chuanwise.command.annotation.Wirer.class);
            if (Objects.nonNull(wirer)) {
                wireService.registerWirer(MethodWirer.of(source, method), wirer.priority());
            }
    
            final cn.chuanwise.command.annotation.Completer completer = method.getAnnotation(cn.chuanwise.command.annotation.Completer.class);
            if (Objects.nonNull(completer)) {
                completeService.registerCompleter(new MethodCompleter(completer.priority(), source, method), completer.priority());
            }
    
            final cn.chuanwise.command.annotation.ExceptionHandler exceptionHandler = method.getAnnotation(cn.chuanwise.command.annotation.ExceptionHandler.class);
            if (Objects.nonNull(exceptionHandler)) {
                exceptionService.registerExceptionHandler(MethodExceptionHandler.of(source, method), exceptionHandler.priority());
            }
    
            final Command commandAnnotation = method.getAnnotation(Command.class);
            if (Objects.nonNull(commandAnnotation)) {
                final String[] formatStrings = commandAnnotation.value();
                final List<FormatInfo> formatInfo = new ArrayList<>(formatStrings.length);
                for (String format: formatStrings) {
                    formatInfo.add(commanderConfiguration.getFormatCompiler().compile(format));
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
                final cn.chuanwise.command.command.Command command = new cn.chuanwise.command.command.Command(name, this, formatInfo);
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
        return exceptionService.catching(() -> commandTree.execute(context), false);
    }
    
    /**
     * 串行调度，只产生一个结果
     *
     * @param context 调度上下文
     * @return 调度出的分支，或 null
     */
    public CommandTreeFork dispatchSerially(DispatchContext context) {
        return exceptionService.catching(() -> commandTree.dispatchSerially(context));
    }
    
    /**
     * 并行调度，产生所有可执行分支
     *
     * @param context 调度上下文
     * @return 调度出的分支
     */
    public List<CommandTreeFork> dispatch(DispatchContext context) {
        return exceptionService.catching(() -> commandTree.dispatch(context), Collections.emptyList());
    }
}