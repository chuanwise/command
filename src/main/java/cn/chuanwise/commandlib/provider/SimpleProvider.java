package cn.chuanwise.commandlib.provider;

import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.commandlib.context.ProvideContext;
import cn.chuanwise.function.ExceptionFunction;
import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.Types;
import lombok.Data;

@Data
public abstract class SimpleProvider<T>
        implements Provider {

    protected final Class<T> providedClass;

    public SimpleProvider(Class<T> providedClass) {
        Preconditions.argumentNonNull(providedClass, "provided class");

        this.providedClass = providedClass;
    }

    @SuppressWarnings("all")
    public SimpleProvider() {
        this.providedClass = (Class<T>) Types.getTypeParameterClass(getClass(), SimpleProvider.class);
    }

    @Override
    public final Container<T> provide(ProvideContext context) throws Exception {
        if (context.getParameter().getType().isAssignableFrom(providedClass)) {
            return provide0(context);
        } else {
            return Container.empty();
        }
    }

    protected abstract Container<T> provide0(ProvideContext context) throws Exception;
}