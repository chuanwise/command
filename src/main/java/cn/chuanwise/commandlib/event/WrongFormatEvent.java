package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.commandlib.context.DispatchContext;
import cn.chuanwise.commandlib.tree.DispatchFork;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

import java.util.List;

@Data
public class WrongFormatEvent
        extends CommandDispatchEvent {

    protected final List<DispatchFork> dispatchForks;

    public WrongFormatEvent(DispatchContext dispatchContext, List<DispatchFork> dispatchForks) {
        super(dispatchContext);

        Preconditions.argumentNonNull(dispatchForks, "dispatch forks");

        this.dispatchForks = dispatchForks;
    }
}
