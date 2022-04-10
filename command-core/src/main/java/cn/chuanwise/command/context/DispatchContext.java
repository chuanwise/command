package cn.chuanwise.command.context;

import cn.chuanwise.command.Commander;
import cn.chuanwise.command.api.CommandSenderHolder;
import cn.chuanwise.command.util.Arguments;
import lombok.Data;

import java.util.List;

/**
 * 调度上下文
 *
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class DispatchContext
        extends CommanderContext
        implements CommandSenderHolder {

    protected final Object commandSender;
    protected final List<String> arguments;

    public DispatchContext(Commander commander, Object commandSender, List<String> arguments) {
        super(commander);

        this.commandSender = commandSender;
        this.arguments = arguments;
    }

    public DispatchContext(Commander commander, Object commandSender, String line) {
        this(commander, commandSender, Arguments.split(line));
    }

    public DispatchContext(Commander commander, List<String> arguments) {
        this(commander, null, arguments);
    }

    public DispatchContext(Commander commander, String line) {
        this(commander, null, Arguments.split(line));
    }
}
