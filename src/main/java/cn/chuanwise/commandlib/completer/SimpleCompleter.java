package cn.chuanwise.commandlib.completer;

import cn.chuanwise.commandlib.command.OptionInfo;
import cn.chuanwise.commandlib.command.ParameterInfo;
import cn.chuanwise.commandlib.configuration.CommandLibConfiguration;
import cn.chuanwise.commandlib.context.CompleteContext;
import cn.chuanwise.commandlib.tree.CommandTree;
import cn.chuanwise.commandlib.tree.DispatchFork;
import cn.chuanwise.commandlib.tree.OptionCommandTree;
import cn.chuanwise.commandlib.tree.ParameterCommandTree;
import cn.chuanwise.commandlib.util.Options;
import cn.chuanwise.function.ExceptionFunction;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public abstract class SimpleCompleter
        implements Completer {

    protected final Class<?> completedClass;

    public SimpleCompleter(Class<?> completedClass) {
        Preconditions.argumentNonNull(completedClass, "completed class");

        this.completedClass = completedClass;
    }

    @Override
    public final Set<String> complete(CompleteContext context) throws Exception {
        final List<DispatchFork> dispatchForks = context.getDispatchForks();
        if (dispatchForks.isEmpty()) {
            return Collections.emptySet();
        }

        final CommandLibConfiguration.Option option = context.getCommandLib().getConfiguration().getOption();
        final Set<String> set = new HashSet<>();
        for (DispatchFork dispatchFork : dispatchForks) {
            for (CommandTree commandTree : dispatchFork.getCommandTree().getSons()) {
                // 对每一种参数的每一种类型，都 complete 一次
                if (commandTree instanceof ParameterCommandTree) {
                    final ParameterCommandTree tree = (ParameterCommandTree) commandTree;
                    for (ParameterInfo parameterInfo : tree.getParameterInfo()) {
                        for (Class<?> parameterClass : parameterInfo.getParameterClasses()) {
                            if (parameterClass.isAssignableFrom(completedClass)) {
                                set.addAll(complete0(context));
                            }
                        }
                    }
                    continue;
                }

                if (commandTree instanceof OptionCommandTree) {
                    final OptionCommandTree tree = (OptionCommandTree) commandTree;
                    for (OptionInfo optionInfo : tree.getOptionInfo()) {
                        for (Class<?> parameterClass : optionInfo.getParameterClasses()) {
                            if (parameterClass.isAssignableFrom(completedClass)) {
                                set.addAll(Options.complete(option, optionInfo, complete0(context)));
                                break;
                            }
                        }
                    }
                }
            }
        }

        return set;
    }

    protected abstract Set<String> complete0(CompleteContext context) throws Exception;
}
