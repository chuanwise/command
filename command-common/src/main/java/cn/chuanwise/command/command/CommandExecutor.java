package cn.chuanwise.command.command;

import cn.chuanwise.command.context.CommandContext;

/**
 * 指令执行器
 *
 * @author Chuanwise
 */
public interface CommandExecutor {
    
    /**
     * 执行指令
     *
     * @param context 执行上下文
     * @return 指令是否被执行
     * @throws Exception 执行过程中出现异常
     */
    boolean execute(CommandContext context) throws Exception;
}
