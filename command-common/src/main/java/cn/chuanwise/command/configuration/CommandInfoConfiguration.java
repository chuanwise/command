package cn.chuanwise.command.configuration;

import java.util.Optional;

/**
 * 指令格式配置，用于载入和保存指令格式
 *
 * @author Chuanwise
 */
public interface CommandInfoConfiguration {
    
    /**
     * 获取指令格式
     *
     * @param name 指令名
     * @return 指令格式
     */
    CommandInfo getCommandInfo(String name);

    void setCommandInfo(String name, CommandInfo commandInfo);

    static CommandInfoConfiguration empty() {
        return EmptyCommandInfoConfiguration.getInstance();
    }
}
