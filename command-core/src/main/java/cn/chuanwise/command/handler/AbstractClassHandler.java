package cn.chuanwise.command.handler;

import cn.chuanwise.command.command.OptionInfo;
import cn.chuanwise.command.command.ParameterInfo;
import cn.chuanwise.command.completer.Completer;
import cn.chuanwise.command.context.CompleteContext;
import cn.chuanwise.command.context.ParseContext;
import cn.chuanwise.command.parser.Parser;
import cn.chuanwise.command.tree.CommandTreeNode;
import cn.chuanwise.command.tree.OptionCommandTreeNode;
import cn.chuanwise.command.tree.ParameterCommandTreeNode;
import cn.chuanwise.common.space.Container;
import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.Types;
import lombok.Data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 对某一类型的补全器和解析器
 *
 * @author Chuanwise
 */
@Data
public abstract class AbstractClassHandler<T>
    implements Completer, Parser {

    protected final Class<T> handledClass;
    
    public AbstractClassHandler(Class<T> handledClass) {
        Preconditions.namedArgumentNonNull(handledClass, "handled class");

        this.handledClass = handledClass;
    }

    @SuppressWarnings("all")
    public AbstractClassHandler() {
        this.handledClass = (Class<T>) Types.getTypeParameterClass(getClass(), AbstractClassHandler.class);
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
                    if (parameterClass.isAssignableFrom(handledClass)) {
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
                    if (parameterClass.isAssignableFrom(handledClass)) {
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

    @Override
    public final Container<?> parse(ParseContext context) throws Exception {
        if (context.getRequiredClass().isAssignableFrom(handledClass)) {
            return parse0(context);
        }
        return Container.empty();
    }

    protected abstract Container<T> parse0(ParseContext context) throws Exception;
}
