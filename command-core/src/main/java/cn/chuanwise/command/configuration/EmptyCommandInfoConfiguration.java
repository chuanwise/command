package cn.chuanwise.command.configuration;

/**
 * 空指令信息配置
 *
 * @author Chuanwise
 */
public final class EmptyCommandInfoConfiguration
        implements CommandInfoConfiguration {

    private static final EmptyCommandInfoConfiguration INSTANCE = new EmptyCommandInfoConfiguration();

    public static EmptyCommandInfoConfiguration getInstance() {
        return INSTANCE;
    }

    private EmptyCommandInfoConfiguration() {}

    @Override
    public CommandInfo getCommandInfo(String name) {
        return null;
    }

    @Override
    public void setCommandInfo(String name, CommandInfo commandInfo) {}
}
