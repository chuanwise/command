package cn.chuanwise.commandlib.context;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.util.Strings;
import lombok.Data;

/**
 * 补全上下文是按下 Tab 或通过其他类似的方式进行代码补全时
 * 自动提示的上下文，主要内容是当前输入的内容以及所对应位置
 * 参数的相关定义信息。
 */
@Data
public class CompleteContext
        extends CommandLibContext {

    protected final String string;

    public CompleteContext(CommandLib commandLib, String string) {
        super(commandLib);

        this.string = string;
    }

    public boolean isUncompleted() {
        return Strings.nonEmpty(string);
    }
}
