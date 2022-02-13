package cn.chuanwise.commandlib.provider;

import cn.chuanwise.commandlib.command.ParameterInfo;
import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.commandlib.context.ReferenceInfo;
import cn.chuanwise.commandlib.parser.Parser;
import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

@Data
public class ParserReferenceProvider<T> extends ReferenceProvider<T> {

    protected final Parser<T> parser;

    public ParserReferenceProvider(Parser<T> parser, ParameterInfo parameterInfo) {
        super(parser.getParsedClass(), parameterInfo);

        this.parser = parser;
    }

    @Override
    protected Container<T> provide0(CommandContext context, ReferenceInfo referenceInfo) throws Exception {
        return parser.parse(context, referenceInfo);
    }
}
