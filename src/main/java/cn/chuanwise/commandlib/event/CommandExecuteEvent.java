package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.commandlib.object.CommandLibObject;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

@Data
public class CommandExecuteEvent
        implements CommandLibObject {

    protected final CommandContext commandContext;

    public CommandExecuteEvent(CommandContext commandContext) {
        Preconditions.argumentNonNull(commandContext, "command context");

        this.commandContext = commandContext;
    }

    @Override
    public CommandLib getCommandLib() {
        return commandContext.getCommandLib();
    }
}
