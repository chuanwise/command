package cn.chuanwise.command.annotation;

import cn.chuanwise.command.handler.Priority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 事件捕捉器
 *
 * @author Chuanwise
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {
    
    /**
     * 监听器优先级
     *
     * @return 优先级
     */
    Priority priority() default Priority.NORMAL;
    
    /**
     * 是否总是生效
     *
     * @return 总是生效
     */
    boolean alwaysValid() default false;
}
