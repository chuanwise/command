package cn.chuanwise.command.event;

import cn.chuanwise.common.util.Exceptions;
import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.Reflections;
import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

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
    
    protected MethodEventHandler(Class<T> eventClass, Object source, Method method) {
        super(eventClass);
        
        Preconditions.objectNonNull(source, "source");
        Preconditions.objectNonNull(method, "method");
        
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
        Preconditions.objectNonNull(method, "method");
    
        final cn.chuanwise.command.annotation.EventHandler eventHandler = method.getAnnotation(cn.chuanwise.command.annotation.EventHandler.class);
        Preconditions.objectNonNull(eventHandler, "方法不具备 @EventHandler 注解");
    
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
        
        return new MethodEventHandler<>(eventClass, source, method);
    }
    
    @Override
    protected boolean handleEvent0(Object event) throws Exception {
        try {
            Reflections.invokeMethod(source, method, event);
        } catch (InvocationTargetException e) {
            Exceptions.rethrow(e.getCause());
        }
        return true;
    }
}
