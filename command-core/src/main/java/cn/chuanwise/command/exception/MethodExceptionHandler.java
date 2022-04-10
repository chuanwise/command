package cn.chuanwise.command.exception;

import cn.chuanwise.command.Priority;
import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.Reflections;
import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

/**
 * 方法异常处理器
 *
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class MethodExceptionHandler<T extends Throwable>
    extends AbstractExceptionHandler<T> {

    protected final Object source;
    protected final Method method;
    
    protected MethodExceptionHandler(Class<T> exceptionClass, Object source, Method method) {
        super(exceptionClass);
    
        Preconditions.namedArgumentNonNull(source, "source");
        Preconditions.namedArgumentNonNull(method, "method");
        
        this.source = source;
        this.method = method;
    }
    
    @SuppressWarnings("all")
    public static MethodExceptionHandler of(Object source, Method method) {
        Preconditions.namedArgumentNonNull(method, "method");
        
        final cn.chuanwise.command.annotation.ExceptionHandler exceptionHandler = method.getAnnotation(cn.chuanwise.command.annotation.ExceptionHandler.class);
        Preconditions.namedArgumentNonNull(exceptionHandler, "方法不具备 @EventHandler 注解");

        final Class<?> declaringClass = method.getDeclaringClass();
        if (Modifier.isStatic(method.getModifiers())) {
            source = declaringClass;
        } else {
            Preconditions.argument(declaringClass.isInstance(source), "method source should be instance of " + declaringClass.getName());
            source = source;
        }

        final Parameter[] parameters = method.getParameters();
        Preconditions.argument(parameters.length == 1,
                "带有 @ExceptionHandler 注解的方法只能具备一个异常类型的形式参数");
        final Class<?> exceptionClass = parameters[0].getType();

        Preconditions.argument(Throwable.class.isAssignableFrom(exceptionClass),
                "带有 @ExceptionHandler 注解的方法只能具备一个异常类型的形式参数");
        
        return new MethodExceptionHandler(exceptionClass, source, method);
    }

    @Override
    protected boolean handleException0(T cause) throws Exception {
        final Object returnValue = Reflections.invokeMethod(source, method, cause);
        if (returnValue instanceof Boolean) {
            return (Boolean) returnValue;
        } else {
            return true;
        }
    }
}
