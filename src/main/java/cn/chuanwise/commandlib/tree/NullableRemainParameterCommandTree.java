package cn.chuanwise.commandlib.tree;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.exception.IllegalOperationException;

import java.util.List;

public class NullableRemainParameterCommandTree
        extends RemainParameterCommandTree {

    public NullableRemainParameterCommandTree(CommandLib commandLib) {
        super(commandLib);
    }

    @Override
    public String getSingleUsage() {
        return null;
    }

    @Override
    protected SimpleParameterCommandTree createSimpleParameterSon() {
        throw new IllegalOperationException("不能在可空剩余参数下继续添加分支");
    }

    @Override
    protected PlainTextsCommandTree createPlainTextSon(List<String> texts) {
        throw new IllegalOperationException("不能在可空剩余参数下继续添加分支");
    }

    @Override
    protected NullableRemainParameterCommandTree createNullableRemainParameterSon() {
        throw new IllegalOperationException("不能在可空剩余参数下继续添加分支");
    }

    @Override
    protected NonNullRemainParameterCommandTree createNonNullRemainParameterSon() {
        throw new IllegalOperationException("不能在可空剩余参数下继续添加分支");
    }

    @Override
    protected OptionCommandTree createOptionSon() {
        throw new IllegalOperationException("不能在可空剩余参数下继续添加分支");
    }
}
