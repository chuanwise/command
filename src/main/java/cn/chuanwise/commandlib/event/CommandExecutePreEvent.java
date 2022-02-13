package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.context.CommandContext;
import lombok.Data;

@Data
public class CommandExecutePreEvent
        extends CommandExecuteEvent
        implements CancellableEvent {

    protected boolean cancelled;

    public CommandExecutePreEvent(CommandContext commandContext) {
        super(commandContext);
    }
}
