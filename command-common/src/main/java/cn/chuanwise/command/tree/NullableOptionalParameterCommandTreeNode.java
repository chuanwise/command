package cn.chuanwise.command.tree;

import cn.chuanwise.command.Commander;

public class NullableOptionalParameterCommandTreeNode
        extends OptionalParameterCommandTreeNode {

    public NullableOptionalParameterCommandTreeNode(Commander commander) {
        super(commander);
    }

    @Override
    public String getSimpleFormat() {
        return "[?~]";
    }
}
