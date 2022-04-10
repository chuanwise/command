package cn.chuanwise.command.completer;

import cn.chuanwise.command.context.CompleteContext;
import cn.chuanwise.common.algorithm.LongestCommonSubsequence;

/**
 * 补全器过滤器，用于过滤不符合输入的项目
 *
 * @author Chuanwise
 */
public interface CompleterFilter {
    
    /**
     * 过滤补全值
     *
     * @param element 当前值
     * @param context 补全上下文
     * @return 是否保留该项目
     * @throws Exception 过滤时出现异常
     */
    boolean filter(String element, CompleteContext context) throws Exception;
    
    /**
     * 默认的最长公共子序列策略
     */
    CompleterFilter LONGEST_COMMON_SUBSEQUENCE_FILTER = (element, context) -> {
        if (context.isUncompleted()) {
            final String uncompletedPart = context.getUncompletedPart();
            final double minSequenceLength = uncompletedPart.length() * context.getCommander().getCommanderConfiguration().getMinCompleterCommonRate();
            return LongestCommonSubsequence.length(element, uncompletedPart) >= minSequenceLength;
        } else {
            return true;
        }
    };
}
