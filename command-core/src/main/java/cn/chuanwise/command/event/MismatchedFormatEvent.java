package cn.chuanwise.command.event;

import cn.chuanwise.command.context.DispatchContext;
import cn.chuanwise.command.tree.CommandTreeFork;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

import java.util.List;

@Data
public class MismatchedFormatEvent
    extends CommandDispatchEvent {

    protected final List<CommandTreeFork> commandTreeForks;

    public MismatchedFormatEvent(DispatchContext dispatchContext, List<CommandTreeFork> commandTreeForks) {
        super(dispatchContext);

        Preconditions.namedArgumentNonNull(commandTreeForks, "command tree forks");

        this.commandTreeForks = commandTreeForks;
    }
}
