package cn.chuanwise.commandlib.handler;

import cn.chuanwise.commandlib.context.CompleteContext;
import cn.chuanwise.commandlib.context.ParserContext;
import cn.chuanwise.commandlib.context.ProvideContext;
import cn.chuanwise.toolkit.container.Container;

import java.util.Collections;
import java.util.Set;

public class HandlerAdapter
        implements Handler {

    @Override
    public boolean handleException(Throwable cause) throws Throwable {
        return false;
    }

    @Override
    public boolean handleEvent(Object event) throws Exception {
        return false;
    }

    @Override
    public Container<?> parse(ParserContext context) throws Exception {
        return Container.empty();
    }

    @Override
    public Set<String> complete(CompleteContext context) throws Exception {
        return Collections.emptySet();
    }

    @Override
    public void handlerAdded(HandlerContext context) throws Exception {}

    @Override
    public void handlerRemoved(HandlerContext context) throws Exception {}

    @Override
    public Container<?> provide(ProvideContext context) throws Exception {
        return Container.empty();
    }
}
