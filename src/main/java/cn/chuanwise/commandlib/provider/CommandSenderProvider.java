package cn.chuanwise.commandlib.provider;

import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.toolkit.container.Container;

public class CommandSenderProvider extends Provider<Object> {
    @Override
    public Container<Object> provide(CommandContext context) throws Exception {
        return Container.of(context.getCommandSender());
    }
}
