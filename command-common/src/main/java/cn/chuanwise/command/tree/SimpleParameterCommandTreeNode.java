package cn.chuanwise.command.tree;

import cn.chuanwise.command.Commander;

public class SimpleParameterCommandTreeNode
        extends ParameterCommandTreeNode {

    public SimpleParameterCommandTreeNode(Commander commander) {
        super(commander);
    }

    @Override
    public String getSimpleFormat() {
        return "[?]";
    }
}
