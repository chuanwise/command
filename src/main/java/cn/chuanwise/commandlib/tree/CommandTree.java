package cn.chuanwise.commandlib.tree;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.command.Command;
import cn.chuanwise.commandlib.completer.Completer;
import cn.chuanwise.commandlib.context.CompleteContext;
import cn.chuanwise.commandlib.context.ReferenceInfo;
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

    protected final Set<Completer> completers = new HashSet<>();

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

    /** 普通文本占位符 */
    @Data
    protected static class PlainTextElement extends Element {

        protected final String text;
    }

    protected abstract Optional<Element> accept(String argument) throws Exception;

    public Set<String> complete(CompleteContext context) throws Exception {
        final Set<String> set = new HashSet<>();
        for (Completer completer : completers) {
            set.addAll(completer.complete(context));
        }
        return set;
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

    public String getParentUsage() {
        if (Objects.isNull(parent)) {
            return "";
        } else {
            final List<String> usages = new ArrayList<>();
            CommandTree commandTree = parent;
            while (commandTree != null) {
                usages.add(commandTree.getSingleUsage());
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
            return getSingleUsage();
        } else {
            return parentUsage + " " + getSingleUsage();
        }
    }

    public abstract String getSingleUsage();

    protected abstract SimpleParameterCommandTree createSimpleParameterSon();

    protected abstract PlainTextsCommandTree createPlainTextSon(List<String> texts);

    protected abstract NullableRemainParameterCommandTree createNullableRemainParameterSon();

    protected abstract NonNullRemainParameterCommandTree createNonNullRemainParameterSon();

    protected abstract OptionCommandTree createOptionSon();
}
