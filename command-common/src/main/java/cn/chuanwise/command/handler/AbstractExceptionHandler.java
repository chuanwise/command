package cn.chuanwise.command.handler;

import cn.chuanwise.command.exception.ExceptionHandler;
import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.Types;
import lombok.Data;

/**
 * 抽象异常处理器
 *
 * @param <T> 异常类型
 * @author Chuanwise
 */
@Data
public abstract class AbstractExceptionHandler<T extends Throwable>
    implements ExceptionHandler {
    
    private Priority priority;
    
    private Class<T> exceptionClass;
    
    public AbstractExceptionHandler(Class<T> exceptionClass, Priority priority) {
        Preconditions.namedArgumentNonNull(exceptionClass, "exception class");
        Preconditions.namedArgumentNonNull(priority, "priority");
        
        this.priority = priority;
        this.exceptionClass = exceptionClass;
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
        if (exceptionClass.isInstance(cause)) {
            handleException0((T) cause);
            return true;
        }
        return false;
    }
    
    /**
     * 调用子类的异常处理逻辑处理异常
     *
     * @param t 异常
     * @throws Exception 处理异常时抛出的新异常
     */
    protected abstract void handleException0(T t) throws Exception;
}
