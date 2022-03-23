package cn.chuanwise.command.completer;

import cn.chuanwise.command.annotation.Completer;
import cn.chuanwise.command.command.OptionInfo;
import cn.chuanwise.command.command.ParameterInfo;
import cn.chuanwise.command.context.CompleteContext;
import cn.chuanwise.command.handler.Priority;
import cn.chuanwise.command.tree.CommandTreeNode;
import cn.chuanwise.command.tree.OptionCommandTreeNode;
import cn.chuanwise.command.tree.ParameterCommandTreeNode;
import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.Reflections;
import cn.chuanwise.common.util.Types;
import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * 方法补全器
 *
 * @author Chuanwise
 */
@Data
public class MethodCompleter
    implements cn.chuanwise.command.completer.Completer {

    protected final Object source;
    
    protected final Method method;

    protected final Set<Class<?>> completedClasses = new HashSet<>();
    private final Priority priority;
    
    public MethodCompleter(Priority priority, Object source, Method method) {
        Preconditions.namedArgumentNonNull(priority, "priority");
        Preconditions.namedArgumentNonNull(method, "method");
        
        this.priority = priority;
        
        final Completer completer = method.getAnnotation(Completer.class);
        Preconditions.namedArgumentNonNull(completer, "方法不具备 @Completer 注解");
        final Class<?>[] completedClasses = completer.value();

        Preconditions.argument(completedClasses.length != 0, "方法具备 @Completer 注解，但是没有指定任何补全类型");
        if (completedClasses.length == 1) {
            this.completedClasses.add(completedClasses[0]);
        } else {
            for (Class<?> completedClass: completedClasses) {
                // 检查是否已经有它的子类了
                for (Class<?> aClass : this.completedClasses) {
                    if (aClass.isAssignableFrom(completedClass)) {
                        if (Objects.equals(aClass, completedClass)) {
                            throw new IllegalArgumentException("方法具备多个 @Completer(" + aClass.getName() + ") 的重复注解，应该仅留一个");
                        } else {
                            throw new IllegalArgumentException("方法有 @Completer(" + aClass.getName() + ") 及 " +
                                    "@Completer(" + completedClass.getName() + ") 这两个具备继承派生关系的两种类型的补全注解，应该仅留一个");
                        }
                    }
                }

                this.completedClasses.add(completedClass);
            }
        }

        final Class<?> declaringClass = method.getDeclaringClass();
        if (Modifier.isStatic(method.getModifiers())) {
            this.source = declaringClass;
        } else {
            Preconditions.argument(declaringClass.isInstance(source), "method source should be instance of " + declaringClass.getName());
            this.source = source;
        }
        this.method = method;

        final Parameter[] parameters = method.getParameters();
        Preconditions.argument(parameters.length == 1 && Objects.equals(CompleteContext.class, parameters[0].getType()),
                "带有一个或多个 @Completer(...) 注解的方法只能具备一个 " + CompleteContext.class.getName() + " 类型的形式参数");

        final Class<?> parameterClass = Types.getTypeParameterClass(method.getGenericReturnType(), Collection.class);
        Preconditions.argument(Objects.equals(parameterClass, String.class),
                "带有一个或多个 @Completer(...) 注解的方法的返回值只能是 Collection<String> 及其子类");
    }

    @Override
    @SuppressWarnings("all")
    public Set<String> complete(CompleteContext context) throws Exception {
        final CommandTreeNode commandTreeNode = context.getCommandTreeFork().getCommandTreeNode();

        // 对每一种参数的每一种类型，都 complete 一次
        if (commandTreeNode instanceof ParameterCommandTreeNode) {
            final Set<String> set = new HashSet<>();
            final ParameterCommandTreeNode tree = (ParameterCommandTreeNode) commandTreeNode;
            for (ParameterInfo parameterInfo : tree.getParameterInfo()) {
                for (Class<?> parameterClass : parameterInfo.getRequiredClass()) {
                    for (Class<?> completedClass : completedClasses) {
                        if (completedClass.isAssignableFrom(parameterClass)) {
                            set.addAll(complete0(context));
                            break;
                        }
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
                    for (Class<?> completedClass : completedClasses) {
                        if (completedClass.isAssignableFrom(parameterClass)) {
                            set.addAll(complete0(context));
                            break;
                        }
                    }
                }
            }
            return Collections.unmodifiableSet(set);
        }

        return Collections.emptySet();
    }

    @SuppressWarnings("all")
    private Collection<String> complete0(CompleteContext context) throws Exception {
        final Object result = Reflections.invokeMethod(source, method, context);
        return (Collection<String>) result;
    }
}
