package cn.chuanwise.commandlib;

import cn.chuanwise.commandlib.completer.Completer;
import cn.chuanwise.commandlib.configuration.CommandLibConfiguration;
import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.commandlib.context.CompleteContext;
import cn.chuanwise.commandlib.context.ParserContext;
import cn.chuanwise.commandlib.handler.Handler;
import cn.chuanwise.commandlib.handler.HandlerAdapter;
import cn.chuanwise.commandlib.handler.Pipeline;
import cn.chuanwise.commandlib.parser.Parser;
import cn.chuanwise.commandlib.provider.Provider;
import cn.chuanwise.commandlib.tree.Dispatcher;
import cn.chuanwise.function.ExceptionConsumer;
import cn.chuanwise.function.ExceptionFunction;
import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.CollectionUtil;
import cn.chuanwise.util.Preconditions;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class CommandLib {

    @Getter(AccessLevel.NONE)
    protected final Dispatcher dispatcher = new Dispatcher(this);

    protected CommandLibConfiguration configuration;

    @Getter(AccessLevel.NONE)
    protected final Pipeline pipeline = new Pipeline(this);

    public CommandLib(CommandLibConfiguration configuration) {
        Preconditions.argumentNonNull(configuration, "configuration");

        this.configuration = configuration;
    }

    public CommandLib() {
        this.configuration = new CommandLibConfiguration();
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }

    public boolean handleEvent(Object event) {
        Preconditions.argumentNonNull(event, "event");

        try {
            return pipeline.handleEvent(event);
        } catch (Throwable cause) {
            handleException(cause);
            return false;
        }
    }

    public boolean handleException(Throwable cause) {
        Preconditions.argumentNonNull(cause, "cause");

        return pipeline.handleException(cause);
    }

    public Pipeline pipeline() {
        return pipeline;
    }
}
