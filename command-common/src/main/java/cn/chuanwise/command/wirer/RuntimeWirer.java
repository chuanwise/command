package cn.chuanwise.command.wirer;

import cn.chuanwise.command.context.WireContext;
import cn.chuanwise.command.handler.Priority;
import cn.chuanwise.common.space.Container;

public class RuntimeWirer<T>
        extends AbstractWirer<T> {

    public RuntimeWirer(Class<T> wiredClass, Priority priority) {
        super(wiredClass, priority);
    }

    @Override
    @SuppressWarnings("all")
    protected Container<T> wire0(WireContext context) throws Exception {
        return (Container<T>) context.getCommander().wire(context);
    }
}
