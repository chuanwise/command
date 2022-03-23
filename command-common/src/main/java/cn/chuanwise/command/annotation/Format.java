package cn.chuanwise.command.annotation;

import java.lang.annotation.*;

/**
 * 指令格式
 *
 * @author Chuanwise
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Formats.class)
public @interface Format {
    
    /**
     * 指令格式
     *
     * @return 指令格式
     */
    String value();
}