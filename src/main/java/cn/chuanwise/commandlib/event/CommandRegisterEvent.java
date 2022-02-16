package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.command.Command;
import cn.chuanwise.commandlib.object.CommandLibObject;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

@Data
public class CommandRegisterEvent
        implements CommandLibObject {

    protected final Command command;
    protected boolean cancelled;

    public CommandRegisterEvent(Command command) {
        Preconditions.argumentNonNull(command, "command");

        this.command = command;
    }

    @Override
    public CommandLib getCommandLib() {
        return command.getCommandLib();
    }
}
