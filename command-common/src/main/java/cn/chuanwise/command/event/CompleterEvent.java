package cn.chuanwise.command.event;

import cn.chuanwise.command.Commander;
import cn.chuanwise.command.completer.Completer;
import cn.chuanwise.command.object.AbstractCommanderObject;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

@Data
public class CompleterEvent
        extends AbstractCommanderObject {

    protected final Completer completer;

    public CompleterEvent(Commander commander, Completer completer) {
        super(commander);

        Preconditions.namedArgumentNonNull(completer, "completer");

        this.completer = completer;
    }
}
