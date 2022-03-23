package cn.chuanwise.command.event;

import cn.chuanwise.command.handler.AbstractEventHandler;
import cn.chuanwise.command.handler.Priority;
import cn.chuanwise.common.api.ExceptionConsumer;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

/**
 * 简单事件处理器
 *
 * @param <T> 事件类型
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class SimpleEventHandler<T>
    extends AbstractEventHandler<T> {
    
    private ExceptionConsumer<T> action;
    
    public SimpleEventHandler(Class<T> eventClass, Priority priority, boolean alwaysValid, ExceptionConsumer<T> action) {
        super(eventClass, priority, alwaysValid);
    
        Preconditions.namedArgumentNonNull(action, "action");
        
        this.action = action;
    }
    
    public SimpleEventHandler(Priority priority, boolean alwaysValid, ExceptionConsumer<T> action) {
        super(priority, alwaysValid);
        
        Preconditions.namedArgumentNonNull(action, "action");
        
        this.action = action;
    }
    
    @Override
    protected boolean handleEvent0(T t) throws Exception {
        action.exceptAccept(t);
        return true;
    }
}
