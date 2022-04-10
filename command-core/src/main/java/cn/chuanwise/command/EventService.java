package cn.chuanwise.command;

import cn.chuanwise.command.event.Cancellable;
import cn.chuanwise.command.event.EventHandler;
import cn.chuanwise.command.event.SimpleEventHandler;
import cn.chuanwise.command.object.AbstractCommanderObject;
import cn.chuanwise.common.api.ExceptionConsumer;
import cn.chuanwise.common.util.Maps;
import cn.chuanwise.common.util.Preconditions;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 事件服务
 *
 * @author Chuanwise
 */
public class EventService
    extends AbstractCommanderObject {
    
    /**
     * 监听器表
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected final Map<Priority, List<EventHandlerContainer>> eventHandlers = new HashMap<>();
    
    @Data
    private static class EventHandlerContainer {
        
        final EventHandler eventHandler;
        
        final boolean alwaysValid;
    }
    
    public EventService(Commander commander) {
        super(commander);
    }
    
    /**
     * 处理某个事件
     *
     * @param event 事件
     * @return 该事件是否被任何监听器处理
     */
    public boolean broadcastEvent(Object event) {
        Preconditions.namedArgumentNonNull(event, "event");
        
        return commander.getExceptionService().catching(() -> {
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
        final List<EventHandlerContainer> containers = this.eventHandlers.get(priority);
        boolean handled = false;
        
        if (cn.chuanwise.common.util.Collections.nonEmpty(containers)) {
            for (EventHandlerContainer container : containers) {
                if (event instanceof Cancellable) {
                    final Cancellable cancellable = (Cancellable) event;
                    if (cancellable.isCancelled() && !container.isAlwaysValid()) {
                        continue;
                    }
                }
                
                if (container.eventHandler.handleEvent(event)) {
                    handled = true;
                }
            }
        }
        
        return handled;
    }
    
    public void registerEventHandler(EventHandler eventHandler, boolean alwaysValid) {
        registerEventHandler(eventHandler, Priority.NORMAL, alwaysValid);
    }
    
    public void registerEventHandler(EventHandler eventHandler, Priority priority) {
        registerEventHandler(eventHandler, priority, false);
    }
    
    public void registerEventHandler(EventHandler eventHandler) {
        registerEventHandler(eventHandler, Priority.NORMAL, false);
    }
    
    /**
     * 注册一个默认优先级和指定有效性的事件监听器
     *
     * @param eventHandler 事件监听器
     * @param priority 监听器优先级
     * @param alwaysValid 监听器有效性
     */
    public void registerEventHandler(EventHandler eventHandler, Priority priority, boolean alwaysValid) {
        Preconditions.namedArgumentNonNull(eventHandler, "event handler");
        Preconditions.namedArgumentNonNull(priority, "priority");
        
        Maps.getOrPutGet(eventHandlers, priority, CopyOnWriteArrayList::new).add(new EventHandlerContainer(eventHandler, alwaysValid));
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
        
        final List<EventHandlerContainer> eventHandlers = Maps.getOrPutGet(this.eventHandlers, priority, CopyOnWriteArrayList::new);
        final SimpleEventHandler<T> eventHandler = new SimpleEventHandler<>(eventClass, action);
        eventHandlers.add(new EventHandlerContainer(eventHandler, alwaysValid));
        
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
    
        final boolean highest = unregisterEventHandler(Priority.HIGHEST, eventHandler);
        final boolean high = unregisterEventHandler(Priority.HIGH, eventHandler);
        final boolean normal = unregisterEventHandler(Priority.NORMAL, eventHandler);
        final boolean low = unregisterEventHandler(Priority.LOW, eventHandler);
        final boolean lowest = unregisterEventHandler(Priority.LOWEST, eventHandler);
        
        return highest || high || normal || low || lowest;
    }
    
    /**
     * 卸载某个事件监听器
     *
     * @param priority     监听器优先级
     * @param eventHandler 事件监听器
     * @return 是否卸载了该事件监听器
     */
    private boolean unregisterEventHandler(Priority priority, EventHandler eventHandler) {
        final List<EventHandlerContainer> containers = this.eventHandlers.get(priority);
        boolean removed;
        if (cn.chuanwise.common.util.Collections.nonEmpty(containers)) {
            removed = containers.removeIf(x -> Objects.equals(x.getEventHandler(), eventHandler));
            if (containers.isEmpty()) {
                this.eventHandlers.remove(priority);
            }
        } else {
            removed = false;
        }
        
        return removed;
    }
}
