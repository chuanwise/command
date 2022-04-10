package cn.chuanwise.command.context;

import cn.chuanwise.command.api.CommandSenderHolder;
import cn.chuanwise.command.command.Command;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

import java.util.Map;

/**
 * 指令上下文
 *
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class CommandContext
        extends CommanderContext
        implements CommandSenderHolder {

    protected final Object commandSender;
    protected final Command command;
    protected final Map<String, ReferenceInfo> referenceInfo;

    public CommandContext(Object commandSender, Map<String, ReferenceInfo> referenceInfo, Command command) {
        super(command.getCommander());

        Preconditions.namedArgumentNonNull(referenceInfo, "reference info");

        // command sender is nullable
        this.commandSender = commandSender;
        this.referenceInfo = referenceInfo;
        this.command = command;
    }
}
