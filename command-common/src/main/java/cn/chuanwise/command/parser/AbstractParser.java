package cn.chuanwise.command.parser;

import cn.chuanwise.command.context.ParseContext;
import cn.chuanwise.command.handler.Priority;
import cn.chuanwise.common.space.Container;
import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.Types;
import lombok.Data;

/**
 * 抽象解析器
 *
 * @author Chuanwise
 */
@Data
public abstract class AbstractParser<T>
    implements Parser {
    
    private final Class<T> parsedClass;
    private final Priority priority;
    
    public AbstractParser(Class<T> parsedClass, Priority priority) {
        Preconditions.namedArgumentNonNull(parsedClass, "parsed class");
        Preconditions.namedArgumentNonNull(priority, "priority");
        
        this.parsedClass = parsedClass;
        this.priority = priority;
    }
    
    @SuppressWarnings("all")
    public AbstractParser(Priority priority) {
        Preconditions.namedArgumentNonNull(priority, "priority");
        
        this.parsedClass = (Class<T>) Types.getTypeParameterClass(getClass(), AbstractParser.class);
        this.priority = priority;
    }
    
    @Override
    public Container<?> parse(ParseContext context) throws Exception {
        if (context.getRequiredClass().isAssignableFrom(parsedClass)) {
            return parse0(context);
        }
        return Container.empty();
    }
    
    protected abstract Container<T> parse0(ParseContext context) throws Exception;
}
