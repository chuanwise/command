package cn.chuanwise.command.annotation;

import cn.chuanwise.command.handler.Priority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 异常捕捉器
 *
 * @author Chuanwise
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionHandler {
    
    /**
     * 获取优先级
     *
     * @return 优先级
     */
    Priority priority();
}
