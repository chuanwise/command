package cn.chuanwise.command.parser;

import cn.chuanwise.command.context.ParseContext;
import cn.chuanwise.command.handler.Priority;
import cn.chuanwise.common.api.ExceptionFunction;
import cn.chuanwise.common.space.Container;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

/**
 * 简单解析器
 *
 * @param <T> 解析器类型
 */
@Data
@SuppressWarnings("all")
public class SimpleParser<T>
    extends AbstractParser<T> {
    
    private ExceptionFunction<ParseContext, Container<T>> action;
    
    public SimpleParser(Priority priority, ExceptionFunction<ParseContext, Container<T>> action) {
        super(priority);
    
        Preconditions.namedArgumentNonNull(action, "action");
        
        this.action = action;
    }
    
    public SimpleParser(Class<T> parsedClass, Priority priority, ExceptionFunction<ParseContext, Container<T>> action) {
        super(parsedClass, priority);
    
        Preconditions.namedArgumentNonNull(action, "action");
        
        this.action = action;
    }
    
    @Override
    protected Container<T> parse0(ParseContext context) throws Exception {
        return action.exceptApply(context);
    }
    
    @SuppressWarnings("all")
    public SimpleParser(Class<T> parsedClass, Priority priority) {
        super(parsedClass, priority);
    }
}