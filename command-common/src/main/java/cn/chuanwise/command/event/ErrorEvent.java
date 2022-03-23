package cn.chuanwise.command.event;

/**
 * 带有异常的事件
 *
 * @author Chuanwise
 */
public interface ErrorEvent {
    
    /**
     * 获取异常对象
     *
     * @return 异常对象
     */
    Throwable getCause();
}
