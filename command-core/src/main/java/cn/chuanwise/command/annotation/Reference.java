package cn.chuanwise.command.annotation;

import cn.chuanwise.command.completer.Completer;
import cn.chuanwise.command.parser.Parser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 参数值，用于引用
 *
 * @author Chuanwise
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Reference {
    
    /**
     * 变量名
     *
     * @return 变量名
     */
    String value() default "";
    
    /**
     * 特定的补全器
     *
     * @return 补全器
     */
    Class<? extends Completer> completer() default Completer.class;
    
    /**
     * 变量描述
     *
     * @return 变量描述
     */
    String description() default "";
    
    /**
     * 特定的解析器
     *
     * @return 解析器
     */
    Class<? extends Parser> parser() default Parser.class;
}
