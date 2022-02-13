package cn.chuanwise.commandlib.tree;

import cn.chuanwise.util.Preconditions;
import lombok.Data;

import java.util.*;

@Data
public class DispatchFork {

    protected CommandTree commandTree;
    protected boolean failed;
    protected final List<CommandTree.Element> elements = new ArrayList<>();

    public DispatchFork(CommandTree commandTree, CommandTree.Element firstElement) {
        Preconditions.argumentNonNull(commandTree, "command tree");
        Preconditions.argumentNonNull(firstElement, "first element");

        this.commandTree = commandTree;
        elements.add(firstElement);
    }

    private DispatchFork(CommandTree commandTree) {
        Preconditions.argumentNonNull(commandTree, "command tree");

        this.commandTree = commandTree;
    }

    /**
     * 调度分支接受一个新的输入，并用它来进行可能的分支
     *
     * @param argument 输入
     * @return 新分支数
     * @throws Exception 运行时出现异常
     */
    public Set<DispatchFork> accept(String argument) throws Exception {
        Preconditions.state(!failed, "dispatch fork failed");

        if (commandTree instanceof RemainParameterCommandTree
                || commandTree instanceof OptionCommandTree) {
            final CommandTree.Element lastElement = elements.get(elements.size() - 1);
            Preconditions.state(lastElement instanceof CommandTree.ValueElement);
            final CommandTree.ValueElement valueElement = (CommandTree.ValueElement) lastElement;

            valueElement.string = valueElement.string + " " + argument;
            return Collections.emptySet();
        }

        final Set<DispatchFork> forks = new HashSet<>();

        boolean succeed = false;
        final CommandTree commandTree = this.commandTree;
        for (CommandTree son : commandTree.sons) {
            final Optional<CommandTree.Element> optionalElement = son.accept(argument);
            if (optionalElement.isPresent()) {
                final CommandTree.Element element = optionalElement.get();
                if (succeed) {
                    forks.add(fork(element));
                } else {
                    this.commandTree = son;
                    elements.add(element);
                    succeed = true;
                }
            }
        }

        if (!succeed) {
            failed = true;
        }

        return forks;
    }

    private DispatchFork fork(CommandTree.Element element) {
        final DispatchFork fork = new DispatchFork(commandTree);

        final List<CommandTree.Element> elements = fork.getElements();
        elements.addAll(this.elements);
        elements.set(elements.size() - 1, element);

        return fork;
    }
}
