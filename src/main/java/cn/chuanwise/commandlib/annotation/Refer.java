package cn.chuanwise.commandlib.annotation;

import cn.chuanwise.commandlib.completer.Completer;
import cn.chuanwise.commandlib.completer.SimpleCompleter;
import cn.chuanwise.commandlib.parser.Parser;
import cn.chuanwise.commandlib.parser.SimpleParser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Refer {
    String value();

    Class<? extends Completer> completer() default Completer.class;

    Class<? extends Parser> parser() default Parser.class;
}
