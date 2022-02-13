package cn.chuanwise.commandlib.provider;

import cn.chuanwise.commandlib.command.ParameterInfo;
import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.commandlib.context.ReferenceInfo;
import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

@Data
public abstract class ReferenceProvider<T> extends Provider<T> {
    final ParameterInfo parameterInfo;

    public ReferenceProvider(Class<T> providedClass, ParameterInfo parameterInfo) {
        super(providedClass);

        Preconditions.argumentNonNull(parameterInfo, "parameter info");

        this.parameterInfo = parameterInfo;
    }

    @Override
    public final Container<T> provide(CommandContext context) throws Exception {
        final ReferenceInfo referenceInfo = context.getReferenceInfo().get(parameterInfo.getName());

        Preconditions.stateNonNull(referenceInfo, "无法找到引用变量：" + referenceInfo.getName());

        return provide0(context, referenceInfo);
    }

    protected abstract Container<T> provide0(CommandContext context, ReferenceInfo referenceInfo) throws Exception;
}
