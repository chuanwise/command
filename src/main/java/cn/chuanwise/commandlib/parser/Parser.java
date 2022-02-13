package cn.chuanwise.commandlib.parser;

import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.commandlib.context.ReferenceInfo;
import cn.chuanwise.function.ExceptionBiFunction;
import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.Types;
import lombok.Data;

@Data
public abstract class Parser<T> {

    protected final Class<T> parsedClass;

    public Parser(Class<T> parsedClass) {
        Preconditions.argumentNonNull(parsedClass, "parsed class");

        this.parsedClass = parsedClass;
    }

    @SuppressWarnings("all")
    public Parser() {
        this.parsedClass = (Class<T>) Types.getTypeParameterClass(getClass(), Parser.class);
    }

    public abstract Container<T> parse(CommandContext context, ReferenceInfo referenceInfo) throws Exception;

    public static <U> Parser<U> of(Class<U> parsedClass, ExceptionBiFunction<CommandContext, ReferenceInfo, Container<U>> function) {
        Preconditions.argumentNonNull(parsedClass, "parsed class");
        Preconditions.argumentNonNull(function, "function");

        return new Parser<U>() {
            @Override
            public Container<U> parse(CommandContext context, ReferenceInfo referenceInfo) throws Exception {
                return function.exceptApply(context, referenceInfo);
            }
        };
    }
}