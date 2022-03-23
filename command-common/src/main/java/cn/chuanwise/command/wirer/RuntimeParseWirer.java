package cn.chuanwise.command.wirer;

import cn.chuanwise.command.command.ParameterInfo;
import cn.chuanwise.command.context.ParseContext;
import cn.chuanwise.command.context.WireContext;
import cn.chuanwise.command.context.ReferenceInfo;
import cn.chuanwise.command.handler.Priority;
import cn.chuanwise.common.space.Container;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

@Data
public class RuntimeParseWirer<T>
        extends AbstractWirer<T> {

    protected final ParameterInfo parameterInfo;

    public RuntimeParseWirer(Class<T> filledClass, ParameterInfo parameterInfo) {
        super(filledClass, Priority.NORMAL);

        Preconditions.namedArgumentNonNull(parameterInfo, "parameter info");

        this.parameterInfo = parameterInfo;
    }

    @Override
    @SuppressWarnings("all")
    protected Container<T> wire0(WireContext context) throws Exception {
        final ReferenceInfo parsingReferenceInfo = context.getReferenceInfo().get(parameterInfo.getName());

        Preconditions.stateNonNull(parsingReferenceInfo, "reference info");

        return (Container<T>) context.getCommander().parse(
                new ParseContext(
                        context.getCommandSender(),
                        context.getReferenceInfo(),
                        context.getCommand(),
                        parsingReferenceInfo,
                        context.getParameter().getType()
                )
        );
    }
}
