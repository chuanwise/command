package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.context.DispatchContext;
import lombok.Data;

@Data
public class CommandDispatchErrorEvent
        extends CommandDispatchEvent
        implements ErrorEvent {

    protected final Throwable cause;

    public CommandDispatchErrorEvent(DispatchContext dispatchContext, Throwable cause) {
        super(dispatchContext);

        this.cause = cause;
    }
}
