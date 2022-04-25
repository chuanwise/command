package cn.chuanwise.command.event;

import cn.chuanwise.command.Priority;
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
    
    public SimpleEventHandler(Class<T> eventClass, ExceptionConsumer<T> action) {
        super(eventClass);
    
        Preconditions.objectNonNull(action, "action");
        
        this.action = action;
    }
    
    public SimpleEventHandler(ExceptionConsumer<T> action) {
        Preconditions.objectNonNull(action, "action");
        
        this.action = action;
    }
    
    @Override
    protected boolean handleEvent0(T t) throws Exception {
        action.exceptAccept(t);
        return true;
    }
}
