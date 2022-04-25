package cn.chuanwise.command.exception;

import cn.chuanwise.command.Priority;
import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.Types;
import lombok.Data;

/**
 * 抽象异常处理器
 *
 * @author Chuanwise
 */
@Data
public abstract class AbstractExceptionHandler<T extends Throwable>
    implements ExceptionHandler {
    
    private final Class<T> exceptionClass;
    
    public AbstractExceptionHandler(Class<T> exceptionClass) {
        Preconditions.objectNonNull(exceptionClass, "exception class");
        
        this.exceptionClass = exceptionClass;
    }
    
    @SuppressWarnings("all")
    public AbstractExceptionHandler() {
        this.exceptionClass = (Class<T>) Types.getTypeParameterClass(getClass(), AbstractExceptionHandler.class);
    }
    
    @Override
    @SuppressWarnings("all")
    public final boolean handleException(Throwable cause) throws Exception {
        Preconditions.objectNonNull(cause, "cause");
    
        if (exceptionClass.isInstance(cause)) {
            return handleException0((T) cause);
        }
        return false;
    }
    
    protected abstract boolean handleException0(T t) throws Exception;
}
