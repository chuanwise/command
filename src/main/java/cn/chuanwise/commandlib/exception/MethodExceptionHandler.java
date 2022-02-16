package cn.chuanwise.commandlib.exception;

import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.Reflects;
import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Optional;

@Data
public class MethodExceptionHandler
        implements ExceptionHandler {

    protected final Object source;
    protected final Method method;

    protected final Class<? extends Throwable> exceptionClass;
    private final boolean defaultHandled;

    @SuppressWarnings("all")
    public MethodExceptionHandler(Object source, Method method) {
        Preconditions.argumentNonNull(method);

        final cn.chuanwise.commandlib.annotation.ExceptionHandler exceptionHandler = method.getAnnotation(cn.chuanwise.commandlib.annotation.ExceptionHandler.class);
        Preconditions.argumentNonNull(exceptionHandler, "方法不具备 @EventHandler 注解");
        this.defaultHandled = exceptionHandler.defaultHandled();

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
                "带有 @ExceptionHandler 注解的方法只能具备一个异常类型的形式参数");
        final Class<?> parameterClass = parameters[0].getType();

        Preconditions.argument(Throwable.class.isAssignableFrom(parameterClass),
                "带有 @ExceptionHandler 注解的方法只能具备一个异常类型的形式参数");
        this.exceptionClass = (Class<? extends Throwable>) parameterClass;
    }

    @Override
    public boolean handleException(Throwable cause) throws Throwable {
        if (exceptionClass.isInstance(cause)) {
            final Optional<Object> optional = Reflects.invoke(source, method, cause);
            if (optional.isPresent()) {
                final Object returnValue = optional.get();
                if (returnValue instanceof Boolean) {
                    return (Boolean) returnValue;
                } else {
                    return defaultHandled;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
