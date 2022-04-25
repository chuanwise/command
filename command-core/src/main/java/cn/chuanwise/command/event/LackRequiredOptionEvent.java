package cn.chuanwise.command.event;

import cn.chuanwise.command.command.Command;
import cn.chuanwise.command.command.OptionInfo;
import cn.chuanwise.command.context.DispatchContext;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

@Data
public class LackRequiredOptionEvent
    extends CommandDispatchOptionEvent {

    protected final Command command;

    public LackRequiredOptionEvent(DispatchContext dispatchContext, Command command, OptionInfo optionInfo) {
        super(dispatchContext, optionInfo);

        Preconditions.objectNonNull(command, "command");

        this.command = command;
    }
}
