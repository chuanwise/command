package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.context.CommandContext;
import lombok.Data;

@Data
public class CommandExecutePostEvent
        extends CommandExecuteEvent {

    public CommandExecutePostEvent(CommandContext commandContext) {
        super(commandContext);
    }
}
