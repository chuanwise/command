package cn.chuanwise.command.parser;

import cn.chuanwise.command.context.ParseContext;
import cn.chuanwise.command.handler.Priority;
import cn.chuanwise.common.space.Container;

/**
 * 解析器
 *
 * @author Chuanwise
 */
public interface Parser {
    
    /**
     * 解析出相关对象
     *
     * @param context 解析上下文
     * @return 相关对象
     * @throws Exception 解析出现异常
     */
    Container<?> parse(ParseContext context) throws Exception;
    
    /**
     * 获取解析器优先级
     *
     * @return 解析器优先级
     */
    Priority getPriority();
    
    /**
     * 获取解析类型
     *
     * @return 解析类型
     */
    Class<?> getParsedClass();
}
