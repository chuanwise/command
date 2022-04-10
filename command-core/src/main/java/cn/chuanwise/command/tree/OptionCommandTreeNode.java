package cn.chuanwise.command.tree;

import cn.chuanwise.command.Commander;
import cn.chuanwise.command.command.OptionInfo;
import cn.chuanwise.command.completer.Completer;
import cn.chuanwise.command.completer.CompleterFilter;
import cn.chuanwise.command.configuration.CommanderConfiguration;
import cn.chuanwise.command.context.CompleteContext;
import cn.chuanwise.command.util.Arguments;
import cn.chuanwise.command.util.Options;
import cn.chuanwise.common.util.Joiner;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class OptionCommandTreeNode
    extends CommandTreeNode
    implements SingletonCommandTreeNode {

    protected final Set<OptionInfo> optionInfo = new HashSet<>();

    public OptionCommandTreeNode(Commander commander) {
        super(commander);
    }

    @Override
    protected Optional<Element> accept(String argument) throws Exception {
        final CommanderConfiguration.Option option = commander.getCommanderConfiguration().getOption();

        for (OptionInfo info : optionInfo) {
            final String nameAssignPrefix = option.getPrefix() + info.getName() + option.getSplitter();
            if (argument.startsWith(nameAssignPrefix)) {
                return Optional.of(new OptionElement(argument.substring(nameAssignPrefix.length()), info));
            }

            for (String alias : info.getAliases()) {
                final String aliasAssignPrefix = option.getPrefix() + alias + option.getSplitter();
                if (argument.startsWith(aliasAssignPrefix)) {
                    return Optional.of(new OptionElement(argument.substring(aliasAssignPrefix.length()), info));
                }
            }

            // 不写默认值也可以
            if (info.hasDefaultValue()) {
                final String namePrefix = option.getPrefix() + info.getName();
                if (Objects.equals(argument, namePrefix)
                || info.getAliases().contains(argument)) {
                    return Optional.of(new OptionElement(null, info));
                }
            }

        }
        return Optional.empty();
    }

    @Override
    protected Optional<Element> attempt(String argument) throws Exception {
        final CommanderConfiguration.Option option = commander.getCommanderConfiguration().getOption();

        final String prefix = option.getPrefix();
        if (!argument.startsWith(prefix)) {
            if (prefix.startsWith(argument)) {
                return Optional.of(new PlainTextElement(argument));
            } else {
                return Optional.empty();
            }
        }

        final Optional<Element> optionalElement = super.attempt(argument);
        if (optionalElement.isPresent()) {
            return optionalElement;
        } else {
            return Optional.of(new PlainTextElement(argument));
        }
    }

    @Override
    public String getSimpleFormat() {
        if (optionInfo.isEmpty()) {
            return "[-...]";
        } else {
            return Joiner.builder()
                .delimiter(" ")
                .build()
                .withAll(optionInfo, x -> "[-" + x.getName() + "]")
                .join();
        }
    }

    @Override
    public String getCompleteUsage() {
        if (optionInfo.isEmpty()) {
            return "[-...]";
        } else {
            return Joiner.builder()
                .delimiter(" ")
                .build()
                .withAll(optionInfo, x -> {
                    final StringBuilder stringBuilder = new StringBuilder(x.getName());
    
                    // | aliases
                    final Set<String> aliases = x.getAliases();
                    if (cn.chuanwise.common.util.Collections.nonEmpty(aliases)) {
                        for (String alias : aliases) {
                            stringBuilder.append("|").append(alias);
                        }
                    }
                    
                    // optional values
                    final Set<String> optionalValues = x.getOptionalValues();
                    if (cn.chuanwise.common.util.Collections.nonEmpty(optionalValues)) {
                        stringBuilder.append(
                            Joiner.builder()
                            .prefix("=")
                            .delimiter("|")
                            .build()
                            .withAll(optionalValues)
                            .join()
                        );
                    }
                    
                    // default values
                    final String defaultValue = x.getDefaultValue();
                    if (cn.chuanwise.common.util.Strings.nonEmpty(defaultValue)) {
                        stringBuilder.append("?").append(defaultValue);
                    }
                    
                    return "[-" + stringBuilder + "]";
                })
                .join();
        }
    }

    @Override
    public Set<String> complete(CompleteContext context) throws Exception {
        final CommanderConfiguration configuration = context.getCommander().getCommanderConfiguration();
        final CommanderConfiguration.Option option = configuration.getOption();
        String string = context.getString();

        // 检查是否是开头都没打完呢
        final String prefix = option.getPrefix();
        final String splitter = option.getSplitter();

        final boolean completeOptionName;
        if (!string.startsWith(prefix)) {
            // -- 都还没打完呢
            if (prefix.startsWith(string)) {
                completeOptionName = true;
            } else {
                return Collections.emptySet();
            }
        } else {
            completeOptionName = !string.contains(splitter);
        }

        // 如果 --XXX=都还没打完，则补全选项名
        final CompleterFilter completerFilter = configuration.getCompleterFilter();
        if (completeOptionName) {
            final Set<String> set = new HashSet<>();
            if (string.length() > prefix.length()) {
                final String name = string.substring(prefix.length());
                final CompleteContext completeContext = new CompleteContext(
                    commander,
                        context.getCommandSender(),
                        context.getArguments(),
                        context.getCommandTreeFork(),
                        name
                );

                for (OptionInfo info : optionInfo) {
                    if (completerFilter.filter(info.getName(), completeContext)) {
                        set.add(prefix + info.getName() + splitter);
                        if (info.hasDefaultValue()) {
                            set.add(prefix + info.getName());
                        }
                    }

                    for (String alias : info.getAliases()) {
                        if (completerFilter.filter(alias, completeContext)) {
                            set.add(prefix + alias + splitter);
                            if (info.hasDefaultValue()) {
                                set.add(prefix + alias);
                            }
                        }
                    }
                }
            } else {
                for (OptionInfo info : optionInfo) {
                    set.add(prefix + info.getName() + splitter);
                    for (String alias : info.getAliases()) {
                        set.add(prefix + alias + splitter);
                    }

                    if (info.hasDefaultValue()) {
                        set.add(prefix + info.getName());
                        for (String alias : info.getAliases()) {
                            set.add(prefix + alias);
                        }
                    }
                }
            }
            return Collections.unmodifiableSet(set);
        }

        // 接下来的 string 是 name + split + value
        final int splitIndex = string.indexOf(splitter);
        Preconditions.state(splitIndex != -1);

        // 选项名及其值
        final String optionName = string.substring(prefix.length(), splitIndex);
        final String optionValue = string.substring(splitIndex + splitter.length());

        // 更新 ctx 的内容
        context = new CompleteContext(commander, context.getCommandSender(), context.getArguments(), context.getCommandTreeFork(), optionValue);

        final Set<String> set = new HashSet<>();

        final CompleteContext finalContext = context;
        for (OptionInfo info : optionInfo) {
            // 检查是否是当前这个选项，如果不是当前这个选项则忽略
            if (!Objects.equals(optionName, info.getName()) && !info.getAliases().contains(optionName)) {
                continue;
            }

            final Set<String> values = new HashSet<>(commander.getCompleteService().complete(context));
            for (Completer completer : info.getSpecialCompleters()) {
                values.addAll(completer.complete(context));
            }

            if (cn.chuanwise.common.util.Collections.nonEmpty(info.getOptionalValues())) {
                values.addAll(info.getOptionalValues());
            }

            final Set<String> descriptions = info.getDescriptions();
            if (cn.chuanwise.common.util.Collections.nonEmpty(descriptions) && configuration.isAddReferenceDescriptionsToCompleterElements()) {
                values.addAll(descriptions);
            }

            if (info.hasDefaultValue()) {
                final String defaultValue = info.getDefaultValue();
                values.add(defaultValue);
            }

            final Iterator<String> it = values.iterator();
            while (it.hasNext()) {
                final String element = it.next();
                if (!completerFilter.filter(element, finalContext)) {
                    it.remove();
                }
            }

            final Set<String> finalArguments;
            if (configuration.isSerializeCompleterElements()) {
                finalArguments = values.stream()
                        .map(Arguments::serialize)
                        .collect(Collectors.toSet());
            } else {
                finalArguments = values;
            }
            set.addAll(Options.complete(option, info, finalArguments));
        }

        return Collections.unmodifiableSet(set);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
