package cn.chuanwise.command.event;

import cn.chuanwise.command.command.Command;
import cn.chuanwise.command.command.OptionInfo;
import cn.chuanwise.command.context.DispatchContext;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

@Data
public class UndefinedOptionValueEvent
    extends CommandDispatchOptionEvent {

    protected final String string;
    protected final Command command;

    public UndefinedOptionValueEvent(DispatchContext dispatchContext, OptionInfo optionInfo, String string, Command command) {
        super(dispatchContext, optionInfo);

        Preconditions.namedArgumentNonNull(command, "command");
        Preconditions.namedArgumentNonNull(string, "string");

        this.string = string;
        this.command = command;
    }
}
