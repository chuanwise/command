package cn.chuanwise.command;

import cn.chuanwise.command.context.ParseContext;
import cn.chuanwise.command.object.AbstractCommanderObject;
import cn.chuanwise.command.parser.Parser;
import cn.chuanwise.command.parser.SimpleParser;
import cn.chuanwise.common.api.ExceptionFunction;
import cn.chuanwise.common.space.Container;
import cn.chuanwise.common.util.Maps;
import cn.chuanwise.common.util.Preconditions;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 解析服务
 *
 * @author Chuanwise
 */
public class ParseService
    extends AbstractCommanderObject {
    
    /**
     * 解析器
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected final Map<Priority, List<Parser>> parsers = new HashMap<>();
    
    public ParseService(Commander commander) {
        super(commander);
    }
    
    public void registerParser(Parser parser) {
        registerParser(parser, Priority.NORMAL);
    }
    
    /**
     * 注册一个指定的解析器
     *
     * @param parser 解析器
     */
    public void registerParser(Parser parser, Priority priority) {
        Preconditions.objectNonNull(parser, "parser");
        Preconditions.objectNonNull(priority, "priority");
        
        Maps.getOrPutGet(parsers, priority, CopyOnWriteArrayList::new).add(parser);
    }
    
    /**
     * 注册一个解析器
     *
     * @param parsedClass 事件类
     * @param priority    优先级
     * @param action      监听行为
     * @param <T>         事件类型
     * @return 被注册的解析器
     */
    public <T> Parser registerParser(Class<T> parsedClass, Priority priority, ExceptionFunction<ParseContext, Container<T>> action) {
        Preconditions.objectNonNull(parsedClass, "parsed class");
        Preconditions.objectNonNull(action, "action");
        Preconditions.objectNonNull(priority, "priority");
        
        final List<Parser> parsers = Maps.getOrPutGet(this.parsers, priority, CopyOnWriteArrayList::new);
        final SimpleParser<T> parser = new SimpleParser<>(parsedClass, action);
        parsers.add(parser);
        
        return parser;
    }
    
    /**
     * 注册一个解析器
     *
     * @param parsedClass 事件类
     * @param action      监听行为
     * @param <T>         事件类型
     * @return 被注册的解析器
     */
    public <T> Parser registerParser(Class<T> parsedClass, ExceptionFunction<ParseContext, Container<T>> action) {
        return registerParser(parsedClass, Priority.NORMAL, action);
    }
    
    /**
     * 卸载解析器
     *
     * @param parser 解析器
     * @return 是否卸载了该解析器
     */
    public boolean unregisterParser(Parser parser) {
        Preconditions.objectNonNull(parser, "parser");
    
        final boolean highest = unregisterParser(Priority.HIGHEST, parser);
        final boolean high = unregisterParser(Priority.HIGH, parser);
        final boolean normal = unregisterParser(Priority.NORMAL, parser);
        final boolean low = unregisterParser(Priority.LOW, parser);
        final boolean lowest = unregisterParser(Priority.LOWEST, parser);
        
        return highest || high || normal || low || lowest;
    }
    
    /**
     * 卸载某个解析器
     *
     * @param priority 解析器优先级
     * @param parser   解析器
     * @return 是否卸载了该解析器
     */
    private boolean unregisterParser(Priority priority, Parser parser) {
        final List<Parser> parsers = this.parsers.get(priority);
        boolean removed;
        if (cn.chuanwise.common.util.Collections.nonEmpty(parsers)) {
            removed = parsers.remove(parser);
            if (parsers.isEmpty()) {
                this.parsers.remove(priority);
            }
        } else {
            removed = false;
        }
        
        return removed;
    }
    
    /**
     * 解析一个对象
     *
     * @param context 解析上下文
     * @return 解析结果
     */
    public Container<?> parse(ParseContext context) {
        Preconditions.objectNonNull(context, "context");
        
        final Container<?> highest = parse0(Priority.HIGHEST, context);
        if (!highest.isEmpty()) {
            return highest;
        }
        final Container<?> high = parse0(Priority.HIGH, context);
        if (!high.isEmpty()) {
            return high;
        }
        final Container<?> normal = parse0(Priority.NORMAL, context);
        if (!normal.isEmpty()) {
            return normal;
        }
        final Container<?> low = parse0(Priority.LOW, context);
        if (!low.isEmpty()) {
            return low;
        }
        final Container<?> lowest = parse0(Priority.LOWEST, context);
        if (!lowest.isEmpty()) {
            return lowest;
        }
        
        return Container.empty();
    }
    
    private Container<?> parse0(Priority priority, ParseContext context) {
        final List<Parser> parsers = this.parsers.get(priority);
        if (cn.chuanwise.common.util.Collections.nonEmpty(parsers)) {
            for (Parser parser : parsers) {
                final Container<?> container = commander.getExceptionService().catching(() -> parser.parse(context), Container.empty());
                if (!container.isEmpty()) {
                    return container;
                }
            }
        }
        return Container.empty();
    }
}
