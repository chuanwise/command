package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.context.CommandContext;
import lombok.Data;

@Data
public class PermissionEvent
        extends CommandExecuteEvent {

    protected final Object commandSender;
    protected final String permission;

    public PermissionEvent(CommandContext commandContext, String permission) {
        super(commandContext);

        this.commandSender = commandContext.getCommandSender();
        this.permission = permission;
    }
}
