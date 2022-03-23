package cn.chuanwise.command.event;

import cn.chuanwise.command.context.CommandContext;
import cn.chuanwise.command.wirer.Wirer;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

import java.lang.reflect.Parameter;

@Data
public class WireMismatchedEvent
        extends WireEvent {

    protected final Object providedObject;

    public WireMismatchedEvent(CommandContext commandContext,
                               int index,
                               Parameter parameter,
                               Wirer wirer,
                               Object wiredObject) {
        super(commandContext, index, parameter, wirer);

        Preconditions.namedArgumentNonNull(wiredObject, "wired object");

        this.providedObject = wiredObject;
    }
}
