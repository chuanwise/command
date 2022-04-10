package cn.chuanwise.command.tree;

import cn.chuanwise.command.Commander;

public class NonNullOptionalParameterCommandTreeNode
        extends OptionalParameterCommandTreeNode {

    public NonNullOptionalParameterCommandTreeNode(Commander commander) {
        super(commander);
    }

    @Override
    public String getSimpleFormat() {
        return "[~]";
    }
}
