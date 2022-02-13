package cn.chuanwise.commandlib.context;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.object.SimpleCommandLibObject;
import lombok.Data;

@Data
public class CommandLibContext extends SimpleCommandLibObject {

    public CommandLibContext(CommandLib commandLib) {
        super(commandLib);
    }
}
