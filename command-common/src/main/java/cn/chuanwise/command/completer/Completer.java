package cn.chuanwise.command.completer;

import cn.chuanwise.command.context.CompleteContext;
import cn.chuanwise.command.handler.Priority;
import cn.chuanwise.common.api.ExceptionFunction;
import cn.chuanwise.common.util.Preconditions;

import java.util.Collections;
import java.util.Set;

/**
 * 补全工具
 *
 * @author Chuanwise
 */
public interface Completer {
    
    /**
     * 执行一次补全
     *
     * @param context 补全上下文
     * @return 补全结果
     * @throws Exception 补全时出现异常
     */
    Set<String> complete(CompleteContext context) throws Exception;
    
    /**
     * 获取优先级
     *
     * @return 优先级
     */
    Priority getPriority();
}