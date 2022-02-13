package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.context.DispatchContext;
import cn.chuanwise.commandlib.tree.CommandTree;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

@Data
public class UnhandledCommandEvent
        extends CommandDispatchEvent {

    protected final CommandTree commandTree;

    public UnhandledCommandEvent(DispatchContext dispatchContext, CommandTree commandTree) {
        super(dispatchContext);

        Preconditions.argumentNonNull(commandTree, "command tree");

        this.commandTree = commandTree;
    }
}
