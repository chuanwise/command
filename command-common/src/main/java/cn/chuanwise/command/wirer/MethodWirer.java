package cn.chuanwise.command.wirer;

import cn.chuanwise.command.annotation.Wirer;
import cn.chuanwise.command.context.WireContext;
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
 * 方法填充器
 *
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class MethodWirer<T>
    extends AbstractWirer<T> {

    protected final Object source;
    protected final Method method;

    protected final boolean contained;

    public MethodWirer(Priority priority, Object source, Method method, boolean contained) {
        super(priority);
        
        Preconditions.namedArgumentNonNull(source, "source");
        Preconditions.namedArgumentNonNull(method, "method");
        
        this.method = method;
        this.source = source;
        this.contained = contained;
    }
    
    public static <T> MethodWirer of(Object source, Method method) {
        Preconditions.namedArgumentNonNull(method, "method");
    
        final Wirer wirer = method.getAnnotation(Wirer.class);
        Preconditions.namedArgumentNonNull(wirer, "方法不具备 @Wirer 注解");
        final Class<T> wiredClass = (Class<T>) wirer.value();
    
        final Class<?> declaringClass = method.getDeclaringClass();
        if (Modifier.isStatic(method.getModifiers())) {
            source = declaringClass;
        } else {
            Preconditions.argument(declaringClass.isInstance(source), "method source should be instance of " + declaringClass.getName());
            source = source;
        }
    
        final Parameter[] parameters = method.getParameters();
        Preconditions.argument(parameters.length == 1,
            "带有 @Wirer 注解的方法只能具备一个 " + WireContext.class.getName() + " 或其子类型的形式参数");
        Preconditions.argument(WireContext.class.isAssignableFrom(parameters[0].getType()),
            "带有 @Wirer 注解的方法只能具备一个 " + WireContext.class.getName() + " 或其子类型的形式参数");
    
        final Class<?> returnType = method.getReturnType();
        
        final boolean contained;
        if (wiredClass.isAssignableFrom(returnType)) {
            contained = false;
        } else {
            Preconditions.argument(Container.class.isAssignableFrom(returnType),
                "方法带有 @Wirer(" + wiredClass.getName() + ") 注解，" +
                    "返回值却不是 Container<" + wiredClass.getName() + "> 类型或 "+ wiredClass.getName() + " 类型");
        
            final Class<?> parameterClass = Types.getTypeParameterClass(method.getGenericReturnType(), Container.class);
            Preconditions.argument(Objects.equals(parameterClass, wiredClass),
                "方法带有 @Wirer(" + wiredClass.getName() + ") 注解，" +
                    "返回值却不是 Container<" + wiredClass.getName() + "> 类型或 "+ wiredClass.getName() + " 类型");
        
            contained = true;
        }
        
        return new MethodWirer(wirer.priority(), source, method, contained);
    }

    @Override
    @SuppressWarnings("all")
    protected Container<T> wire0(WireContext context) throws Exception {
        if (context.getParameter().getType().isAssignableFrom(wiredClass)) {
            final Object value = Reflections.invokeMethod(source, method, context);
    
            if (contained) {
                return (Container<T>) value;
            }

            return (Container<T>) Container.of(value);
        } else {
            return Container.empty();
        }
    }
}
