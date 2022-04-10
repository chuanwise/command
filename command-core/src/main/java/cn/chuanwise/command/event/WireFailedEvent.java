package cn.chuanwise.command.event;

import cn.chuanwise.command.context.CommandContext;
import cn.chuanwise.command.wirer.Wirer;
import lombok.Data;

import java.lang.reflect.Parameter;

@Data
public class WireFailedEvent
    extends WireEvent {

    public WireFailedEvent(CommandContext commandContext,
                           int index,
                           Parameter parameter,
                           Wirer wirer) {
        super(commandContext, index, parameter, wirer);
    }
}
