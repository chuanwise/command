package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.command.Command;
import cn.chuanwise.commandlib.command.OptionInfo;
import cn.chuanwise.commandlib.context.DispatchContext;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

@Data
public class LackRequiredOptionEvent
        extends CommandDispatchOptionEvent {

    protected final Command command;

    public LackRequiredOptionEvent(DispatchContext dispatchContext, Command command, OptionInfo optionInfo) {
        super(dispatchContext, optionInfo);

        Preconditions.argumentNonNull(command, "command");

        this.command = command;
    }
}
