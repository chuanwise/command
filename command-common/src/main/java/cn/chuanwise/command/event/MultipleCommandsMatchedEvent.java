package cn.chuanwise.command.event;

import cn.chuanwise.command.context.DispatchContext;
import cn.chuanwise.command.tree.CommandTreeFork;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

import java.util.List;

@Data
public class MultipleCommandsMatchedEvent
        extends CommandDispatchEvent {

    protected final List<CommandTreeFork> forks;

    public MultipleCommandsMatchedEvent(DispatchContext dispatchContext, List<CommandTreeFork> forks) {
        super(dispatchContext);

        Preconditions.argumentNonEmpty(forks, "command tree forks");

        this.forks = forks;
    }
}
