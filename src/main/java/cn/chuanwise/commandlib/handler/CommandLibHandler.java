package cn.chuanwise.commandlib.handler;

import cn.chuanwise.function.ExceptionConsumer;
import cn.chuanwise.util.Preconditions;

public interface CommandLibHandler {

    boolean handleException(Throwable cause) throws Exception;

    boolean handleEvent(Object event) throws Exception;

    static <T> CommandLibHandler ofEvent(Class<T> eventClass, ExceptionConsumer<T> consumer) {
        Preconditions.argumentNonNull(eventClass, "event class");
        Preconditions.argumentNonNull(consumer, "consumer");

        return new SimpleEventHandler<T>(eventClass) {
            @Override
            public void handleEvent0(T t) throws Exception {
                consumer.exceptAccept(t);
            }
        };
    }

    static <T extends Throwable> CommandLibHandler ofException(Class<T> exceptionClass, ExceptionConsumer<T> consumer) {
        Preconditions.argumentNonNull(exceptionClass, "exception class");
        Preconditions.argumentNonNull(consumer, "consumer");

        return new SimpleExceptionHandler<T>(exceptionClass) {
            @Override
            protected void handleException0(T t) throws Exception {
                consumer.exceptAccept(t);
            }
        };
    }
}
