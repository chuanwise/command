package cn.chuanwise.commandlib.tree;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.command.OptionInfo;
import cn.chuanwise.commandlib.completer.Completer;
import cn.chuanwise.commandlib.completer.SimpleCompleter;
import cn.chuanwise.commandlib.configuration.CommandLibConfiguration;
import cn.chuanwise.commandlib.context.CompleteContext;
import cn.chuanwise.commandlib.util.Arguments;
import cn.chuanwise.commandlib.util.Options;
import cn.chuanwise.exception.IllegalOperationException;
import cn.chuanwise.util.CollectionUtil;
import cn.chuanwise.util.Strings;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

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
        final CommandLibConfiguration.Option option = commandLib.getConfiguration().getOption();

        for (OptionInfo info : optionInfo) {
            final String namePrefix = option.getPrefix() + info.getName();
            if (argument.startsWith(namePrefix)) {
                return Optional.of(new OptionElement(argument.substring(namePrefix.length()), info));
            }

            for (String alias : info.getAliases()) {
                final String aliasPrefix = option.getPrefix() + alias;
                if (argument.startsWith(aliasPrefix)) {
                    return Optional.of(new OptionElement(argument.substring(aliasPrefix.length()), info));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public String getSimpleUsage() {
        if (optionInfo.isEmpty()) {
            return "[-...]";
        } else {
            return CollectionUtil.toString(
                    optionInfo,
                    x -> "[-" + x.getName() + "]",
                    " "
            );
        }
    }

    @Override
    public String getCompleteUsage() {
        if (optionInfo.isEmpty()) {
            return "[-...]";
        } else {
            return CollectionUtil.toString(
                    optionInfo,
                    x -> "[-"
                            + x.getName()
                            + (Optional.ofNullable(CollectionUtil.toString(x.getAliases(), "|")).orElse(""))
                            + (Optional.ofNullable(CollectionUtil.toString(x.getOptionalValues(), "|")).map(y -> "=" + y).orElse(""))
                            + (Objects.nonNull(x.getDefaultValue()) ? "?" : "")
                            + "]",
                    " "
            );
        }
    }

    @Override
    public Set<String> complete(CompleteContext context) throws Exception {
        final Set<String> set = new HashSet<>(super.complete(context));

        final CommandLibConfiguration.Option option = context.getCommandLib().getConfiguration().getOption();
        for (OptionInfo info : optionInfo) {
            final Set<String> values = new HashSet<>();
            for (Completer completer : info.getSpecialCompleters()) {
                values.addAll(completer.complete(context));
            }

            if (cn.chuanwise.util.Collections.nonEmpty(info.getOptionalValues())) {
                values.addAll(info.getOptionalValues());
            }

            if (Strings.nonEmpty(info.getDefaultValue())) {
                final String defaultValue = info.getDefaultValue();
                values.add(defaultValue);
            }

            values.add("");

            final Set<String> serializedArguments = values.stream()
                    .map(Arguments::serialize)
                    .collect(Collectors.toSet());
            set.addAll(Options.complete(option, info, serializedArguments));
        }

        return java.util.Collections.unmodifiableSet(set);
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

    @Override
    public String toString() {
        return super.toString();
    }
}
