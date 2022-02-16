package cn.chuanwise.commandlib.tree;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.command.Command;
import cn.chuanwise.commandlib.command.OptionInfo;
import cn.chuanwise.commandlib.configuration.CommandLibConfiguration;
import cn.chuanwise.commandlib.context.CompleteContext;
import cn.chuanwise.commandlib.object.SimpleCommandLibObject;
import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.Strings;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public abstract class CommandTree extends SimpleCommandLibObject {

    protected CommandTree parent;
    protected final Set<CommandTree> sons = new HashSet<>();

    protected Command command;

    public CommandTree(CommandTree parent, CommandLib commandLib) {
        super(commandLib);

        Preconditions.argumentNonNull(parent, "parent command tree");

        this.parent = parent;
    }

    public CommandTree(CommandLib commandLib) {
        super(commandLib);

        this.parent = null;
    }

    /** 扫描结果的占位符 */
    protected static abstract class Element {

        public abstract String getText();
    }

    /** 带值的占位符 */
    @Data
    protected static class ValueElement extends Element {
        protected String string;

        public ValueElement(String string) {
            this.string = string;
        }

        @Override
        public String getText() {
            return string;
        }
    }

    @Data
    protected static class OptionElement extends ValueElement {

        protected final OptionInfo optionInfo;

        public OptionElement(String string, OptionInfo optionInfo) {
            super(string);

            Preconditions.argumentNonNull(optionInfo, "option info");

            this.optionInfo = optionInfo;
        }
    }

    /** 普通文本占位符 */
    @Data
    protected static class PlainTextElement extends Element {

        protected final String text;
    }

    protected abstract Optional<Element> accept(String argument) throws Exception;

    public Set<String> complete(CompleteContext context) throws Exception {
        return commandLib.pipeline().handleComplete(context);
    }

    public boolean isExecutable() {
        return Objects.nonNull(command);
    }

    public Set<CommandTree> getExecutableSubCommandTrees() {
        final Set<CommandTree> set = new HashSet<>();
        final Queue<CommandTree> queue = new ArrayDeque<>();
        queue.add(this);

        while (!queue.isEmpty()) {
            final CommandTree tree = queue.poll();

            if (tree.isExecutable()) {
                set.add(tree);
            }
            queue.addAll(tree.sons);
        }

        return Collections.unmodifiableSet(set);
    }

    public Set<Command> getSubCommands() {
        return Collections.unmodifiableSet(getExecutableSubCommandTrees().stream()
                .map(CommandTree::getCommand)
                .collect(Collectors.toSet()));
    }

    public Set<CommandTree> getExecutableParentCommandTrees() {
        final Set<CommandTree> set = new HashSet<>();

        CommandTree parent = this.parent;
        while (Objects.nonNull(parent)) {
            if (parent.isExecutable()) {
                set.add(parent);
            }
            parent = parent.parent;
        }

        return Collections.unmodifiableSet(set);
    }

    public Set<Command> getParentCommands() {
        return Collections.unmodifiableSet(getExecutableParentCommandTrees()
                .stream()
                .map(CommandTree::getCommand)
                .collect(Collectors.toSet()));
    }

    public Set<CommandTree> getRelatedCommandTrees() {
        final Set<CommandTree> set = new HashSet<>(getExecutableSubCommandTrees());
        if (isExecutable()) {
            set.add(this);
        }
        set.addAll(getExecutableParentCommandTrees());
        return Collections.unmodifiableSet(set);
    }

    public Set<Command> getRelatedCommands() {
        final Set<Command> set = new HashSet<>();

        getExecutableParentCommandTrees().forEach(x -> set.add(x.command));
        getExecutableSubCommandTrees().forEach(x -> set.add(x.command));

        if (isExecutable()) {
            set.add(command);
        }
        return Collections.unmodifiableSet(set);
    }

    public String getParentUsage() {
        if (Objects.isNull(parent)) {
            return "";
        } else {
            final List<String> usages = new ArrayList<>();
            CommandTree commandTree = parent;
            while (commandTree != null) {
                usages.add(commandTree.getSimpleUsage());
                commandTree = commandTree.parent;
            }

            final StringBuilder stringBuilder = new StringBuilder(usages.get(usages.size() - 1));
            for (int i = usages.size() - 2; i >= 0; i--) {
                stringBuilder.append(" " + usages.get(i));
            }

            return stringBuilder.toString();
        }
    }

    public String getUsage() {
        final String parentUsage = getParentUsage();
        if (Strings.isEmpty(parentUsage)) {
            return getSimpleUsage();
        } else {
            return parentUsage + " " + getSimpleUsage();
        }
    }

    public abstract String getSimpleUsage();

    public String getCompleteUsage() {
        return getSimpleUsage();
    }

    protected SimpleParameterCommandTree createSimpleParameterSon() {
//        // 检查是否已经有孤儿
//        for (CommandTree son : sons) {
//            Preconditions.state(!(son instanceof SingletonCommandTree), "不能为 " + son.getClass().getSimpleName() + " 添加新的兄弟节点");
//        }

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

    protected PlainTextsCommandTree createPlainTextSon(List<String> texts) {
        final CommandLibConfiguration configuration = commandLib.getConfiguration();

        // 检查是否已经有孤儿
        for (CommandTree son : sons) {
            Preconditions.state(!(son instanceof SingletonCommandTree) || !configuration.isStrongMatch(),
                    "除非关闭强匹配 strongMatch，否则不能为其添加新的兄弟节点");
        }

        // 检查是否重复
        for (CommandTree son : sons) {
            if (son instanceof PlainTextsCommandTree) {
                final PlainTextsCommandTree plainTextsCommandTree = (PlainTextsCommandTree) son;

                // 要么完全不相干，要么完全相等
                final List<String> clonedTexts = new ArrayList<>(texts);
                clonedTexts.removeAll(plainTextsCommandTree.getTexts());

                final boolean related = clonedTexts.size() != texts.size();
                if (related) {
                    final boolean equals = clonedTexts.isEmpty() && texts.size() == plainTextsCommandTree.getTexts().size();
                    Preconditions.state(configuration.isMergeIntersectedForks() || equals, "交错的指令分支");

                    return plainTextsCommandTree;
                }
            }
        }

        // 创建新的
        final PlainTextsCommandTree commandTree = new PlainTextsCommandTree(texts, getCommandLib());
        commandTree.setParent(this);
        sons.add(commandTree);
        return commandTree;
    }

    protected NullableRemainParameterCommandTree createNullableRemainParameterSon() {
        // 检查是否已经有孤儿
        Preconditions.state(sons.isEmpty() || !commandLib.getConfiguration().isStrongMatch(), "除非关闭强匹配 strongMatch，否则剩余参数分支不能具备兄弟节点");

        final NullableRemainParameterCommandTree commandTree = new NullableRemainParameterCommandTree(getCommandLib());
        commandTree.setParent(this);
        sons.add(commandTree);

        return commandTree;
    }

    protected NonNullRemainParameterCommandTree createNonNullRemainParameterSon() {
        // 检查是否已经有孤儿
        Preconditions.state(sons.isEmpty() || !commandLib.getConfiguration().isStrongMatch(), "除非关闭强匹配 strongMatch，否则剩余参数分支不能具备兄弟节点");

        final NonNullRemainParameterCommandTree commandTree = new NonNullRemainParameterCommandTree(getCommandLib());
        commandTree.setParent(this);
        sons.add(commandTree);

        return commandTree;
    }

    protected OptionCommandTree createOptionSon() {
//        // 检查是否已经有孤儿
//        Preconditions.state(sons.isEmpty(), "选项列表不能具备兄弟节点");

        final OptionCommandTree commandTree = new OptionCommandTree(getCommandLib());
        commandTree.setParent(this);
        sons.add(commandTree);

        return commandTree;
    }

    public Optional<PlainTextsCommandTree> getPlainTextsSubCommandTree(String text) {
        Preconditions.argumentNonEmpty(text, "text");

        for (CommandTree son : sons) {
            if (son instanceof PlainTextsCommandTree) {
                final PlainTextsCommandTree commandTree = (PlainTextsCommandTree) son;
                if (commandTree.texts.contains(text)) {
                    return Optional.of(commandTree);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public String toString() {
        return getSimpleUsage();
    }
}
