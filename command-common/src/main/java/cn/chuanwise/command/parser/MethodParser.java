package cn.chuanwise.command.parser;

import cn.chuanwise.command.context.ParseContext;
import cn.chuanwise.command.handler.Priority;
import cn.chuanwise.common.space.Container;
import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.Reflections;
import cn.chuanwise.common.util.Types;
import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Objects;

/**
 * 方法解析器
 *
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class MethodParser<T>
    extends AbstractParser<T> {

    protected final Object source;
    protected final Method method;

    protected final boolean contained;

    public MethodParser(Priority priority, Object source, Method method, boolean contained) {
        super(priority);
        
        Preconditions.namedArgumentNonNull(source, "source");
        Preconditions.namedArgumentNonNull(method, "method");
        
        this.source = source;
        this.method = method;
        this.contained = contained;
    }
    
    public static <T> MethodParser of(Object source, Method method) {
        Preconditions.namedArgumentNonNull(method, "method");
    
        final cn.chuanwise.command.annotation.Parser parser = method.getAnnotation(cn.chuanwise.command.annotation.Parser.class);
        Preconditions.namedArgumentNonNull(parser, "方法不具备 @Parser 注解");
    
        final Class<?> declaringClass = method.getDeclaringClass();
        if (Modifier.isStatic(method.getModifiers())) {
            source = declaringClass;
        } else {
            Preconditions.argument(declaringClass.isInstance(source), "method source should be instance of " + declaringClass.getName());
            source = source;
        }
    
        final Parameter[] parameters = method.getParameters();
        Preconditions.argument(parameters.length == 1,
            "带有 @Parser 注解的方法只能具备一个 " + ParseContext.class.getName() + " 或其子类型的形式参数");
        Preconditions.argument(ParseContext.class.isAssignableFrom(parameters[0].getType()),
            "带有 @Parser 注解的方法只能具备一个 " + ParseContext.class.getName() + " 或其子类型的形式参数");
    
        final boolean contained;
        final Class<T> parsedClass;
        final Class<?> returnType = method.getReturnType();
        if (Objects.equals(parser.value(), Object.class)) {
            contained = false;
            parsedClass = (Class<T>) returnType;
        } else {
            parsedClass = (Class<T>) parser.value();
        
            Preconditions.argument(Container.class.isAssignableFrom(returnType),
                "方法带有 @Parser(" + parsedClass.getName() + ") 注解，" +
                    "返回值却不是 Container<" + parsedClass.getName() + "> 类型或 " + parsedClass.getName() + " 类型");
        
            final Class<?> parameterClass = Types.getTypeParameterClass(method.getGenericReturnType(), Container.class);
            Preconditions.argument(Objects.equals(parameterClass, parsedClass),
                "方法带有 @Parser(" + parsedClass.getName() + ") 注解，" +
                    "返回值却不是 Container<" + parsedClass.getName() + "> 类型或 " + parsedClass.getName() + " 类型");
            contained = true;
        }
        
        return new MethodParser(parser.priority(), source, method, contained);
    }
    
    @Override
    @SuppressWarnings("all")
    protected Container<T> parse0(ParseContext context) throws Exception {
        final Object object = Reflections.invokeMethod(source, method, context);
    
        if (contained) {
            return (Container<T>) object;
        }
    
        return (Container<T>) Container.of(object);
    }
}
