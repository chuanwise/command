package cn.chuanwise.command.event;

import cn.chuanwise.command.context.DispatchContext;
import cn.chuanwise.command.tree.CommandTreeNode;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

@Data
public class UnhandledCommandEvent
        extends CommandDispatchEvent {

    protected final CommandTreeNode commandTreeNode;

    public UnhandledCommandEvent(DispatchContext dispatchContext, CommandTreeNode commandTreeNode) {
        super(dispatchContext);

        Preconditions.objectNonNull(commandTreeNode, "command tree");

        this.commandTreeNode = commandTreeNode;
    }
}
