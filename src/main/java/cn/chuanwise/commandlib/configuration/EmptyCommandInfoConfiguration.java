package cn.chuanwise.commandlib.configuration;

import java.util.Optional;

public final class EmptyCommandInfoConfiguration
        implements CommandInfoConfiguration {

    private static final EmptyCommandInfoConfiguration INSTANCE = new EmptyCommandInfoConfiguration();

    public static EmptyCommandInfoConfiguration getInstance() {
        return INSTANCE;
    }

    private EmptyCommandInfoConfiguration() {}

    @Override
    public Optional<CommandInfo> getCommandInfo(String name) {
        return Optional.empty();
    }

    @Override
    public void setCommandInfo(String name, CommandInfo commandInfo) {}
}
