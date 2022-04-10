package cn.chuanwise.command;

import cn.chuanwise.command.Commander;
import cn.chuanwise.command.Priority;
import cn.chuanwise.command.completer.Completer;
import cn.chuanwise.command.completer.SimpleCompleter;
import cn.chuanwise.command.context.CompleteContext;
import cn.chuanwise.command.context.DispatchContext;
import cn.chuanwise.command.object.AbstractCommanderObject;
import cn.chuanwise.common.api.ExceptionFunction;
import cn.chuanwise.common.util.Maps;
import cn.chuanwise.common.util.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 补全服务
 *
 * @author Chuanwise
 */
public class CompleteService
    extends AbstractCommanderObject {
    
    /**
     * 补全器表
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected final Map<Priority, List<Completer>> completers = new HashMap<>();
    
    public CompleteService(Commander commander) {
        super(commander);
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
     * 注册一个补全器
     *
     * @param completer 补全器
     */
    public void registerCompleter(Completer completer) {
        registerCompleter(completer, Priority.NORMAL);
    }
    
    /**
     * 注册一个指定的补全器
     *
     * @param completer 补全器
     * @param priority 优先级
     */
    public void registerCompleter(Completer completer, Priority priority) {
        Preconditions.namedArgumentNonNull(completer, "completer");
        Preconditions.namedArgumentNonNull(priority, "priority");
        
        Maps.getOrPutGet(completers, priority, CopyOnWriteArrayList::new).add(completer);
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
        final SimpleCompleter completer = new SimpleCompleter(completedClass, action);
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
    
        final boolean highest = unregisterCompleter(Priority.HIGHEST, completer);
        final boolean high = unregisterCompleter(Priority.HIGH, completer);
        final boolean normal = unregisterCompleter(Priority.NORMAL, completer);
        final boolean low = unregisterCompleter(Priority.LOW, completer);
        final boolean lowest = unregisterCompleter(Priority.LOWEST, completer);
        
        return highest || high || normal || low || lowest;
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
            set.addAll(commander.getExceptionService().catching(() -> completer.complete(context), Collections.emptySet()));
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
        return commander.getExceptionService().catching(() -> commander.commandTree.complete(context, uncompleted), Collections.emptySet());
    }
    
    /**
     * 调用某个优先级的补全工具补全某个指令，并排序
     *
     * @param context     补全上下文
     * @param uncompleted 最后一个词是否输入完成
     * @return 补全出的选项
     */
    public List<String> sortedComplete(DispatchContext context, boolean uncompleted) {
        return commander.getExceptionService().catching(() -> commander.commandTree.sortedComplete(context, uncompleted), Collections.emptyList());
    }
}
