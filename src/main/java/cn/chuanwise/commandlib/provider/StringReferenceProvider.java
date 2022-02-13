package cn.chuanwise.commandlib.provider;

import cn.chuanwise.commandlib.command.ParameterInfo;
import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.commandlib.context.ReferenceInfo;
import cn.chuanwise.toolkit.container.Container;

public class StringReferenceProvider extends ReferenceProvider<String> {

    public StringReferenceProvider(ParameterInfo parameterInfo) {
        super(String.class, parameterInfo);
    }

    @Override
    protected Container<String> provide0(CommandContext context, ReferenceInfo referenceInfo) throws Exception {
        return Container.of(referenceInfo.getString());
    }
}
