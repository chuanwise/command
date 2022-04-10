package cn.chuanwise.command.completer;

import cn.chuanwise.command.context.CompleteContext;
import cn.chuanwise.command.Priority;

import java.util.Set;

/**
 * 补全工具
 *
 * @author Chuanwise
 */
@FunctionalInterface
public interface Completer {
    
    /**
     * 执行一次补全
     *
     * @param context 补全上下文
     * @return 补全结果
     * @throws Exception 补全时出现异常
     */
    Set<String> complete(CompleteContext context) throws Exception;
}