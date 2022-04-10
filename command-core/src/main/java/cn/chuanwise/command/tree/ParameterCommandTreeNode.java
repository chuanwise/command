package cn.chuanwise.command.tree;

import cn.chuanwise.command.Commander;
import cn.chuanwise.command.command.ParameterInfo;
import cn.chuanwise.command.completer.Completer;
import cn.chuanwise.command.configuration.CommanderConfiguration;
import cn.chuanwise.command.context.CompleteContext;
import cn.chuanwise.command.util.Arguments;
import cn.chuanwise.common.util.Strings;
import lombok.Data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public abstract class ParameterCommandTreeNode
        extends CommandTreeNode {

    protected final Set<ParameterInfo> parameterInfo = new HashSet<>();

    public ParameterCommandTreeNode(Commander commander) {
        super(commander);
    }

    @Override
    protected Optional<Element> accept(String argument) throws Exception {
        return Optional.of(new ValueElement(argument));
    }

    @Override
    protected Optional<Element> attempt(String argument) throws Exception {
        return Optional.of(new PlainTextElement(argument));
    }

    @Override
    public Set<String> complete(CompleteContext context) throws Exception {
        final Set<String> set = new HashSet<>(commander.getCompleteService().complete(context));
        final CommanderConfiguration configuration = commander.getCommanderConfiguration();

        for (ParameterInfo info : parameterInfo) {
            for (Completer completer : info.getSpecialCompleters()) {
                set.addAll(completer.complete(context));
            }

            final Set<String> descriptions = info.getDescriptions();
            if (cn.chuanwise.common.util.Collections.nonEmpty(descriptions) && configuration.isAddReferenceDescriptionsToCompleterElements()) {
                set.addAll(descriptions);
            }

            final String defaultValue = info.getDefaultValue();
            if (Strings.nonEmpty(defaultValue)) {
                set.add(defaultValue);
            }
        }

        if (configuration.isSerializeCompleterElements()) {
            return Collections.unmodifiableSet(set.stream()
                    .map(Arguments::serialize)
                    .collect(Collectors.toSet()));
        } else {
            return Collections.unmodifiableSet(set);
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
