package cn.chuanwise.command.event;

import cn.chuanwise.command.Commander;
import cn.chuanwise.command.command.Command;
import cn.chuanwise.command.object.CommanderObject;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

@Data
public class CommandRegisterEvent
        implements CommanderObject {

    protected final Command command;
    protected boolean cancelled;

    public CommandRegisterEvent(Command command) {
        Preconditions.objectNonNull(command, "command");

        this.command = command;
    }

    @Override
    public Commander getCommander() {
        return command.getCommander();
    }
}
