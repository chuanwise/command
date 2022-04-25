package cn.chuanwise.command.handler;

import cn.chuanwise.command.command.Command;
import cn.chuanwise.command.configuration.CommandInfo;
import cn.chuanwise.command.configuration.CommandInfoConfiguration;
import cn.chuanwise.command.event.AbstractEventHandler;
import cn.chuanwise.command.event.CommandRegisterEvent;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

import java.util.Objects;

/**
 * 指令信息配置处理器
 *
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class CommandInfoConfigurationEventHandler
    extends AbstractEventHandler<CommandRegisterEvent> {

    protected final CommandInfoConfiguration configuration;
    
    public CommandInfoConfigurationEventHandler(CommandInfoConfiguration configuration) {
        Preconditions.objectNonNull(configuration, "configuration");
        
        this.configuration = configuration;
    }
    
    @Override
    public boolean handleEvent0(CommandRegisterEvent commandRegisterEvent) throws Exception {
        final Command command = commandRegisterEvent.getCommand();

        // 更新相关设置
        final CommandInfo commandInfo = configuration.getCommandInfo(command.getName());
        if (Objects.nonNull(commandInfo)) {
            command.setCommandInfo(commandInfo);
        } else {
            configuration.setCommandInfo(command.getName(), command.getCommandInfo());
        }
        
        return true;
    }
}
