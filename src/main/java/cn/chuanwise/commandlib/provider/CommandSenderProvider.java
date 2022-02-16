package cn.chuanwise.commandlib.provider;

import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.commandlib.context.ProvideContext;
import cn.chuanwise.toolkit.container.Container;

public class CommandSenderProvider
        extends SimpleProvider<Object> {

    @Override
    protected Container<Object> provide0(ProvideContext context) throws Exception {
        return Container.of(context.getCommandSender());
    }
}
