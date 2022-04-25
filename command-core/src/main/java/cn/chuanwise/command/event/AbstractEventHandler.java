package cn.chuanwise.command.event;

import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.Types;
import lombok.Data;

/**
 * @see EventHandler
 * @author Chuanwise
 */
@Data
public abstract class AbstractEventHandler<T>
    implements EventHandler {
    
    /**
     * 事件类型
     */
    protected final Class<T> eventClass;

    public AbstractEventHandler(Class<T> eventClass) {
        Preconditions.objectNonNull(eventClass, "event class");

        this.eventClass = eventClass;
    }

    @SuppressWarnings("all")
    public AbstractEventHandler() {
        this.eventClass = (Class<T>) Types.getTypeParameterClass(getClass(), AbstractEventHandler.class);
    }

    @Override
    @SuppressWarnings("all")
    public final boolean handleEvent(Object event) throws Exception {
        if (eventClass.isInstance(event)) {
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
