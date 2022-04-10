package cn.chuanwise.command.event;

import cn.chuanwise.command.command.Command;
import cn.chuanwise.command.context.CommandContext;
import cn.chuanwise.command.context.CommanderContext;
import cn.chuanwise.command.context.ReferenceInfo;
import lombok.Data;

import java.util.Map;

@Data
public class CommandExecuteEvent
        extends CommanderContext {

    protected final Object commandSender;
    protected final Command command;
    protected final Map<String, ReferenceInfo> referenceInfo;

    public CommandExecuteEvent(CommandContext commandContext) {
        super(commandContext.getCommander());

        this.commandSender = commandContext.getCommandSender();
        this.command = commandContext.getCommand();
        this.referenceInfo = commandContext.getReferenceInfo();
    }
}
