package cn.chuanwise.commandlib.context;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.command.Command;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

import java.util.Map;

@Data
public class CommandContext
        extends CommandLibContext {

    protected final Object commandSender;
    protected final Command command;
    protected final Map<String, ReferenceInfo> referenceInfo;

    public CommandContext(Object commandSender, Map<String, ReferenceInfo> referenceInfo, Command command) {
        super(command.getCommandLib());

        Preconditions.argumentNonNull(referenceInfo, "reference info");

        // command sender is nullable
        this.commandSender = commandSender;
        this.referenceInfo = referenceInfo;
        this.command = command;
    }
}
