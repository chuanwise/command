package cn.chuanwise.commandlib.parser;

import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.commandlib.context.ParserContext;
import cn.chuanwise.commandlib.context.ReferenceInfo;
import cn.chuanwise.function.ExceptionBiFunction;
import cn.chuanwise.function.ExceptionFunction;
import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.Types;
import lombok.Data;

@Data
public abstract class SimpleParser<T>
        implements Parser {

    protected final Class<T> parsedClass;

    public SimpleParser(Class<T> parsedClass) {
        Preconditions.argumentNonNull(parsedClass, "parsed class");

        this.parsedClass = parsedClass;
    }

    @SuppressWarnings("all")
    public SimpleParser() {
        this.parsedClass = (Class<T>) Types.getTypeParameterClass(getClass(), SimpleParser.class);
    }

    public static <U> SimpleParser<U> of(Class<U> parsedClass, ExceptionFunction<ParserContext, Container<U>> function) {
        Preconditions.argumentNonNull(parsedClass, "parsed class");
        Preconditions.argumentNonNull(function, "function");

        return new SimpleParser<U>() {
            @Override
            public Container<?> parse(ParserContext context) throws Exception {
                return function.exceptApply(context);
            }
        };
    }
}