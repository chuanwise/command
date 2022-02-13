package cn.chuanwise.commandlib.provider;

import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.function.ExceptionFunction;
import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.Types;
import lombok.Data;

@Data
public abstract class Provider<T> {

    protected final Class<T> providedClass;

    public Provider(Class<T> providedClass) {
        Preconditions.argumentNonNull(providedClass, "provided class");

        this.providedClass = providedClass;
    }

    @SuppressWarnings("all")
    public Provider() {
        this.providedClass = (Class<T>) Types.getTypeParameterClass(getClass(), Provider.class);
    }

    public static <U> Provider<U> of(Class<U> providedClass, ExceptionFunction<CommandContext, Container<U>> function) {
        Preconditions.argumentNonNull(providedClass, "provided class");
        Preconditions.argumentNonNull(function, "function");

        return new Provider<U>(providedClass) {
            @Override
            public Container<U> provide(CommandContext context) throws Exception {
                return function.exceptApply(context);
            }
        };
    }

    public abstract Container<T> provide(CommandContext context) throws Exception;
}