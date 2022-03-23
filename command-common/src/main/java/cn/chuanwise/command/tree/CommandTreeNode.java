package cn.chuanwise.command.tree;

import cn.chuanwise.command.Commander;
import cn.chuanwise.command.command.Command;
import cn.chuanwise.command.command.OptionInfo;
import cn.chuanwise.command.completer.Completer;
import cn.chuanwise.command.handler.Priority;
import cn.chuanwise.command.object.AbstractCommanderObject;
import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.Strings;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public abstract class CommandTreeNode
    extends AbstractCommanderObject
    implements Completer {

    protected CommandTreeNode parent;
    protected final List<CommandTreeNode> sons = new ArrayList<>();

    protected Command command;

    public CommandTreeNode(CommandTreeNode parent, Commander commander) {
        super(commander);

        Preconditions.namedArgumentNonNull(parent, "parent command tree");

        this.parent = parent;
    }

    public CommandTreeNode(Commander commander) {
        super(commander);

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

            Preconditions.namedArgumentNonNull(optionInfo, "option info");

            this.optionInfo = optionInfo;
        }
    }

    /** 普通文本占位符 */
    @Data
    protected static class PlainTextElement extends Element {

        protected final String text;
    }

    @SuppressWarnings("unchecked")
    public <T extends CommandTreeNode> List<T> addSon(T son) {
        Preconditions.namedArgumentNonNull(son, "son");

        final List<T> list = CommandTreeNodes.addSon(sons, son);
        for (T t : list) {
            t.parent = this;
        }
        return list;
    }
    
    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }
    
    protected abstract Optional<Element> accept(String argument) throws Exception;

    protected Optional<Element> attempt(String argument) throws Exception {
        return accept(argument);
    }

    public boolean isExecutable() {
        return Objects.nonNull(command);
    }

    public Set<CommandTreeNode> getExecutableSubCommandTrees() {
        final Set<CommandTreeNode> set = new HashSet<>();
        final Queue<CommandTreeNode> queue = new ArrayDeque<>();
        queue.add(this);

        while (!queue.isEmpty()) {
            final CommandTreeNode tree = queue.poll();

            if (tree.isExecutable()) {
                set.add(tree);
            }
            queue.addAll(tree.sons);
        }

        return Collections.unmodifiableSet(set);
    }

    public Set<Command> getSubCommands() {
        return Collections.unmodifiableSet(getExecutableSubCommandTrees().stream()
                .map(CommandTreeNode::getCommand)
                .collect(Collectors.toSet()));
    }

    public Set<CommandTreeNode> getExecutableParentCommandTrees() {
        final Set<CommandTreeNode> set = new HashSet<>();

        CommandTreeNode parent = this.parent;
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
                .map(CommandTreeNode::getCommand)
                .collect(Collectors.toSet()));
    }

    public Set<CommandTreeNode> getRelatedCommandTrees() {
        final Set<CommandTreeNode> set = new HashSet<>(getExecutableSubCommandTrees());
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

    public String getParentFormat() {
        if (Objects.isNull(parent)) {
            return "";
        } else {
            final List<String> usages = new ArrayList<>();
            CommandTreeNode commandTreeNode = parent;
            while (commandTreeNode != null) {
                usages.add(commandTreeNode.getSimpleFormat());
                commandTreeNode = commandTreeNode.parent;
            }

            final StringBuilder stringBuilder = new StringBuilder(usages.get(usages.size() - 1));
            for (int i = usages.size() - 2; i >= 0; i--) {
                stringBuilder.append(" " + usages.get(i));
            }

            return stringBuilder.toString();
        }
    }

    public String getFormat() {
        final String parentFormat = getParentFormat();
        if (Strings.isEmpty(parentFormat)) {
            return getSimpleFormat();
        } else {
            return parentFormat + " " + getSimpleFormat();
        }
    }

    public abstract String getSimpleFormat();

    public String getCompleteUsage() {
        return getSimpleFormat();
    }

    protected SimpleParameterCommandTreeNode createSimpleParameterSon() {
        return addSon(new SimpleParameterCommandTreeNode(commander)).get(0);
    }

    protected List<PlainTextsCommandTreeNode> createPlainTextSon(List<String> texts) {
        return addSon(new PlainTextsCommandTreeNode(texts, commander));
    }

    protected NullableOptionalParameterCommandTreeNode createNullableRemainParameterSon() {
        return addSon(new NullableOptionalParameterCommandTreeNode(commander)).get(0);
    }

    protected NonNullOptionalParameterCommandTreeNode createNonNullRemainParameterSon() {
        return addSon(new NonNullOptionalParameterCommandTreeNode(commander)).get(0);
    }

    protected OptionCommandTreeNode createOptionSon() {
        return addSon(new OptionCommandTreeNode(commander)).get(0);
    }

    public Optional<PlainTextsCommandTreeNode> getPlainTextsSubCommandTree(String text) {
        Preconditions.argumentNonEmpty(text, "text");

        for (CommandTreeNode son : sons) {
            if (son instanceof PlainTextsCommandTreeNode) {
                final PlainTextsCommandTreeNode commandTree = (PlainTextsCommandTreeNode) son;
                if (commandTree.texts.contains(text)) {
                    return Optional.of(commandTree);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public String toString() {
        return getSimpleFormat();
    }
}
