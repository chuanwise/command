package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.context.DispatchContext;
import cn.chuanwise.commandlib.object.CommandLibObject;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

@Data
public class CommandDispatchEvent
        implements CommandLibObject {

    protected final DispatchContext dispatchContext;

    public CommandDispatchEvent(DispatchContext dispatchContext) {
        Preconditions.argumentNonNull(dispatchContext, "dispatch context");

        this.dispatchContext = dispatchContext;
    }

    @Override
    public CommandLib getCommandLib() {
        return dispatchContext.getCommandLib();
    }
}
