package cn.chuanwise.command.annotation;

import java.lang.annotation.*;

/**
 * 指令格式
 *
 * @author Chuanwise
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    
    /**
     * 指令格式
     *
     * @return 指令格式
     */
    String[] value();
}