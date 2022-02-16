package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.command.Command;
import cn.chuanwise.commandlib.command.OptionInfo;
import cn.chuanwise.commandlib.context.DispatchContext;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

@Data
public class UndefinedOptionValueEvent
        extends CommandDispatchEvent {

    protected final OptionInfo optionInfo;
    protected final String string;
    protected final Command command;

    public UndefinedOptionValueEvent(DispatchContext dispatchContext, OptionInfo optionInfo, String string, Command command) {
        super(dispatchContext);

        Preconditions.argumentNonNull(optionInfo, "option info");
        Preconditions.argumentNonNull(command, "command");
        Preconditions.argumentNonNull(string, "string");

        this.optionInfo = optionInfo;
        this.string = string;
        this.command = command;
    }
}
