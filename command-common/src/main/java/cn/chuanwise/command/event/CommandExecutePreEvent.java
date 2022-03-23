package cn.chuanwise.command.event;

import cn.chuanwise.command.context.CommandContext;
import lombok.Data;

@Data
public class CommandExecutePreEvent
        extends CommandExecuteEvent
        implements Cancellable {

    protected boolean cancelled;

    public CommandExecutePreEvent(CommandContext commandContext) {
        super(commandContext);
    }
}
