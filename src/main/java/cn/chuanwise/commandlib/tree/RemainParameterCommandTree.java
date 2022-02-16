package cn.chuanwise.commandlib.tree;

import cn.chuanwise.commandlib.CommandLib;

public abstract class RemainParameterCommandTree
        extends ParameterCommandTree
        implements SingletonCommandTree, WeekMatchableCommandTree {

    public RemainParameterCommandTree(CommandLib commandLib) {
        super(commandLib);
    }
}
