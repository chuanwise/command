package cn.chuanwise.commandlib.handler;

import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.Types;
import lombok.Data;

@Data
public abstract class SimpleEventHandler<T>
        extends HandlerAdapter {

    protected final Class<T> eventClass;

    public SimpleEventHandler(Class<T> eventClass) {
        Preconditions.argumentNonNull(eventClass, "event class");

        this.eventClass = eventClass;
    }

    @SuppressWarnings("all")
    public SimpleEventHandler() {
        this.eventClass = (Class<T>) Types.getTypeParameterClass(getClass(), SimpleEventHandler.class);
    }

    @Override
    @SuppressWarnings("all")
    public final boolean handleEvent(Object event) throws Exception {
        if (eventClass.isInstance(event)) {
            handleEvent0((T) event);
            return true;
        }
        return false;
    }

    public abstract void handleEvent0(T t) throws Exception;
}
