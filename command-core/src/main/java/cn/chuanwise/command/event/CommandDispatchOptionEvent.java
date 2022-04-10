package cn.chuanwise.command.event;

import cn.chuanwise.command.command.OptionInfo;
import cn.chuanwise.command.context.DispatchContext;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

/**
 * 和指令调度选项相关的事件
 *
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public abstract class CommandDispatchOptionEvent
        extends CommandDispatchEvent {

    protected final OptionInfo optionInfo;

    public CommandDispatchOptionEvent(DispatchContext dispatchContext, OptionInfo optionInfo) {
        super(dispatchContext);

        Preconditions.namedArgumentNonNull(optionInfo, "option info");

        this.optionInfo = optionInfo;
    }
}
