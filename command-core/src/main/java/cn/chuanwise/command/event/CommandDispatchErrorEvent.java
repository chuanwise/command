package cn.chuanwise.command.event;

import cn.chuanwise.command.context.DispatchContext;
import lombok.Data;

/**
 * 指令调度时出现异常
 *
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class CommandDispatchErrorEvent
        extends CommandDispatchEvent
        implements ErrorEvent {

    protected final Throwable cause;

    public CommandDispatchErrorEvent(DispatchContext dispatchContext, Throwable cause) {
        super(dispatchContext);

        this.cause = cause;
    }
}
