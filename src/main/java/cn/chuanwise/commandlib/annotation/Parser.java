package cn.chuanwise.commandlib.annotation;

import cn.chuanwise.toolkit.container.Container;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parser {

    /** 指令类注解 */
    Class<?> value() default Object.class;
}
