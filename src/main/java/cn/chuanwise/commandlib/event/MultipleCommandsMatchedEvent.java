package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.context.DispatchContext;
import cn.chuanwise.commandlib.tree.DispatchFork;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

import java.util.List;

@Data
public class MultipleCommandsMatchedEvent
        extends CommandDispatchEvent {

    protected final List<DispatchFork> forks;

    public MultipleCommandsMatchedEvent(DispatchContext dispatchContext, List<DispatchFork> forks) {
        super(dispatchContext);

        Preconditions.argumentNonEmpty(forks, "dispatch forks");

        this.forks = forks;
    }
}
