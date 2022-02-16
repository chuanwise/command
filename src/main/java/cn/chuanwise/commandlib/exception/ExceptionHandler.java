package cn.chuanwise.commandlib.exception;

import cn.chuanwise.function.ExceptionConsumer;
import cn.chuanwise.util.Preconditions;

public interface ExceptionHandler {
    boolean handleException(Throwable cause) throws Throwable;

    @SuppressWarnings("all")
    static <T extends Throwable> ExceptionHandler of(Class<T> exceptionClass, ExceptionConsumer<T> consumer) {
        Preconditions.argumentNonNull(exceptionClass, "exception class");
        Preconditions.argumentNonNull(consumer, "consumer");

        return throwable -> {
            if (exceptionClass.isInstance(throwable)) {
                consumer.exceptAccept((T) throwable);
                return true;
            } else {
                return false;
            }
        };
    }
}
