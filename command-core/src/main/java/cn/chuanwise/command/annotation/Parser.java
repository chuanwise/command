package cn.chuanwise.command.annotation;

import cn.chuanwise.command.Priority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 解析器
 *
 * @author Chuanwise
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parser {
    
    /**
     * 解析器优先级
     *
     * @return 优先级
     */
    Priority priority() default Priority.NORMAL;
}
