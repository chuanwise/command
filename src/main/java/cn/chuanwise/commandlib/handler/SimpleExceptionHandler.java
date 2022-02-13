package cn.chuanwise.commandlib.handler;

import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.Types;
import lombok.Data;

@Data
public abstract class SimpleExceptionHandler<T extends Throwable>
        extends CommandLibHandlerAdapter {

    protected final Class<T> exceptionClass;

    public SimpleExceptionHandler(Class<T> exceptionClass) {
        Preconditions.argumentNonNull(exceptionClass, "exception class");

        this.exceptionClass = exceptionClass;
    }

    @SuppressWarnings("all")
    public SimpleExceptionHandler() {
        this.exceptionClass = (Class<T>) Types.getTypeParameterClass(getClass(), SimpleExceptionHandler.class);
    }

    @Override
    @SuppressWarnings("all")
    public final boolean handleException(Throwable cause) throws Exception {
        if (exceptionClass.isInstance(cause)) {
            handleException0((T) cause);
            return true;
        }
        return false;
    }

    protected abstract void handleException0(T t) throws Exception;
}
