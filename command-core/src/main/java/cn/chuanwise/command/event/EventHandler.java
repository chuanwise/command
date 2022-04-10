package cn.chuanwise.command.event;

/**
 * 事件监听器
 *
 * @author Chuanwise
 */
@FunctionalInterface
public interface EventHandler {
    
    /**
     * 事件监听器
     *
     * @param event 事件
     * @return 事件是否被监听
     * @throws Exception 监听途中出现异常
     */
    boolean handleEvent(Object event) throws Exception;
}
