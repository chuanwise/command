package cn.chuanwise.command.wirer;

import cn.chuanwise.command.context.WireContext;
import cn.chuanwise.command.event.CommandSenderMismatchedEvent;
import cn.chuanwise.command.wirer.AbstractWirer;
import cn.chuanwise.common.space.Container;
import lombok.Data;

/**
 * 指令发送者自动装配器
 *
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class CommandSenderWrier<T>
    extends AbstractWirer<T> {
    
    public CommandSenderWrier(Class<T> wiredClass) {
        super(wiredClass);
    }
    
    @Override
    public Container<T> wire0(WireContext context) throws Exception {
        final Class<?> parameterClass = context.getParameter().getType();
        final Object commandSender = context.getCommandSender();
        
        if (parameterClass.isInstance(commandSender)) {
            return Container.of((T) commandSender);
        } else {
            final CommandSenderMismatchedEvent event = new CommandSenderMismatchedEvent(context, parameterClass);
            context.getCommander().getEventService().broadcastEvent(event);
            return Container.empty();
        }
    }
}
