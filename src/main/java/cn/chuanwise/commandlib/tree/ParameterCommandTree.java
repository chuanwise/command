package cn.chuanwise.commandlib.tree;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.command.ParameterInfo;
import cn.chuanwise.commandlib.completer.Completer;
import cn.chuanwise.commandlib.context.CompleteContext;
import cn.chuanwise.commandlib.context.ReferenceInfo;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

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
            for (Completer completer : info.getCompleters()) {
                set.addAll(completer.complete(context));
            }
        }

        return set;
    }
}
