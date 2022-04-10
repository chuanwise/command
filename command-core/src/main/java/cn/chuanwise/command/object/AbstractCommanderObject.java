package cn.chuanwise.command.object;

import cn.chuanwise.command.Commander;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

/**
 * @see CommanderObject
 * @author Chuanwise
 */
@Data
public abstract class AbstractCommanderObject
        implements CommanderObject {

    protected final Commander commander;

    public AbstractCommanderObject(Commander commander) {
        Preconditions.namedArgumentNonNull(commander, "commander");

        this.commander = commander;
    }
}
