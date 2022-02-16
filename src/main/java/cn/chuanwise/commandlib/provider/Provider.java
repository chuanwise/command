package cn.chuanwise.commandlib.provider;

import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.commandlib.context.ProvideContext;
import cn.chuanwise.function.ExceptionFunction;
import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.Preconditions;

public interface Provider {
    Container<?> provide(ProvideContext context) throws Exception;

    static <U> Provider of(Class<U> providedClass, ExceptionFunction<ProvideContext, Container<U>> function) {
        Preconditions.argumentNonNull(providedClass, "provided class");
        Preconditions.argumentNonNull(function, "function");

        return context -> {
            if (context.getParameter().getType().isAssignableFrom(providedClass)) {
                return function.exceptApply(context);
            } else {
                return Container.empty();
            }
        };
    }
}
