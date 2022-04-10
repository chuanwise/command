package cn.chuanwise.command.context;

import cn.chuanwise.command.Commander;
import cn.chuanwise.command.object.AbstractCommanderObject;
import lombok.Data;

/**
 * 所有上下文的父类
 *
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class CommanderContext
        extends AbstractCommanderObject {

    public CommanderContext(Commander commander) {
        super(commander);
    }
}
