package cn.chuanwise.command.event;

import cn.chuanwise.command.context.CommandContext;
import lombok.Data;

@Data
public class CommandPostExecuteEvent
        extends CommandExecuteEvent {

    public CommandPostExecuteEvent(CommandContext commandContext) {
        super(commandContext);
    }
}
