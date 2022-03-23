package cn.chuanwise.command.annotation;

import cn.chuanwise.command.handler.Priority;

import java.lang.annotation.*;

/**
 * 针对某一类型的装配器
 *
 * @author Chuanwise
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Wirer {
    
    /**
     * 装配类型
     *
     * @return 装配类型
     */
    Class<?> value();
    
    /**
     * 解析器优先级
     *
     * @return 优先级
     */
    Priority priority() default Priority.NORMAL;
}
