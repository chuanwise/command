package cn.chuanwise.command.event;

import cn.chuanwise.command.command.OptionInfo;
import cn.chuanwise.command.context.DispatchContext;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

@Data
public class ReassignOptionEvent
        extends CommandDispatchOptionEvent {

    protected final String firstString;
    protected final String secondString;

    public ReassignOptionEvent(DispatchContext dispatchContext, OptionInfo optionInfo, String firstString, String secondString) {
        super(dispatchContext, optionInfo);

        Preconditions.objectNonNull(firstString, "first string");
        Preconditions.objectNonNull(secondString, "second string");

        this.firstString = firstString;
        this.secondString = secondString;
    }
}
