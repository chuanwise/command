package cn.chuanwise.command.exception;

import cn.chuanwise.command.handler.Priority;
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
    private final Priority priority;
    
    public AbstractExceptionHandler(Class<T> exceptionClass, Priority priority) {
        Preconditions.namedArgumentNonNull(exceptionClass, "exception class");
        Preconditions.namedArgumentNonNull(priority, "priority");
        
        this.exceptionClass = exceptionClass;
        this.priority = priority;
    }
    
    @SuppressWarnings("all")
    public AbstractExceptionHandler(Priority priority) {
        Preconditions.namedArgumentNonNull(priority, "priority");
        
        this.priority = priority;
        this.exceptionClass = (Class<T>) Types.getTypeParameterClass(getClass(), AbstractExceptionHandler.class);
    }
    
    @Override
    @SuppressWarnings("all")
    public final boolean handleException(Throwable cause) throws Exception {
        Preconditions.namedArgumentNonNull(cause, "cause");
    
        if (exceptionClass.isInstance(cause)) {
            return handleException0((T) cause);
        }
        return false;
    }
    
    protected abstract boolean handleException0(T t) throws Exception;
}
