package cn.chuanwise.command.wirer;

import cn.chuanwise.command.command.ParameterInfo;
import cn.chuanwise.command.context.WireContext;
import cn.chuanwise.command.context.ReferenceInfo;
import cn.chuanwise.common.space.Container;

public class StringReferenceWirer extends ReferenceWirer<String> {

    public StringReferenceWirer(ParameterInfo parameterInfo) {
        super(String.class, parameterInfo);
    }

    @Override
    protected Container<String> provide1(WireContext context, ReferenceInfo referenceInfo) throws Exception {
        return Container.of(referenceInfo.getString());
    }
}
