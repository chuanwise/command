package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.command.OptionInfo;
import cn.chuanwise.commandlib.context.DispatchContext;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

@Data
public class ReassignOptionEvent
        extends CommandDispatchOptionEvent {

    protected final String firstString;
    protected final String secondString;

    public ReassignOptionEvent(DispatchContext dispatchContext, OptionInfo optionInfo, String firstString, String secondString) {
        super(dispatchContext, optionInfo);

        Preconditions.argumentNonNull(firstString, "first string");
        Preconditions.argumentNonNull(secondString, "second string");

        this.firstString = firstString;
        this.secondString = secondString;
    }
}
