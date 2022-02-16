package cn.chuanwise.commandlib.provider;

import cn.chuanwise.commandlib.command.ParameterInfo;
import cn.chuanwise.commandlib.context.ParserContext;
import cn.chuanwise.commandlib.context.ProvideContext;
import cn.chuanwise.commandlib.context.ReferenceInfo;
import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

@Data
public class RuntimeParseProvider<T>
        extends SimpleProvider<T> {

    protected final ParameterInfo parameterInfo;

    public RuntimeParseProvider(Class<T> providedClass, ParameterInfo parameterInfo) {
        super(providedClass);

        Preconditions.argumentNonNull(parameterInfo, "parameter info");

        this.parameterInfo = parameterInfo;
    }

    @Override
    @SuppressWarnings("all")
    protected Container<T> provide0(ProvideContext context) throws Exception {
        final ReferenceInfo parsingReferenceInfo = context.getReferenceInfo().get(parameterInfo.getName());

        Preconditions.stateNonNull(parsingReferenceInfo, "reference info");

        return (Container<T>) context.getCommandLib().pipeline().handleParse(
                new ParserContext(
                        context.getCommandSender(),
                        context.getReferenceInfo(),
                        context.getCommand(),
                        parsingReferenceInfo,
                        context.getParameter().getType()
                )
        );
    }
}
