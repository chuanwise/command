package cn.chuanwise.command.wirer;

import cn.chuanwise.command.context.WireContext;
import cn.chuanwise.command.handler.Priority;
import cn.chuanwise.common.space.Container;

/**
 * 指令发送人填充器
 *
 * @author Chuanwise
 */
public class CommandSenderWirer
        extends AbstractWirer<Object> {
    
    public CommandSenderWirer(Class<Object> wiredClass, Priority priority) {
        super(wiredClass, priority);
    }
    
    @Override
    protected Container<Object> wire0(WireContext context) throws Exception {
        return Container.of(context.getCommandSender());
    }
}
