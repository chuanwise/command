package cn.chuanwise.commandlib.tree;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.exception.IllegalOperationException;
import cn.chuanwise.util.Preconditions;

import java.util.List;

public class NullableRemainParameterCommandTree
        extends RemainParameterCommandTree {

    public NullableRemainParameterCommandTree(CommandLib commandLib) {
        super(commandLib);
    }

    @Override
    public String getSimpleUsage() {
        return "[?~]";
    }

    private void checkStrongMatch() {
        Preconditions.state(!commandLib.getConfiguration().isStrongMatch(), "除非关闭强匹配 strongMatch，否则不能在可空剩余参数下继续添加分支");
    }

    @Override
    protected SimpleParameterCommandTree createSimpleParameterSon() {
        checkStrongMatch();
        return super.createSimpleParameterSon();
    }

    @Override
    protected PlainTextsCommandTree createPlainTextSon(List<String> texts) {
        checkStrongMatch();
        return createPlainTextSon(texts);
    }

    @Override
    protected NullableRemainParameterCommandTree createNullableRemainParameterSon() {
        checkStrongMatch();
        return super.createNullableRemainParameterSon();
    }

    @Override
    protected NonNullRemainParameterCommandTree createNonNullRemainParameterSon() {
        checkStrongMatch();
        return super.createNonNullRemainParameterSon();
    }

    @Override
    protected OptionCommandTree createOptionSon() {
        checkStrongMatch();
        return super.createOptionSon();
    }
}
