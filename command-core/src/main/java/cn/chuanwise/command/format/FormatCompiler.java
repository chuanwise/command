package cn.chuanwise.command.format;

/**
 * 指令格式编译器
 *
 * @author Chuanwise
 */
@FunctionalInterface
public interface FormatCompiler {
    
    /**
     * 编译指令格式
     *
     * @param input 指令格式字符串
     * @return 指令格式
     */
    FormatInfo compile(String input);
    
    /**
     * 获取默认指令格式编译器
     *
     * @return 默认指令格式编译器
     */
    static FormatCompiler defaultCompiler() {
        return DefaultFormatCompiler.getInstance();
    }
}
