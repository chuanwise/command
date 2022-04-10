package cn.chuanwise.command.wirer;

import cn.chuanwise.command.context.WireContext;
import cn.chuanwise.command.Priority;
import cn.chuanwise.common.space.Container;

/**
 * 装载器
 *
 * @author Chuanwise
 */
@FunctionalInterface
public interface Wirer {
    
    /**
     * 装载
     *
     * @param context 装载器上下文
     * @return 装载结果
     * @throws Exception 装载时出现异常
     */
    Container<?> wire(WireContext context) throws Exception;
}
