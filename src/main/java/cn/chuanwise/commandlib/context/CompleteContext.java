package cn.chuanwise.commandlib.context;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.tree.DispatchFork;
import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.Strings;
import lombok.Data;

import java.util.List;

/**
 * 补全上下文是按下 Tab 或通过其他类似的方式进行代码补全时
 * 自动提示的上下文，主要内容是当前输入的内容以及所对应位置
 * 参数的相关定义信息。
 */
@Data
public class CompleteContext
        extends CommandLibContext {

    protected final String string;
    protected final List<DispatchFork> dispatchForks;

    public CompleteContext(CommandLib commandLib, List<DispatchFork> dispatchForks, String string) {
        super(commandLib);

        Preconditions.argumentNonNull(dispatchForks, "dispatch forks");

        this.string = string;
        this.dispatchForks = dispatchForks;
    }

    public boolean isUncompleted() {
        return Strings.nonEmpty(string);
    }
}
