package cn.chuanwise.command.exception;

import cn.chuanwise.command.handler.Priority;

/**
 * 异常处理器
 *
 * @author Chuanwise
 */
public interface ExceptionHandler {
    
    /**
     * 获取异常处理器
     *
     * @return 异常处理器
     */
    Class<? extends Throwable> getExceptionClass();
    
    /**
     * 获取优先级
     *
     * @return 优先级
     */
    Priority getPriority();
    
    /**
     * 处理异常
     *
     * @param cause 异常
     * @return 异常是否被处理
     * @throws Exception 处理异常途中抛出的异常
     */
    boolean handleException(Throwable cause) throws Exception;
}
