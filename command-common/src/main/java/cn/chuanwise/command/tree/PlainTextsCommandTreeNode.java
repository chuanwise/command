package cn.chuanwise.command.tree;

import cn.chuanwise.command.Commander;
import cn.chuanwise.command.context.CompleteContext;
import cn.chuanwise.common.algorithm.LongestCommonSubsequence;
import cn.chuanwise.common.util.Joiner;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

import java.util.*;

@Data
public class PlainTextsCommandTreeNode
        extends CommandTreeNode {

    protected final List<String> texts;

    public PlainTextsCommandTreeNode(List<String> texts, Commander commander) {
        super(commander);

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
    protected Optional<Element> attempt(String argument) throws Exception {
        final double minSequenceLength = argument.length() * commander.getCommanderConfiguration().getMinCompleterCommonRate();
        for (String text : texts) {
            if (LongestCommonSubsequence.length(text, argument) >= minSequenceLength) {
                return Optional.of(new PlainTextElement(argument));
            }
        }
        return Optional.empty();
    }

    @Override
    public String getSimpleFormat() {
        return texts.get(0);
    }

    @Override
    public String getCompleteUsage() {
        return Joiner.builder().delimiter("|").build().withAll(texts).join();
    }

    @Override
    public Set<String> complete(CompleteContext context) throws Exception {
        if (!texts.isEmpty()) {
            final Set<String> copied = new HashSet<>();
            copied.addAll(texts);
            return Collections.unmodifiableSet(copied);
        } else {
            return Collections.emptySet();
        }
    }

    public void merge(PlainTextsCommandTreeNode commandTree) {
        Preconditions.namedArgumentNonNull(commandTree, "command tree");

        Preconditions.state(Objects.isNull(command) || Objects.isNull(commandTree.command), "无法将两个已注册的指令合并");

        // 从老的树上摘下来
        final CommandTreeNode parent = commandTree.getParent();
        if (Objects.nonNull(parent)) {
            parent.sons.remove(commandTree);
        }

        // 迁移指令
        if (Objects.nonNull(commandTree.command)) {
            command = commandTree.command;
        }

        // 把孩子都加进来
        for (CommandTreeNode son : commandTree.sons) {
            addSon(son);
        }

        // 把 text 加进来
        cn.chuanwise.common.util.Collections.addAllDistinctly(texts, commandTree.getTexts());
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
