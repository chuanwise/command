package cn.chuanwise.commandlib.provider;

import cn.chuanwise.commandlib.context.ProvideContext;
import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.Reflects;
import cn.chuanwise.util.Types;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MethodProvider
        implements Provider {

    protected final Object source;
    protected final Method method;

    protected final Class<?> providedClass;
    protected final Class<?> contextClass;
    protected final boolean contained;

    public MethodProvider(Object source, Method method) {
        Preconditions.argumentNonNull(method);

        final cn.chuanwise.commandlib.annotation.Provider provider = method.getAnnotation(cn.chuanwise.commandlib.annotation.Provider.class);
        Preconditions.argumentNonNull(provider, "方法不具备 @Provider 注解");
        this.providedClass = provider.value();

        final Class<?> declaringClass = method.getDeclaringClass();
        if (Modifier.isStatic(method.getModifiers())) {
            this.source = declaringClass;
        } else {
            Preconditions.argument(declaringClass.isInstance(source), "method source should be instance of " + declaringClass.getName());
            this.source = source;
        }
        this.method = method;

        final Parameter[] parameters = method.getParameters();
        Preconditions.argument(parameters.length == 1,
                "带有 @Provider 注解的方法只能具备一个 " + ProvideContext.class.getName() + " 或其子类型的形式参数");
        this.contextClass = parameters[0].getType();
        Preconditions.argument(ProvideContext.class.isAssignableFrom(contextClass),
                "带有 @Provider 注解的方法只能具备一个 " + ProvideContext.class.getName() + " 或其子类型的形式参数");

        final Class<?> returnType = method.getReturnType();
        if (providedClass.isAssignableFrom(returnType)) {
            this.contained = false;
        } else {
            Preconditions.argument(Container.class.isAssignableFrom(returnType),
                    "方法带有 @Provider(" + providedClass.getName() + ") 注解，" +
                            "返回值却不是 Container<" + providedClass.getName() + "> 类型或 "+ providedClass.getName() + " 类型");

            final Class<?> parameterClass = Types.getTypeParameterClass(method.getGenericReturnType(), Container.class);
            Preconditions.argument(Objects.equals(parameterClass, providedClass),
                    "方法带有 @Provider(" + providedClass.getName() + ") 注解，" +
                            "返回值却不是 Container<" + providedClass.getName() + "> 类型或 "+ providedClass.getName() + " 类型");

            this.contained = true;
        }
    }

    @Override
    @SuppressWarnings("all")
    public Container<?> provide(ProvideContext context) throws Exception {
        if (contextClass.isInstance(context)) {
            Container<Object> container = Reflects.invoke(source, method, context)
                    .map(Container::of)
                    .orElse(Container.empty());

            if (contained) {
                container = (Container<Object>) container.get();
            }

            return container;
        } else {
            return Container.empty();
        }
    }
}
