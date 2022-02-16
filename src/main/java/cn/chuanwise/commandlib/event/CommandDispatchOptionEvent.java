package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.command.OptionInfo;
import cn.chuanwise.commandlib.context.DispatchContext;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

@Data
public abstract class CommandDispatchOptionEvent
        extends CommandDispatchEvent {

    protected final OptionInfo optionInfo;

    public CommandDispatchOptionEvent(DispatchContext dispatchContext, OptionInfo optionInfo) {
        super(dispatchContext);

        Preconditions.argumentNonNull(optionInfo, "option info");

        this.optionInfo = optionInfo;
    }
}
