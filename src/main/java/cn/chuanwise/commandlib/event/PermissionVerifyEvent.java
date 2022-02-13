package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.context.CommandContext;
import lombok.Data;

@Data
public class PermissionVerifyEvent
        extends PermissionEvent {

    protected boolean authorized;

    public PermissionVerifyEvent(CommandContext commandContext, String permission) {
        super(commandContext, permission);
    }
}
