package cn.chuanwise.command.completer;

import cn.chuanwise.command.command.OptionInfo;
import cn.chuanwise.command.command.ParameterInfo;
import cn.chuanwise.command.context.CompleteContext;
import cn.chuanwise.command.Priority;
import cn.chuanwise.command.tree.CommandTreeNode;
import cn.chuanwise.command.tree.OptionCommandTreeNode;
import cn.chuanwise.command.tree.ParameterCommandTreeNode;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 针对某种类型的补全器
 *
 * @author Chuanwise
 */
@Data
public abstract class AbstractCompleter
    implements Completer {

    protected final Class<?> completedClass;
    
    public AbstractCompleter(Class<?> completedClass) {
        Preconditions.namedArgumentNonNull(completedClass, "completed class");

        this.completedClass = completedClass;
    }

    @Override
    public final Set<String> complete(CompleteContext context) throws Exception {
        final CommandTreeNode commandTreeNode = context.getCommandTreeFork().getCommandTreeNode();

        // 对每一种参数的每一种类型，都 complete 一次
        if (commandTreeNode instanceof ParameterCommandTreeNode) {
            final Set<String> set = new HashSet<>();
            final ParameterCommandTreeNode tree = (ParameterCommandTreeNode) commandTreeNode;
            for (ParameterInfo parameterInfo : tree.getParameterInfo()) {
                for (Class<?> parameterClass : parameterInfo.getRequiredClass()) {
                    if (parameterClass.isAssignableFrom(completedClass)) {
                        set.addAll(complete0(context));
                    }
                }
            }
            return Collections.unmodifiableSet(set);
        }

        if (commandTreeNode instanceof OptionCommandTreeNode) {
            final Set<String> set = new HashSet<>();
            final OptionCommandTreeNode tree = (OptionCommandTreeNode) commandTreeNode;
            for (OptionInfo optionInfo : tree.getOptionInfo()) {
                for (Class<?> parameterClass : optionInfo.getRequiredClass()) {
                    if (parameterClass.isAssignableFrom(completedClass)) {
                        set.addAll(complete0(context));
                        break;
                    }
                }
            }
            return Collections.unmodifiableSet(set);
        }

        return Collections.emptySet();
    }

    protected abstract Set<String> complete0(CompleteContext context) throws Exception;
}
