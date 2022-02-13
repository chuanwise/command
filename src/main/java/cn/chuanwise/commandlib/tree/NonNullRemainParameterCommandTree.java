package cn.chuanwise.commandlib.tree;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.exception.IllegalOperationException;

import java.util.List;

public class NonNullRemainParameterCommandTree
        extends RemainParameterCommandTree {

    public NonNullRemainParameterCommandTree(CommandLib commandLib) {
        super(commandLib);
    }

    @Override
    public String getSingleUsage() {
        return null;
    }

    @Override
    protected SimpleParameterCommandTree createSimpleParameterSon() {
        throw new IllegalOperationException("不能在最终参数后添加子指令");
    }

    @Override
    protected PlainTextsCommandTree createPlainTextSon(List<String> texts) {
        throw new IllegalOperationException("不能在最终参数后添加子指令");
    }

    @Override
    protected NullableRemainParameterCommandTree createNullableRemainParameterSon() {
        throw new IllegalOperationException("不能在最终参数后添加子指令");
    }

    @Override
    protected NonNullRemainParameterCommandTree createNonNullRemainParameterSon() {
        throw new IllegalOperationException("不能在最终参数后添加子指令");
    }

    @Override
    protected OptionCommandTree createOptionSon() {
        throw new IllegalOperationException("不能在最终参数后添加子指令");
    }
}
