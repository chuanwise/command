package cn.chuanwise.command.event;

import cn.chuanwise.command.context.CommandContext;
import lombok.Data;

@Data
public class CommandExecutePostEvent
        extends CommandExecuteEvent {

    public CommandExecutePostEvent(CommandContext commandContext) {
        super(commandContext);
    }
}
