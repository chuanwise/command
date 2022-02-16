package cn.chuanwise.commandlib.parser;

import cn.chuanwise.commandlib.context.ParserContext;
import cn.chuanwise.toolkit.container.Container;

public interface Parser {
    Container<?> parse(ParserContext context) throws Exception;
}
