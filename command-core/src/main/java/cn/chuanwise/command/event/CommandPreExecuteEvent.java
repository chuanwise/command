package cn.chuanwise.command.event;

import cn.chuanwise.command.context.CommandContext;
import lombok.Data;

@Data
public class CommandPreExecuteEvent
        extends CommandExecuteEvent
        implements Cancellable {

    protected boolean cancelled;

    public CommandPreExecuteEvent(CommandContext commandContext) {
        super(commandContext);
    }
}
