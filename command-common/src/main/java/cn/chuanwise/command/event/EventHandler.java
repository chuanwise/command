package cn.chuanwise.command.event;

import cn.chuanwise.command.handler.Priority;

/**
 * 事件监听器
 *
 * @author Chuanwise
 */
public interface EventHandler {
    
    /**
     * 事件监听器
     *
     * @param event 事件
     * @return 事件是否被监听
     * @throws Exception 监听途中出现异常
     */
    boolean handleEvent(Object event) throws Exception;
    
    /**
     * 获取事件类型
     *
     * @return 事件类型
     */
    Class<?> getEventClass();
    
    /**
     * 获取监听器优先级
     *
     * @return 监听器优先级
     */
    Priority getPriority();
    
    /**
     * 询问监听器是否总是生效
     *
     * @return 监听器是否总是生效
     */
    boolean isAlwaysValid();
}
