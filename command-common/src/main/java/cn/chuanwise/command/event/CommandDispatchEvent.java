package cn.chuanwise.command.event;

import cn.chuanwise.command.context.CommanderContext;
import cn.chuanwise.command.context.DispatchContext;
import lombok.Data;

import java.util.List;

/**
 * 和指令调度相关的事件
 *
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class CommandDispatchEvent
        extends CommanderContext {

    protected final Object commandSender;
    protected final List<String> arguments;

    public CommandDispatchEvent(DispatchContext dispatchContext) {
        super(dispatchContext.getCommander());

        this.commandSender = dispatchContext.getCommandSender();
        this.arguments = dispatchContext.getArguments();
    }
}
