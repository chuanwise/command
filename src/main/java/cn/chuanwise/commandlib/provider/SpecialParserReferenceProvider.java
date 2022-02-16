package cn.chuanwise.commandlib.provider;

import cn.chuanwise.commandlib.command.ParameterInfo;
import cn.chuanwise.commandlib.context.*;
import cn.chuanwise.commandlib.parser.Parser;
import cn.chuanwise.commandlib.parser.SimpleParser;
import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

@Data
public class SpecialParserReferenceProvider<T>
        extends ReferenceProvider<T> {

    protected final Parser parser;

    public SpecialParserReferenceProvider(Parser parser, Class<T> providedClass, ParameterInfo parameterInfo) {
        super(providedClass, parameterInfo);

        Preconditions.argumentNonNull(parser, "parser");

        this.parser = parser;
    }

    @Override
    @SuppressWarnings("all")
    protected Container<T> provide1(ProvideContext context, ReferenceInfo referenceInfo) throws Exception {
        return (Container<T>) parser.parse(
                new ParserContext(
                        context.getCommandSender(),
                        context.getReferenceInfo(),
                        context.getCommand(),
                        referenceInfo,
                        providedClass
                )
        );
    }
}
