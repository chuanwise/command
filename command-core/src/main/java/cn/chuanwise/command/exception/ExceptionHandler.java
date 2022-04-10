package cn.chuanwise.command.exception;

/**
 * 异常处理器
 *
 * @author Chuanwise
 */
@FunctionalInterface
public interface ExceptionHandler {
    
    /**
     * 处理异常
     *
     * @param cause 异常
     * @return 异常是否被处理
     * @throws Exception 处理异常途中抛出的异常
     */
    boolean handleException(Throwable cause) throws Exception;
}
