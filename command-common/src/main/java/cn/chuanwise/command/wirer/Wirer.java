package cn.chuanwise.command.wirer;

import cn.chuanwise.command.context.WireContext;
import cn.chuanwise.command.handler.Priority;
import cn.chuanwise.common.api.ExceptionFunction;
import cn.chuanwise.common.space.Container;
import cn.chuanwise.common.util.Preconditions;

/**
 * 装载器
 *
 * @author Chuanwise
 */
public interface Wirer {
    
    /**
     * 装载
     *
     * @param context 装载器上下文
     * @return 装载结果
     * @throws Exception 装载时出现异常
     */
    Container<?> wire(WireContext context) throws Exception;
    
    /**
     * 获取装配器优先级
     *
     * @return 装配器优先级
     */
    Priority getPriority();
    
    /**
     * 获取装配类型
     *
     * @return 装配类型
     */
    Class<?> getWiredClass();
}
