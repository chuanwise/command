package cn.chuanwise.command.event;

import cn.chuanwise.command.context.CommandContext;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

@Data
public class CommandExecuteErrorEvent
        extends CommandExecuteEvent
        implements ErrorEvent {

    protected final Throwable cause;

    public CommandExecuteErrorEvent(CommandContext commandContext, Throwable cause) {
        super(commandContext);

        Preconditions.namedArgumentNonNull(cause, "cause");

        this.cause = cause;
    }
}
