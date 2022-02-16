package cn.chuanwise.commandlib.event;

import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.Reflects;
import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Optional;

@Data
public class MethodEventHandler
        implements EventHandler {

    protected final Object source;
    protected final Method method;

    protected final Class<?> eventClass;
    private final boolean defaultHandled;

    public MethodEventHandler(Object source, Method method) {
        Preconditions.argumentNonNull(method);

        final cn.chuanwise.commandlib.annotation.EventHandler eventHandler = method.getAnnotation(cn.chuanwise.commandlib.annotation.EventHandler.class);
        Preconditions.argumentNonNull(eventHandler, "方法不具备 @EventHandler 注解");
        this.defaultHandled = eventHandler.defaultHandled();

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
                "带有 @EventHandler 注解的方法只能具备一个事件类型的形式参数");
        this.eventClass = parameters[0].getType();
    }

    @Override
    public boolean handleEvent(Object event) throws Exception {
        if (eventClass.isInstance(event)) {
            final Optional<Object> optional = Reflects.invoke(source, method, event);
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
