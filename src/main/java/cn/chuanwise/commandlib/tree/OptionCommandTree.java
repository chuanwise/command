package cn.chuanwise.commandlib.tree;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.command.OptionInfo;
import cn.chuanwise.commandlib.completer.Completer;
import cn.chuanwise.commandlib.configuration.CommandLibConfiguration;
import cn.chuanwise.commandlib.context.CompleteContext;
import cn.chuanwise.exception.IllegalOperationException;
import cn.chuanwise.util.Strings;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Data
public class OptionCommandTree
        extends CommandTree
        implements SingletonCommandTree {

    protected final Set<OptionInfo> optionInfo = new HashSet<>();

    public OptionCommandTree(CommandLib commandLib) {
        super(commandLib);
    }

    @Override
    protected Optional<Element> accept(String argument) throws Exception {
        return Optional.empty();
    }

    @Override
    public String getSingleUsage() {
        return null;
    }

    @Override
    public Set<String> complete(CompleteContext context) throws Exception {
        final Set<String> set = new HashSet<>(super.complete(context));

        final CommandLibConfiguration.Option option = context.getCommandLib().getConfiguration().getOption();
        for (OptionInfo info : optionInfo) {
            for (Completer completer : info.getCompleters()) {
                for (String result : completer.complete(context)) {
                    set.add(option.getPrefix() + info.getName() + option.getSplitter() + result);
                }
            }

            if (Strings.nonEmpty(info.getDefaultValue())) {
                set.add(option.getPrefix() + info.getName());
            }
        }

        return set;
    }

    @Override
    protected SimpleParameterCommandTree createSimpleParameterSon() {
        throw new IllegalOperationException("不能在选项列表下继续添加分支");
    }

    @Override
    protected PlainTextsCommandTree createPlainTextSon(List<String> texts) {
        throw new IllegalOperationException("不能在选项列表下继续添加分支");
    }

    @Override
    protected NullableRemainParameterCommandTree createNullableRemainParameterSon() {
        throw new IllegalOperationException("不能在选项列表下继续添加分支");
    }

    @Override
    protected NonNullRemainParameterCommandTree createNonNullRemainParameterSon() {
        throw new IllegalOperationException("不能在选项列表下继续添加分支");
    }

    @Override
    protected OptionCommandTree createOptionSon() {
        throw new IllegalOperationException("不能在选项列表下继续添加分支");
    }
}
