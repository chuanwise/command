package cn.chuanwise.command.api;

/**
 * 带有指令发送人的某种对象
 *
 * @author Chuanwise
 */
public interface CommandSenderHolder {
    
    /**
     * 获取指令发送人
     *
     * @return 指令发送人
     */
    Object getCommandSender();
}
