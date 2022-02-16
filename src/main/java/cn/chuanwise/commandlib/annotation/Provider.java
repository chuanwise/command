package cn.chuanwise.commandlib.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Provider {

    Class<?> value() default Object.class;
}
