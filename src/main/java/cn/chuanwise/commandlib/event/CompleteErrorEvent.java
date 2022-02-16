package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.context.DispatchContext;
import cn.chuanwise.commandlib.object.CommandLibObject;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

@Data
public class CompleteErrorEvent
        implements CommandLibObject, ErrorEvent {

    protected final DispatchContext dispatchContext;
    protected final Throwable cause;

    public CompleteErrorEvent(DispatchContext dispatchContext, Throwable cause) {
        Preconditions.argumentNonNull(dispatchContext, "dispatch context");

        this.dispatchContext = dispatchContext;
        this.cause = cause;
    }

    @Override
    public CommandLib getCommandLib() {
        return dispatchContext.getCommandLib();
    }
}
