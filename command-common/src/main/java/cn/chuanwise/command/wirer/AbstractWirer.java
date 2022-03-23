package cn.chuanwise.command.wirer;

import cn.chuanwise.command.context.WireContext;
import cn.chuanwise.command.handler.Priority;
import cn.chuanwise.common.space.Container;
import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.Types;
import lombok.Data;

/**
 * 抽象装配器
 *
 * @author Chuanwise
 */
@Data
public abstract class AbstractWirer<T>
    implements Wirer {

    protected final Class<T> wiredClass;
    private Priority priority;
    
    public AbstractWirer(Class<T> wiredClass, Priority priority) {
        Preconditions.namedArgumentNonNull(wiredClass, "wired class");
        Preconditions.namedArgumentNonNull(priority, "priority");

        this.wiredClass = wiredClass;
        this.priority = priority;
    }

    @SuppressWarnings("all")
    public AbstractWirer(Priority priority) {
        Preconditions.namedArgumentNonNull(priority, "priority");
    
        this.priority = priority;
        this.wiredClass = (Class<T>) Types.getTypeParameterClass(getClass(), AbstractWirer.class);
    }

    @Override
    public final Container<T> wire(WireContext context) throws Exception {
        if (context.getParameter().getType().isAssignableFrom(wiredClass)) {
            return wire0(context);
        } else {
            return Container.empty();
        }
    }

    protected abstract Container<T> wire0(WireContext context) throws Exception;
}