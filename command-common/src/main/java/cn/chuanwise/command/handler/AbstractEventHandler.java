package cn.chuanwise.command.handler;

import cn.chuanwise.command.event.Cancellable;
import cn.chuanwise.command.event.EventHandler;
import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.Types;
import lombok.Data;

/**
 * @see cn.chuanwise.command.event.EventHandler
 * @author Chuanwise
 */
@Data
public abstract class AbstractEventHandler<T>
    implements EventHandler {
    
    /**
     * 监听器优先级
     */
    protected final Priority priority;
    
    /**
     * 监听器是否总是生效
     */
    protected final boolean alwaysValid;
    
    /**
     * 事件类型
     */
    protected final Class<T> eventClass;

    public AbstractEventHandler(Class<T> eventClass, Priority priority, boolean alwaysValid) {
        Preconditions.namedArgumentNonNull(eventClass, "event class");
        Preconditions.namedArgumentNonNull(priority, "priority");

        this.priority = priority;
        this.alwaysValid = alwaysValid;
        this.eventClass = eventClass;
    }

    @SuppressWarnings("all")
    public AbstractEventHandler(Priority priority, boolean alwaysValid) {
        Preconditions.namedArgumentNonNull(priority, "priority");
        
        this.priority = priority;
        this.alwaysValid = alwaysValid;
        this.eventClass = (Class<T>) Types.getTypeParameterClass(getClass(), AbstractEventHandler.class);
    }

    @Override
    @SuppressWarnings("all")
    public final boolean handleEvent(Object event) throws Exception {
        if (eventClass.isInstance(event)) {
            if (event instanceof Cancellable) {
                final Cancellable cancellable = (Cancellable) event;
                if (cancellable.isCancelled() && !alwaysValid) {
                    return false;
                }
            }
    
            return handleEvent0((T) event);
        }
        return false;
    }
    
    /**
     * 真正执行事件的监听操作
     *
     * @param t 事件对象
     * @throws Exception 监听时抛出异常
     * @return 事件是否被监听
     */
    protected abstract boolean handleEvent0(T t) throws Exception;
}
