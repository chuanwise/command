package cn.chuanwise.commandlib.handler;

import cn.chuanwise.commandlib.context.ParserContext;
import cn.chuanwise.commandlib.parser.SimpleParser;
import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.Types;
import lombok.Data;

@Data
public abstract class SimpleParserHandler<T>
        extends HandlerAdapter {

    protected final Class<T> parsedClass;

    public SimpleParserHandler(Class<T> parsedClass) {
        Preconditions.argumentNonNull(parsedClass, "parsed class");

        this.parsedClass = parsedClass;
    }

    @SuppressWarnings("all")
    public SimpleParserHandler() {
        this.parsedClass = (Class<T>) Types.getTypeParameterClass(getClass(), SimpleParser.class);
    }

    @Override
    public final Container<?> parse(ParserContext context) throws Exception {
        if (context.getRequiredClass().isAssignableFrom(parsedClass)) {
            return parse0(context);
        }
        return Container.empty();
    }

    protected abstract Container<T> parse0(ParserContext context) throws Exception;
}
