package cn.chuanwise.command.event;

import cn.chuanwise.command.context.CommandContext;
import lombok.Data;

/**
 * 指令发送人不匹配事件
 *
 * @author Chuanwise
 */
@Data
public class CommandSenderMismatchedEvent {

    protected final CommandContext commandContext;
    
    protected final Class<?> commandSenderClass;
}
