package cn.chuanwise.commandlib.tree;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.configuration.CommandLibConfiguration;
import cn.chuanwise.util.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SimpleParameterCommandTree
        extends ParameterCommandTree {

    public SimpleParameterCommandTree(CommandLib commandLib) {
        super(commandLib);
    }

    @Override
    public String getSimpleUsage() {
        return "[?]";
    }
}
