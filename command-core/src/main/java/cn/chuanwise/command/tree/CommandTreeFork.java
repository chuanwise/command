package cn.chuanwise.command.tree;

import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.Strings;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class CommandTreeFork
        implements Cloneable {

    protected CommandTreeNode commandTreeNode;
    protected boolean failed;
    protected final List<CommandTreeNode.Element> elements = new ArrayList<>();

    public CommandTreeFork(CommandTreeNode commandTreeNode, CommandTreeNode.Element firstElement) {
        Preconditions.objectNonNull(commandTreeNode, "command tree");
        Preconditions.objectNonNull(firstElement, "first element");

        this.commandTreeNode = commandTreeNode;
        elements.add(firstElement);
    }

    public CommandTreeFork(CommandTreeNode commandTreeNode) {
        Preconditions.objectNonNull(commandTreeNode, "command tree");

        this.commandTreeNode = commandTreeNode;
    }

    public boolean isWeek() {
        return commandTreeNode instanceof WeekMatchableCommandTreeNode;
    }

    public boolean isStrong() {
        return !isWeek();
    }

    /**
     * 调度分支接受一个新的输入，并用它来进行可能的分支
     *
     * @param argument 输入
     * @return 新分支数
     * @throws Exception 运行时出现异常
     */
    public Set<CommandTreeFork> accept(String argument) throws Exception {
        Preconditions.state(!failed, "command tree fork failed");

        if (this.commandTreeNode instanceof OptionalParameterCommandTreeNode) {
            final CommandTreeNode.Element lastElement = elements.get(elements.size() - 1);
            Preconditions.state(lastElement instanceof CommandTreeNode.ValueElement);
            final CommandTreeNode.ValueElement valueElement = (CommandTreeNode.ValueElement) lastElement;

            valueElement.string = valueElement.string + " " + argument;
            return Collections.emptySet();
        }

        final Set<CommandTreeFork> forks = new HashSet<>();

        boolean forked = false;
        final CommandTreeNode commandTreeNode = this.commandTreeNode;

        if (commandTreeNode instanceof OptionCommandTreeNode) {
            final OptionCommandTreeNode tree = (OptionCommandTreeNode) commandTreeNode;
            final Optional<CommandTreeNode.Element> optionalElement = tree.accept(argument);
            if (optionalElement.isPresent()) {
                elements.add(optionalElement.get());
                forked = true;
            }
        }
        for (CommandTreeNode son : commandTreeNode.sons) {
            final Optional<CommandTreeNode.Element> optionalElement = son.accept(argument);
            if (optionalElement.isPresent()) {
                final CommandTreeNode.Element element = optionalElement.get();
                if (forked) {
                    forks.add(forkChange(son, element));
                } else {
                    this.commandTreeNode = son;
                    elements.add(element);
                    forked = true;
                }
            }
        }

        if (!forked) {
            failed = true;
        }

        return forks;
    }

    public Set<CommandTreeFork> attempt(String argument) throws Exception {
        Preconditions.state(!failed, "command tree fork failed");

        if (Strings.isEmpty(argument)) {
            final CommandTreeNode.PlainTextElement newElement = new CommandTreeNode.PlainTextElement("");
            elements.add(newElement);

            if (commandTreeNode instanceof OptionCommandTreeNode) {
                return Collections.emptySet();
            } else {
                failed = true;
                return Collections.unmodifiableSet(commandTreeNode.sons.stream()
                        .map(x -> forkChange(x, newElement))
                        .collect(Collectors.toSet()));
            }
        }

        if (commandTreeNode instanceof OptionCommandTreeNode) {
            final Optional<CommandTreeNode.Element> optional = commandTreeNode.attempt(argument);
            if (optional.isPresent()) {
                final CommandTreeNode.Element element = optional.get();
                elements.add(element);
            } else {
                failed = true;
            }
            return Collections.emptySet();
        } else {
            boolean attempted = false;
            final Set<CommandTreeFork> set = new HashSet<>();
            for (CommandTreeNode son : commandTreeNode.sons) {
                final Optional<CommandTreeNode.Element> optional = son.attempt(argument);
                if (optional.isPresent()) {
                    final CommandTreeNode.Element element = optional.get();
                    if (attempted) {
                        set.add(forkChange(son, element));
                    } else {
                        elements.add(element);
                        this.commandTreeNode = son;
                        attempted = true;
                    }
                }
            }

            if (!attempted) {
                failed = true;
            }

            return Collections.unmodifiableSet(set);
        }
    }

    protected CommandTreeFork forkChange(CommandTreeNode son, CommandTreeNode.Element element) {
        final CommandTreeFork fork = new CommandTreeFork(son);

        final List<CommandTreeNode.Element> elements = fork.getElements();
        elements.addAll(this.elements);
        elements.set(elements.size() - 1, element);

        return fork;
    }
    
    protected CommandTreeFork fork(CommandTreeNode son) {
        final CommandTreeFork fork = new CommandTreeFork(son);
    
        final List<CommandTreeNode.Element> elements = fork.getElements();
        elements.addAll(this.elements);
    
        return fork;
    }

    protected CommandTreeFork forkWith(CommandTreeNode son, CommandTreeNode.Element element) {
        final CommandTreeFork fork = new CommandTreeFork(son);

        final List<CommandTreeNode.Element> elements = fork.getElements();
        elements.addAll(this.elements);
        elements.add(element);

        return fork;
    }

    @Override
    protected CommandTreeFork clone() {
        final CommandTreeFork fork = new CommandTreeFork(commandTreeNode);

        final List<CommandTreeNode.Element> elements = fork.getElements();
        elements.addAll(this.elements);

        return fork;
    }
}
