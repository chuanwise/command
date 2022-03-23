package cn.chuanwise.command.configuration;

import cn.chuanwise.command.completer.CompleterFilter;
import cn.chuanwise.command.completer.CompleterSorter;
import cn.chuanwise.command.format.FormatCompiler;
import lombok.Data;

/**
 * 设置项
 *
 * @author Chuanwise
 */
@Data
public class CommanderConfiguration {
    
    /**
     * 选项前后缀设置
     */
    @Data
    public static class Option {

        protected String prefix = "--";
        protected String splitter = "=";
    }
    
    /**
     * 选项前后缀设置
     */
    protected Option option = new Option();
    
    /**
     * 是否开启强匹配。
     * 强匹配意味着可空参数等孤立节点不能有兄弟节点。
     */
    protected boolean strongMatch = false;
    
    /**
     * 是否合并相关分支
     */
    protected boolean mergeRelatedForks = true;
    
    /**
     * 是否允许未定义选项值出现
     */
    protected boolean allowUndefinedOptionValue = false;
    
    /**
     * 是否将描述值增加到补全内容中
     */
    protected boolean addReferenceDescriptionsToCompleterElements = true;
    
    /**
     * 是否序列化补全项目
     */
    protected boolean serializeCompleterElements = true;
    
    /**
     * 筛除不符合条件的补全项时的最小相似度
     */
    protected double minCompleterCommonRate = 1;
    
    /**
     * 补全项排序器
     */
    protected CompleterSorter completerSorter = CompleterSorter.LONGEST_COMMON_SUBSTRING;
    
    /**
     * 补全项过滤器
     */
    protected CompleterFilter completerFilter = CompleterFilter.LONGEST_COMMON_SUBSEQUENCE_FILTER;
    
    /**
     * 格式串编译器
     */
    protected FormatCompiler formatCompiler = FormatCompiler.defaultCompiler();
}
