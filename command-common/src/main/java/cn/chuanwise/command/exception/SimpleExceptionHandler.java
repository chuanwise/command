package cn.chuanwise.command.exception;

import cn.chuanwise.command.handler.Priority;
import cn.chuanwise.common.api.ExceptionConsumer;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

/**
 * 简单异常捕捉器
 *
 * @param <T> 异常类型
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class SimpleExceptionHandler<T extends Throwable>
    extends AbstractExceptionHandler<T> {
    
    private ExceptionConsumer<T> action;
    
    public SimpleExceptionHandler(Class<T> exceptionClass, Priority priority, ExceptionConsumer<T> action) {
        super(exceptionClass, priority);
    
        Preconditions.namedArgumentNonNull(action, "action");
        
        this.action = action;
    }
    
    public SimpleExceptionHandler(Priority priority, ExceptionConsumer<T> action) {
        super(priority);
        
        Preconditions.namedArgumentNonNull(action, "action");
        
        this.action = action;
    }
    
    @Override
    protected boolean handleException0(T t) throws Exception {
        action.exceptAccept(t);
        return true;
    }
}
