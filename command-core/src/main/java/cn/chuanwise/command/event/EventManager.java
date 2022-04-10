package cn.chuanwise.command.event;

import cn.chuanwise.command.Commander;
import cn.chuanwise.command.Priority;
import cn.chuanwise.command.object.AbstractCommanderObject;
import cn.chuanwise.common.util.Collections;
import cn.chuanwise.common.util.Maps;
import cn.chuanwise.common.util.Preconditions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 事件管理器
 *
 * @author Chuanwise
 */
public class EventManager
    extends AbstractCommanderObject {
    
    protected final Map<Priority, List<EventHandler>> eventHandlers = new HashMap<>();
    
    public EventManager(Commander commander) {
        super(commander);
    }
    
    public void registerEventHandler(EventHandler eventHandler, Priority priority) {
        Preconditions.namedArgumentNonNull(eventHandler, "event handler");
        Preconditions.namedArgumentNonNull(priority, "priority");
    
        Maps.getOrPutGet(eventHandlers, priority, CopyOnWriteArrayList::new).add(eventHandler);
    }
    
    public void broadcastEvent(Object event) {
        Preconditions.namedArgumentNonNull(event, "event");
        
        
    }
    
    private void broadcastEvent0(Priority priority, Object event) throws Exception {
        final List<EventHandler> eventHandlers = this.eventHandlers.get(priority);
        if (Collections.nonEmpty(eventHandlers)) {
            for (EventHandler eventHandler : eventHandlers) {
                eventHandler.handleEvent(event);
            }
        }
    }
}
