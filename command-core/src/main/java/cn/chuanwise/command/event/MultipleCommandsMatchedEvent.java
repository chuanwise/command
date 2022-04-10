package cn.chuanwise.command.event;

import cn.chuanwise.command.context.DispatchContext;
import cn.chuanwise.command.tree.CommandTreeFork;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

import java.util.List;

@Data
public class MultipleCommandsMatchedEvent
    extends CommandDispatchEvent {

    protected final List<CommandTreeFork> commandTreeForks;

    public MultipleCommandsMatchedEvent(DispatchContext dispatchContext, List<CommandTreeFork> commandTreeForks) {
        super(dispatchContext);

        Preconditions.argumentNonEmpty(commandTreeForks, "command tree forks");

        this.commandTreeForks = commandTreeForks;
    }
}
