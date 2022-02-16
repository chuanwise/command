package cn.chuanwise.commandlib.tree;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.command.ParameterInfo;
import cn.chuanwise.commandlib.completer.Completer;
import cn.chuanwise.commandlib.completer.SimpleCompleter;
import cn.chuanwise.commandlib.context.CompleteContext;
import cn.chuanwise.commandlib.util.Arguments;
import lombok.Data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Data
public abstract class ParameterCommandTree
        extends CommandTree {

    protected final Set<ParameterInfo> parameterInfo = new HashSet<>();

    public ParameterCommandTree(CommandLib commandLib) {
        super(commandLib);
    }

    @Override
    protected Optional<Element> accept(String argument) throws Exception {
        return Optional.of(new ValueElement(argument));
    }

    @Override
    public Set<String> complete(CompleteContext context) throws Exception {
        final Set<String> set = new HashSet<>(super.complete(context));

        for (ParameterInfo info : parameterInfo) {
            for (Completer completer : info.getSpecialCompleters()) {
                for (String string : completer.complete(context)) {
                    set.add(Arguments.serialize(string));
                }
            }
        }

        return Collections.unmodifiableSet(set);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
