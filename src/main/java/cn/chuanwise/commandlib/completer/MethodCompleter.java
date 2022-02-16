package cn.chuanwise.commandlib.completer;

import cn.chuanwise.commandlib.command.OptionInfo;
import cn.chuanwise.commandlib.command.ParameterInfo;
import cn.chuanwise.commandlib.configuration.CommandLibConfiguration;
import cn.chuanwise.commandlib.context.CompleteContext;
import cn.chuanwise.commandlib.context.ParserContext;
import cn.chuanwise.commandlib.tree.CommandTree;
import cn.chuanwise.commandlib.tree.DispatchFork;
import cn.chuanwise.commandlib.tree.OptionCommandTree;
import cn.chuanwise.commandlib.tree.ParameterCommandTree;
import cn.chuanwise.commandlib.util.Options;
import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.Reflects;
import cn.chuanwise.util.Types;
import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;

@Data
public class MethodCompleter
        implements Completer {

    protected final Object source;
    protected final Method method;

    protected final Set<Class<?>> completedClasses = new HashSet<>();

    public MethodCompleter(Object source, Method method) {
        Preconditions.argumentNonNull(method);

        final cn.chuanwise.commandlib.annotation.Completer completer = method.getAnnotation(cn.chuanwise.commandlib.annotation.Completer.class);
        Preconditions.argumentNonNull(completer, "方法不具备 @Completer 注解");
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
        final Set<String> set = new HashSet<>();

        final CommandLibConfiguration.Option option = context.getCommandLib().getConfiguration().getOption();
        for (DispatchFork dispatchFork : context.getDispatchForks()) {
            for (CommandTree commandTree : dispatchFork.getCommandTree().getSons()) {
                // 对每一种参数的每一种类型，都 complete 一次
                if (commandTree instanceof ParameterCommandTree) {
                    final ParameterCommandTree tree = (ParameterCommandTree) commandTree;
                    for (ParameterInfo parameterInfo : tree.getParameterInfo()) {
                        for (Class<?> parameterClass : parameterInfo.getParameterClasses()) {
                            for (Class<?> completedClass : completedClasses) {
                                if (completedClass.isAssignableFrom(parameterClass)) {
                                    set.addAll(complete0(context));
                                    break;
                                }
                            }
                        }
                    }
                    continue;
                }

                if (commandTree instanceof OptionCommandTree) {
                    final OptionCommandTree tree = (OptionCommandTree) commandTree;
                    for (OptionInfo optionInfo : tree.getOptionInfo()) {
                        for (Class<?> parameterClass : optionInfo.getParameterClasses()) {
                            for (Class<?> completedClass : completedClasses) {
                                if (completedClass.isAssignableFrom(parameterClass)) {
                                    set.addAll(Options.complete(option, optionInfo, complete0(context)));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        return Collections.unmodifiableSet(set);
    }

    @SuppressWarnings("all")
    private Collection<String> complete0(CompleteContext context) throws Exception {
        final Container<Collection<String>> container = (Container) Reflects.invoke(source, method, context)
                .map(Container::of)
                .orElse(Container.empty());
        if (container.isEmpty()) {
            return Collections.emptySet();
        } else {
            return container.get();
        }
    }
}
