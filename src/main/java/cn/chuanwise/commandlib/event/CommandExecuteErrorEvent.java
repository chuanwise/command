package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

@Data
public class CommandExecuteErrorEvent
        extends CommandExecuteEvent
        implements ErrorEvent {

    protected final Throwable cause;

    public CommandExecuteErrorEvent(CommandContext commandContext, Throwable cause) {
        super(commandContext);

        Preconditions.argumentNonNull(cause, "cause");

        this.cause = cause;
    }
}
