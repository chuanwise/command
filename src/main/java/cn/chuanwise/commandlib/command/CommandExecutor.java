package cn.chuanwise.commandlib.command;

import cn.chuanwise.commandlib.context.CommandContext;

public interface CommandExecutor {
    boolean execute(CommandContext context) throws Exception;
}
