package cn.chuanwise.command;

import cn.chuanwise.command.context.WireContext;
import cn.chuanwise.command.object.AbstractCommanderObject;
import cn.chuanwise.command.wirer.SimpleWirer;
import cn.chuanwise.command.wirer.Wirer;
import cn.chuanwise.common.api.ExceptionFunction;
import cn.chuanwise.common.space.Container;
import cn.chuanwise.common.util.Maps;
import cn.chuanwise.common.util.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 装配服务
 *
 * @author Chuanwise
 */
public class WireService
    extends AbstractCommanderObject {
    
    /**
     * 装配器
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected final Map<Priority, List<Wirer>> wirers = new HashMap<>();
    
    public WireService(Commander commander) {
        super(commander);
    }
    
    public void registerWirer(Wirer wirer) {
        registerWirer(wirer, Priority.NORMAL);
    }
    
    /**
     * 注册一个指定的装配器
     *
     * @param wirer 装配器
     */
    public void registerWirer(Wirer wirer, Priority priority) {
        Preconditions.objectNonNull(wirer, "wirer");
        Preconditions.objectNonNull(priority, "priority");
        
        Maps.getOrPutGet(wirers, priority, CopyOnWriteArrayList::new).add(wirer);
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
        Preconditions.objectNonNull(wiredClass, "event class");
        Preconditions.objectNonNull(action, "action");
        Preconditions.objectNonNull(priority, "priority");
        
        final List<Wirer> wirers = Maps.getOrPutGet(this.wirers, priority, CopyOnWriteArrayList::new);
        final SimpleWirer<T> wirer = new SimpleWirer<>(wiredClass, action);
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
        Preconditions.objectNonNull(wirer, "wirer");
    
        final boolean highest = unregisterWirer(Priority.HIGHEST, wirer);
        final boolean high = unregisterWirer(Priority.HIGH, wirer);
        final boolean normal = unregisterWirer(Priority.NORMAL, wirer);
        final boolean low = unregisterWirer(Priority.LOW, wirer);
        final boolean lowest = unregisterWirer(Priority.LOWEST, wirer);
        
        return highest || high || normal || low || lowest;
    }
    
    /**
     * 装配某个对象
     *
     * @param context 装配上下文
     * @return 装配结果
     */
    public Container<?> wire(WireContext context) {
        Preconditions.objectNonNull(context, "context");
        
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
                final Container<?> container = commander.getExceptionService().catching(() -> wirer.wire(context), Container.empty());
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
}
