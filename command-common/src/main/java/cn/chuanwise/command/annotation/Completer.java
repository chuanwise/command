package cn.chuanwise.command.annotation;

import cn.chuanwise.command.handler.Priority;

import java.lang.annotation.*;

/**
 * 补全控制器，用于进行某一类型的补全
 *
 * @author Chuanwise
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Completer {
    
    /**
     * 支持的补全类型
     *
     * @return 补全类型
     */
    Class<?>[] value();
    
    /**
     * 解析器优先级
     *
     * @return 优先级
     */
    Priority priority() default Priority.NORMAL;
}
