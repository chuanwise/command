package cn.chuanwise.commandlib.configuration;

import java.util.Optional;

public interface CommandInfoConfiguration {
    Optional<CommandInfo> getCommandInfo(String name);

    void setCommandInfo(String name, CommandInfo commandInfo);

    static CommandInfoConfiguration empty() {
        return EmptyCommandInfoConfiguration.getInstance();
    }
}
