package cn.chuanwise.commandlib.provider;

import cn.chuanwise.commandlib.context.ProvideContext;
import cn.chuanwise.toolkit.container.Container;

public class RuntimeProvider<T>
        extends SimpleProvider<T> {

    public RuntimeProvider(Class<T> providedClass) {
        super(providedClass);
    }

    @Override
    @SuppressWarnings("all")
    protected Container<T> provide0(ProvideContext context) throws Exception {
        return (Container<T>) context.getCommandLib().pipeline().handleProvide(context);
    }
}
