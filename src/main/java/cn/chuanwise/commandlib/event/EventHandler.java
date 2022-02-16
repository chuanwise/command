package cn.chuanwise.commandlib.event;

import cn.chuanwise.function.ExceptionConsumer;
import cn.chuanwise.util.Preconditions;

public interface EventHandler {
    boolean handleEvent(Object event) throws Exception;

    @SuppressWarnings("all")
    static <T> EventHandler of(Class<T> eventClass, ExceptionConsumer<T> consumer) {
        Preconditions.argumentNonNull(eventClass, "event class");
        Preconditions.argumentNonNull(consumer, "consumer");

        return event -> {
            if (eventClass.isInstance(event)) {
                consumer.exceptAccept((T) event);
                return true;
            } else {
                return false;
            }
        };
    }
}
