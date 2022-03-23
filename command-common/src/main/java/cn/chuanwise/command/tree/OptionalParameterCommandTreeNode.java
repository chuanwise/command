package cn.chuanwise.command.tree;

import cn.chuanwise.command.Commander;

public abstract class OptionalParameterCommandTreeNode
        extends ParameterCommandTreeNode
        implements SingletonCommandTreeNode, WeekMatchableCommandTreeNode {

    public OptionalParameterCommandTreeNode(Commander commander) {
        super(commander);
    }
}
