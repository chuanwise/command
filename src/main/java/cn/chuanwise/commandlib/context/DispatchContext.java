package cn.chuanwise.commandlib.context;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

import java.util.List;

@Data
public class DispatchContext extends CommandLibContext {

    protected final Object commandSender;
    protected final List<String> arguments;

    public DispatchContext(CommandLib commandLib, Object commandSender, List<String> arguments) {
        super(commandLib);

        Preconditions.argumentNonNull(arguments, "arguments");

        this.commandSender = commandSender;
        this.arguments = arguments;
    }
}
