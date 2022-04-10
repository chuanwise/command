package cn.chuanwise.command.event;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * 方法注册事件
 *
 * @author Chuanwise
 */
@Data
public class MethodRegisterEvent
    implements Cancellable {

    protected final Object source;
    protected final Method method;
    
    protected boolean cancelled;
}
