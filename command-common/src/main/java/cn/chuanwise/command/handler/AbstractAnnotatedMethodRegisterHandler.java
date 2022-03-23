package cn.chuanwise.command.handler;

import cn.chuanwise.command.event.MethodRegisterEvent;
import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.Types;
import lombok.Data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public abstract class AbstractAnnotatedMethodRegisterHandler<T extends Annotation>
    extends AbstractEventHandler<MethodRegisterEvent> {

    protected final Class<T> annotationClass;
    
    public AbstractAnnotatedMethodRegisterHandler(Class<T> annotationClass, Priority priority, boolean alwaysValid) {
        super(priority, alwaysValid);
        
        Preconditions.namedArgumentNonNull(annotationClass, "annotation class");
    
        this.annotationClass = annotationClass;
    }
    
    @SuppressWarnings("all")
    public AbstractAnnotatedMethodRegisterHandler(Priority priority, boolean alwaysValid) {
        super(priority, alwaysValid);
        
        this.annotationClass = (Class<T>) Types.getTypeParameterClass(getClass(), AbstractAnnotatedMethodRegisterHandler.class);
    }
    
    @Override
    protected boolean handleEvent0(MethodRegisterEvent methodRegisterEvent) throws Exception {
        final Method method = methodRegisterEvent.getMethod();
    
        final T annotation = method.getAnnotation(annotationClass);
        if (Objects.isNull(annotation)) {
            return false;
        }
    
        registerMethod(methodRegisterEvent.getSource(), methodRegisterEvent.getMethod(), annotation);
        return true;
    }
    
    protected abstract void registerMethod(Object source, Method method, T annotation) throws Exception;
}
