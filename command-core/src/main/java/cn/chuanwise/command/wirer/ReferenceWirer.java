package cn.chuanwise.command.wirer;

import cn.chuanwise.command.command.ParameterInfo;
import cn.chuanwise.command.context.WireContext;
import cn.chuanwise.command.context.ReferenceInfo;
import cn.chuanwise.command.Priority;
import cn.chuanwise.common.space.Container;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

@Data
public abstract class ReferenceWirer<T>
        extends AbstractWirer<T> {

    protected final ParameterInfo parameterInfo;

    public ReferenceWirer(Class<T> filledClass, ParameterInfo parameterInfo) {
        super(filledClass);

        Preconditions.namedArgumentNonNull(parameterInfo, "parameter info");

        this.parameterInfo = parameterInfo;
    }

    @Override
    protected final Container<T> wire0(WireContext context) throws Exception {
        final ReferenceInfo referenceInfo = context.getReferenceInfo().get(parameterInfo.getName());

        Preconditions.stateNonNull(referenceInfo, "无法找到引用变量：" + referenceInfo.getName());

        return provide1(context, referenceInfo);
    }

    protected abstract Container<T> provide1(WireContext context, ReferenceInfo referenceInfo) throws Exception;
}
