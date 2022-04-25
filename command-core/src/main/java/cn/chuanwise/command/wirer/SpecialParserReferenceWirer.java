package cn.chuanwise.command.wirer;

import cn.chuanwise.command.command.ParameterInfo;
import cn.chuanwise.command.context.*;
import cn.chuanwise.command.parser.Parser;
import cn.chuanwise.common.space.Container;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

@Data
public class SpecialParserReferenceWirer<T>
        extends ReferenceWirer<T> {

    protected final Parser parser;

    public SpecialParserReferenceWirer(Parser parser, Class<T> filledClass, ParameterInfo parameterInfo) {
        super(filledClass, parameterInfo);

        Preconditions.objectNonNull(parser, "parser");

        this.parser = parser;
    }

    @Override
    @SuppressWarnings("all")
    protected Container<T> provide1(WireContext context, ReferenceInfo referenceInfo) throws Exception {
        return (Container<T>) parser.parse(
                new ParseContext(
                        context.getCommandSender(),
                        context.getReferenceInfo(),
                        context.getCommand(),
                        referenceInfo,
                    wiredClass
                )
        );
    }
}
