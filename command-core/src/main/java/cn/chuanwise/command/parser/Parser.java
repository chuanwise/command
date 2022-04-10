package cn.chuanwise.command.parser;

import cn.chuanwise.command.context.ParseContext;
import cn.chuanwise.command.Priority;
import cn.chuanwise.common.space.Container;

/**
 * 解析器
 *
 * @author Chuanwise
 */
@FunctionalInterface
public interface Parser {
    
    /**
     * 解析出相关对象
     *
     * @param context 解析上下文
     * @return 相关对象
     * @throws Exception 解析出现异常
     */
    Container<?> parse(ParseContext context) throws Exception;
}
