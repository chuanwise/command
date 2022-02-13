package cn.chuanwise.commandlib;

import cn.chuanwise.commandlib.completer.Completer;
import cn.chuanwise.commandlib.configuration.CommandLibConfiguration;
import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.commandlib.context.CompleteContext;
import cn.chuanwise.commandlib.context.ReferenceInfo;
import cn.chuanwise.commandlib.handler.CommandLibHandler;
import cn.chuanwise.commandlib.parser.Parser;
import cn.chuanwise.commandlib.provider.Provider;
import cn.chuanwise.commandlib.tree.CommandManager;
import cn.chuanwise.function.ExceptionBiFunction;
import cn.chuanwise.function.ExceptionConsumer;
import cn.chuanwise.function.ExceptionFunction;
import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.CollectionUtil;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class CommandLib {

    protected final CommandManager commandManager = new CommandManager(this);

    protected CommandLibConfiguration configuration;

    protected final List<Parser<?>> parsers = new ArrayList<>();
    protected final List<Provider<?>> providers = new ArrayList<>();
    protected final List<Completer> completers = new ArrayList<>();

    protected final List<CommandLibHandler> handlers = new ArrayList<>();

    public CommandLib(CommandLibConfiguration configuration) {
        Preconditions.argumentNonNull(configuration, "configuration");

        this.configuration = configuration;
    }

    public CommandLib() {
        this.configuration = new CommandLibConfiguration();
    }


    public <T> void registerParser(Class<T> parsedClass, ExceptionBiFunction<CommandContext, ReferenceInfo, Container<T>> function) {
        Preconditions.argumentNonNull(parsedClass, "parsed class");
        Preconditions.argumentNonNull(function, "function");

        parsers.add(Parser.of(parsedClass, function));
    }

    public void registerParser(Parser<?> parser) {
        Preconditions.argumentNonNull(parser, "parser");

        parsers.add(parser);
    }

    @SuppressWarnings("all")
    public <T> List<Parser<T>> getParsers(Class<T> parsedClass) {
        Preconditions.argumentNonNull(parsedClass, "parsed class");

        return (List) Collections.unmodifiableList(parsers.stream()
                .filter(x -> parsedClass.isAssignableFrom(x.getParsedClass()))
                .collect(Collectors.toList()));
    }

    public List<Parser<?>> getParsers() {
        return Collections.unmodifiableList(parsers);
    }

    @SuppressWarnings("all")
    public <T> Optional<Parser<T>> getParser(Class<T> parsedClass) {
        Preconditions.argumentNonNull(parsedClass, "parsed class");

        return (Optional) CollectionUtil.findLast(parsers, x -> parsedClass.isAssignableFrom(x.getParsedClass()))
                .toOptional();
    }


    public <T> void registerProvider(Class<T> providedClass, ExceptionFunction<CommandContext, Container<T>> function) {
        Preconditions.argumentNonNull(providedClass, "provided class");
        Preconditions.argumentNonNull(function, "function");

        providers.add(Provider.of(providedClass, function));
    }

    public void registerProvider(Provider<?> provider) {
        Preconditions.argumentNonNull(provider, "provider");

        providers.add(provider);
    }

    @SuppressWarnings("all")
    public <T> List<Provider<T>> getProviders(Class<T> providedClass) {
        Preconditions.argumentNonNull(providedClass, "provided class");

        return (List) Collections.unmodifiableList(providers.stream()
                .filter(x -> providedClass.isAssignableFrom(x.getProvidedClass()))
                .collect(Collectors.toList()));
    }

    public List<Provider<?>> getProviders() {
        return Collections.unmodifiableList(providers);
    }

    @SuppressWarnings("all")
    public <T> Optional<Provider<T>> getProvider(Class<T> providedClass) {
        Preconditions.argumentNonNull(providedClass, "provided class");

        return (Optional) CollectionUtil.findLast(providers, x -> providedClass.isAssignableFrom(x.getProvidedClass()))
                .toOptional();
    }


    public <T> void registerEventHandler(Class<T> eventClass, ExceptionConsumer<T> consumer) {
        Preconditions.argumentNonNull(eventClass, "event class");
        Preconditions.argumentNonNull(consumer, "consumer");

        handlers.add(CommandLibHandler.ofEvent(eventClass, consumer));
    }


    public void registerCompleter(Class<?> completedClass, ExceptionFunction<CompleteContext, Set<String>> function) {
        Preconditions.argumentNonNull(completedClass, "completed class");
        Preconditions.argumentNonNull(function, "function");

        completers.add(Completer.of(completedClass, function));
    }

    public void registerCompleter(Completer completer) {
        Preconditions.argumentNonNull(completer, "completer");

        completers.add(completer);
    }

    public List<Completer> getCompleters(Class<?> completedClass) {
        return Collections.unmodifiableList(completers.stream()
                .filter(x -> completedClass.isAssignableFrom(x.getCompletedClass()))
                .collect(Collectors.toList()));
    }

    public List<Completer> getCompleters() {
        return Collections.unmodifiableList(completers);
    }

    @SuppressWarnings("all")
    public Optional<Completer> getCompleter(Class<?> completedClass) {
        Preconditions.argumentNonNull(completedClass, "completed class");

        return (Optional) CollectionUtil.findLast(completers, x -> completedClass.isAssignableFrom(x.getCompletedClass()))
                .toOptional();
    }


    public <T extends Throwable> void registerExceptionHandler(Class<T> exceptionClass, ExceptionConsumer<T> consumer) {
        Preconditions.argumentNonNull(exceptionClass, "exception class");
        Preconditions.argumentNonNull(consumer, "consumer");

        handlers.add(CommandLibHandler.ofException(exceptionClass, consumer));
    }


    public boolean handleEvent(Object event) throws Exception {
        Preconditions.argumentNonNull(event, "event");

        for (CommandLibHandler commandLibHandler : handlers) {
            if (commandLibHandler.handleEvent(event)) {
                return true;
            }
        }

        return false;
    }

    public boolean handleException(Throwable cause) {
        for (CommandLibHandler handler : handlers) {
            try {
                if (handler.handleException(cause)) {
                    return true;
                }
            } catch (Throwable t) {
                cause = t;
            }
        }

        cause.printStackTrace();
        return false;
    }


    public void registerHandler(CommandLibHandler commandLibHandler) {
        Preconditions.argumentNonNull(commandLibHandler, "listener");

        handlers.add(commandLibHandler);
    }

    public List<CommandLibHandler> getHandlers() {
        return Collections.unmodifiableList(handlers);
    }
}
