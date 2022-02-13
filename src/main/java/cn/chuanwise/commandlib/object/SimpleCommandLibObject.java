package cn.chuanwise.commandlib.object;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

@Data
public class SimpleCommandLibObject
        implements CommandLibObject {

    protected final CommandLib commandLib;

    public SimpleCommandLibObject(CommandLib commandLib) {
        Preconditions.argumentNonNull(commandLib, "command lib");

        this.commandLib = commandLib;
    }
}
