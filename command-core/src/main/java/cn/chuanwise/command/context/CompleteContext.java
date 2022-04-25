package cn.chuanwise.command.context;

import cn.chuanwise.command.Commander;
import cn.chuanwise.command.api.CommandSenderHolder;
import cn.chuanwise.command.tree.CommandTreeFork;
import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.Strings;
import lombok.Data;

import java.util.List;

/**
 * 补全上下文是按下 Tab 或通过其他类似的方式进行代码补全时
 * 自动提示的上下文，主要内容是当前输入的内容以及所对应位置
 * 参数的相关定义信息。
 *
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class CompleteContext
        extends CommanderContext
        implements CommandSenderHolder {

    protected final Object commandSender;
    protected final String string;
    protected final CommandTreeFork commandTreeFork;
    protected final List<String> arguments;

    public CompleteContext(Commander commander, Object commandSender, List<String> arguments, CommandTreeFork commandTreeFork, String string) {
        super(commander);

        Preconditions.objectNonNull(commandTreeFork, "command tree forks");
        Preconditions.objectNonNull(arguments, "arguments");
        Preconditions.objectNonNull(string, "string");

        this.commandSender = commandSender;
        this.arguments = arguments;
        this.string = string;
        this.commandTreeFork = commandTreeFork;
    }

    public boolean isUncompleted() {
        return Strings.nonEmpty(string);
    }

    public String getUncompletedPart() {
        Preconditions.state(isUncompleted(), "当前并非未补全项");

        return string;
    }
}
