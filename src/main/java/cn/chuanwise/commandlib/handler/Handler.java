package cn.chuanwise.commandlib.handler;

import cn.chuanwise.commandlib.completer.Completer;
import cn.chuanwise.commandlib.event.EventHandler;
import cn.chuanwise.commandlib.exception.ExceptionHandler;
import cn.chuanwise.commandlib.parser.Parser;
import cn.chuanwise.commandlib.provider.Provider;

public interface Handler
        extends Parser, ExceptionHandler, EventHandler, Completer, Provider {

    void handlerAdded(HandlerContext context) throws Exception;

    void handlerRemoved(HandlerContext context) throws Exception;
}
