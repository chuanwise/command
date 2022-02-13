package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.context.CommandContext;
import lombok.Data;

@Data
public class PermissionDeniedEvent
        extends PermissionEvent {

    public PermissionDeniedEvent(CommandContext commandContext, String permission) {
        super(commandContext, permission);
    }
}
