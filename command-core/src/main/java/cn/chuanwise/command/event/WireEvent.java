package cn.chuanwise.command.event;

import cn.chuanwise.command.context.CommandContext;
import cn.chuanwise.command.wirer.Wirer;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

import java.lang.reflect.Parameter;

@Data
public class WireEvent
        extends CommandExecuteEvent {

    protected final int index;
    protected final Wirer wirer;
    protected final Parameter parameter;

    public WireEvent(CommandContext commandContext,
                     int index,
                     Parameter parameter,
                     Wirer wirer) {
        super(commandContext);

        Preconditions.namedArgumentNonNull(parameter, "parameter");
        Preconditions.namedArgumentNonNull(wirer, "provider");
        Preconditions.argument(index >= 0, "index");

        this.parameter = parameter;
        this.index = index;
        this.wirer = wirer;
    }
}
