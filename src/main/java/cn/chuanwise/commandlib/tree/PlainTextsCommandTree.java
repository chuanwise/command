package cn.chuanwise.commandlib.tree;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.context.CompleteContext;
import cn.chuanwise.util.CollectionUtil;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

import java.util.*;

@Data
public class PlainTextsCommandTree
        extends CommandTree {

    protected final List<String> texts;

    public PlainTextsCommandTree(List<String> texts, CommandLib commandLib) {
        super(commandLib);

        Preconditions.argumentNonEmpty(texts, "texts");

        this.texts = texts;
    }

    @Override
    protected Optional<Element> accept(String argument) throws Exception {
        if (texts.contains(argument)) {
            return Optional.of(new PlainTextElement(argument));
        }
        return Optional.empty();
    }

    @Override
    public String getSingleUsage() {
        return CollectionUtil.toString(texts, "|");
    }

    @Override
    public Set<String> complete(CompleteContext context) throws Exception {
        final Set<String> strings = super.complete(context);
        strings.addAll(texts);
        return strings;
    }

    @Override
    protected SimpleParameterCommandTree createSimpleParameterSon() {
        // 检查是否已经有孤儿
        for (CommandTree son : sons) {
            Preconditions.state(!(son instanceof SingletonCommandTree), "不能为 " + son.getClass().getSimpleName() + " 添加新的兄弟节点");
        }

        // 寻找第一个简单参数孩子
        SimpleParameterCommandTree commandTree = null;
        for (CommandTree son : sons) {
            if (son instanceof SimpleParameterCommandTree) {
                commandTree = (SimpleParameterCommandTree) son;
                break;
            }
        }

        if (Objects.isNull(commandTree)) {
            commandTree = new SimpleParameterCommandTree(getCommandLib());
            commandTree.setParent(this);
            sons.add(commandTree);
        }

        return commandTree;
    }

    @Override
    protected PlainTextsCommandTree createPlainTextSon(List<String> texts) {
        // 检查是否已经有孤儿
        for (CommandTree son : sons) {
            Preconditions.state(!(son instanceof SingletonCommandTree), "不能为 " + son.getClass().getSimpleName() + " 添加新的兄弟节点");
        }

        // 检查是否重复
        for (CommandTree son : sons) {
            if (son instanceof PlainTextsCommandTree) {
                final PlainTextsCommandTree plainTextsCommandTree = (PlainTextsCommandTree) son;
                final List<String> clonedPlainTexts = new ArrayList<>(plainTextsCommandTree.texts);

                clonedPlainTexts.removeAll(texts);
                if (clonedPlainTexts.isEmpty() && plainTextsCommandTree.texts.size() == texts.size()) {
                    return plainTextsCommandTree;
                }

                Preconditions.state(clonedPlainTexts.size() == plainTextsCommandTree.texts.size(), "交错的指令分支");
            }
        }

        // 创建新的
        final PlainTextsCommandTree commandTree = new PlainTextsCommandTree(texts, getCommandLib());
        commandTree.setParent(this);
        sons.add(commandTree);
        return commandTree;
    }

    @Override
    protected NullableRemainParameterCommandTree createNullableRemainParameterSon() {
        // 检查是否已经有孤儿
        Preconditions.state(sons.isEmpty(), "剩余参数分支不能具备兄弟节点");

        final NullableRemainParameterCommandTree commandTree = new NullableRemainParameterCommandTree(getCommandLib());
        commandTree.setParent(this);
        sons.add(commandTree);

        return commandTree;
    }

    @Override
    protected NonNullRemainParameterCommandTree createNonNullRemainParameterSon() {
        // 检查是否已经有孤儿
        Preconditions.state(sons.isEmpty(), "剩余参数分支不能具备兄弟节点");

        final NonNullRemainParameterCommandTree commandTree = new NonNullRemainParameterCommandTree(getCommandLib());
        commandTree.setParent(this);
        sons.add(commandTree);

        return commandTree;
    }

    @Override
    protected OptionCommandTree createOptionSon() {
        // 检查是否已经有孤儿
        Preconditions.state(sons.isEmpty(), "选项列表不能具备兄弟节点");

        final OptionCommandTree commandTree = new OptionCommandTree(getCommandLib());
        commandTree.setParent(this);
        sons.add(commandTree);

        return commandTree;
    }
}
