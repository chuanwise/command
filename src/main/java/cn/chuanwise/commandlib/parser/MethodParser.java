package cn.chuanwise.commandlib.parser;

import cn.chuanwise.commandlib.context.ParserContext;
import cn.chuanwise.commandlib.context.ProvideContext;
import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.Reflects;
import cn.chuanwise.util.Types;
import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Objects;

@Data
public class MethodParser
        implements Parser {

    protected final Object source;
    protected final Method method;

    protected final Class<?> parsedClass;
    protected final Class<?> contextClass;
    protected final boolean contained;

    public MethodParser(Object source, Method method) {
        Preconditions.argumentNonNull(method);

        final cn.chuanwise.commandlib.annotation.Parser parser = method.getAnnotation(cn.chuanwise.commandlib.annotation.Parser.class);
        Preconditions.argumentNonNull(parser, "方法不具备 @Parser 注解");
        this.parsedClass = parser.value();

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
                "带有 @Parser 注解的方法只能具备一个 " + ParserContext.class.getName() + " 或其子类型的形式参数");
        this.contextClass = parameters[0].getType();
        Preconditions.argument(ParserContext.class.isAssignableFrom(contextClass),
                "带有 @Parser 注解的方法只能具备一个 " + ParserContext.class.getName() + " 或其子类型的形式参数");

        final Class<?> returnType = method.getReturnType();
        if (parsedClass.isAssignableFrom(returnType)) {
            this.contained = false;
        } else {
            Preconditions.argument(Container.class.isAssignableFrom(returnType),
                    "方法带有 @Provider(" + parsedClass.getName() + ") 注解，" +
                            "返回值却不是 Container<" + parsedClass.getName() + "> 类型或 " + parsedClass.getName() + " 类型");

            final Class<?> parameterClass = Types.getTypeParameterClass(method.getGenericReturnType(), Container.class);
            Preconditions.argument(Objects.equals(parameterClass, parsedClass),
                    "方法带有 @Provider(" + parsedClass.getName() + ") 注解，" +
                            "返回值却不是 Container<" + parsedClass.getName() + "> 类型或 " + parsedClass.getName() + " 类型");
            this.contained = true;
        }
    }

    @Override
    @SuppressWarnings("all")
    public Container<?> parse(ParserContext context) throws Exception {
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
