package cn.chuanwise.command.event;

import cn.chuanwise.command.handler.AbstractEventHandler;
import cn.chuanwise.command.handler.Priority;
import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.Reflections;
import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Optional;

/**
 * 方法事件监听器
 *
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class MethodEventHandler<T>
    extends AbstractEventHandler<T>
        implements EventHandler {

    protected final Object source;
    protected final Method method;
    
    protected MethodEventHandler(Class<T> eventClass, Priority priority, boolean alwaysValid, Object source, Method method) {
        super(eventClass, priority, alwaysValid);
        
        Preconditions.namedArgumentNonNull(source, "source");
        Preconditions.namedArgumentNonNull(method, "method");
        
        this.source = source;
        this.method = method;
    }
    
    /**
     * 构造一个方法事件监听器
     *
     * @param source 方法调用者
     * @param method 方法
     * @return 事件监听器
     */
    public static MethodEventHandler<?> of(Object source, Method method) {
        Preconditions.namedArgumentNonNull(method, "method");
    
        final cn.chuanwise.command.annotation.EventHandler eventHandler = method.getAnnotation(cn.chuanwise.command.annotation.EventHandler.class);
        Preconditions.namedArgumentNonNull(eventHandler, "方法不具备 @EventHandler 注解");
    
        final Class<?> declaringClass = method.getDeclaringClass();
        if (Modifier.isStatic(method.getModifiers())) {
            source = declaringClass;
        } else {
            Preconditions.argument(declaringClass.isInstance(source), "method source should be instance of " + declaringClass.getName());
            source = source;
        }
    
        final Parameter[] parameters = method.getParameters();
        Preconditions.argument(parameters.length == 1,
            "带有 @EventHandler 注解的方法只能具备一个事件类型的形式参数");
        final Class<?> eventClass = parameters[0].getType();
        
        return new MethodEventHandler<>(eventClass, eventHandler.priority(), eventHandler.alwaysValid(), source, method);
    }
    
    @Override
    protected boolean handleEvent0(Object event) throws Exception {
        Reflections.invokeMethod(source, method, event);
        return true;
    }
}
