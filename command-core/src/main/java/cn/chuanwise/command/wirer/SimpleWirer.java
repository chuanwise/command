package cn.chuanwise.command.wirer;

import cn.chuanwise.command.context.WireContext;
import cn.chuanwise.command.Priority;
import cn.chuanwise.common.api.ExceptionFunction;
import cn.chuanwise.common.space.Container;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

/**
 * 简单装配器
 *
 * @param <T> 装配器类型
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class SimpleWirer<T>
    extends AbstractWirer<T> {
    
    private ExceptionFunction<WireContext, Container<T>> action;
    
    public SimpleWirer(Class<T> wiredClass, ExceptionFunction<WireContext, Container<T>> action) {
        super(wiredClass);
    
        Preconditions.namedArgumentNonNull(action, "action");
        
        this.action = action;
    }
    
    public SimpleWirer(ExceptionFunction<WireContext, Container<T>> action) {
        Preconditions.namedArgumentNonNull(action, "action");
        
        this.action = action;
    }
    
    @Override
    protected Container<T> wire0(WireContext context) throws Exception {
        return action.exceptApply(context);
    }
}
