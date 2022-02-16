package cn.chuanwise.commandlib.tree;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.annotation.*;
import cn.chuanwise.commandlib.annotation.EventHandler;
import cn.chuanwise.commandlib.command.*;
import cn.chuanwise.commandlib.completer.MethodCompleter;
import cn.chuanwise.commandlib.configuration.CommandInfo;
import cn.chuanwise.commandlib.configuration.CommandInfoConfiguration;
import cn.chuanwise.commandlib.configuration.CommandLibConfiguration;
import cn.chuanwise.commandlib.context.*;
import cn.chuanwise.commandlib.event.*;
import cn.chuanwise.commandlib.exception.MethodExceptionHandler;
import cn.chuanwise.commandlib.handler.HandlerAdapter;
import cn.chuanwise.commandlib.handler.Pipeline;
import cn.chuanwise.commandlib.object.SimpleCommandLibObject;
import cn.chuanwise.commandlib.parser.MethodParser;
import cn.chuanwise.commandlib.provider.MethodProvider;
import cn.chuanwise.commandlib.util.Arguments;
import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.Arrays;
import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.Reflects;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class Dispatcher
        extends SimpleCommandLibObject {

    protected final Set<CommandTree> sons = new HashSet<>();

    public Dispatcher(CommandLib commandLib) {
        super(commandLib);
    }

    public Set<CommandTree> getExecutableCommandTrees() {
        final Set<CommandTree> set = new HashSet<>();

        for (CommandTree son : sons) {
            set.addAll(son.getExecutableSubCommandTrees());
        }

        return Collections.unmodifiableSet(set);
    }

    public Set<Command> getCommands() {
        return Collections.unmodifiableSet(getExecutableCommandTrees()
                .stream()
                .map(CommandTree::getCommand)
                .collect(Collectors.toSet()));
    }

    public Set<CommandTree> getSons() {
        return Collections.unmodifiableSet(sons);
    }

    public boolean execute(DispatchContext dispatchContext) {
        try {
            return execute0(dispatchContext);
        } catch (Throwable t) {
            final CommandDispatchErrorEvent event = new CommandDispatchErrorEvent(dispatchContext, t);
            try {
                commandLib.handleEvent(event);
            } catch (Throwable cause) {
                commandLib.handleException(cause);
            }
            return true;
        }
    }

    @Data
    private static class OptionInfoKey {

        protected final OptionInfo optionInfo;
        protected String string;
    }

    protected boolean execute0(DispatchContext dispatchContext) throws Exception {
        final Optional<DispatchFork> optionalDispatchFork = dispatch0(dispatchContext);
        if (!optionalDispatchFork.isPresent()) {
            return false;
        }

        final DispatchFork dispatchFork = optionalDispatchFork.get();
        final CommandTree commandTree = dispatchFork.commandTree;
        final List<CommandTree.Element> elements = dispatchFork.elements;

        // 检查指令接收器
        Command command = commandTree.getCommand();
        if (Objects.isNull(command) || Objects.isNull(command.getExecutor())) {
            // 检查下一个位置是否是 NullableRemain
            if (commandTree.getSons().size() == 1) {
                final CommandTree son = commandTree.getSons().iterator().next();
                if (son instanceof NullableRemainParameterCommandTree) {
                    command = son.getCommand();
                }
            }
        }
        if (Objects.isNull(command) || Objects.isNull(command.getExecutor())) {
            commandLib.handleEvent(new UnhandledCommandEvent(dispatchContext, commandTree));
            return false;
        }

        // 寻找一种匹配的组合
        FormatInfo matchedFormatInfo = null;
        if (command.getFormatInfo().size() == 1) {
            matchedFormatInfo = command.getFormatInfo().get(0);
        } else {
            for (FormatInfo formatInfo : command.getFormatInfo()) {
                final FormatInfo.Element[] formatInfoElements = formatInfo.getElements();
                boolean matched = true;
                for (int i = 0; i < formatInfoElements.length; i++) {
                    final FormatInfo.Element formatInfoElement = formatInfoElements[i];

                    if (i >= elements.size()) {
                        if (!(formatInfoElement instanceof FormatInfo.Element.Reference.Remain.Nullable)
                                && !(formatInfoElement instanceof FormatInfo.Element.Reference.Option)) {
                            matched = false;
                            break;
                        }
                    } else {
                        final CommandTree.Element element = elements.get(i);
                        if (element instanceof CommandTree.ValueElement != formatInfoElement instanceof FormatInfo.Element.Reference) {
                            matched = false;
                            break;
                        }
                    }
                }

                if (matched) {
                    matchedFormatInfo = formatInfo;
                    break;
                }
            }
        }
        Preconditions.stateNonNull(matchedFormatInfo);

        // 构造调用上下文
        final Map<String, ReferenceInfo> referenceInfo = new HashMap<>();
        final FormatInfo.Element[] formatInfoElements = matchedFormatInfo.getElements();
        int i = 0;
        for (; i < formatInfoElements.length; i++) {
            final FormatInfo.Element formatInfoElement = formatInfoElements[i];
            if (!(formatInfoElement instanceof FormatInfo.Element.Reference)) {
                continue;
            }
            final FormatInfo.Element.Reference reference = (FormatInfo.Element.Reference) formatInfoElement;

            final Optional<ParameterInfo> optionalParameterInfo = command.getParameterInfo(reference.getName());
            Preconditions.state(optionalParameterInfo.isPresent(), "指令被添加在指令树上错误的位置，无法找到引用：" + reference.getName());
            final ParameterInfo parameterInfo = optionalParameterInfo.get();

            if (parameterInfo instanceof OptionInfo) {
                break;
            }

            final String string;
            if (i >= elements.size()) {
                Preconditions.state(formatInfoElement instanceof FormatInfo.Element.Reference.Remain.Nullable);
                final FormatInfo.Element.Reference.Remain.Nullable nullable = (FormatInfo.Element.Reference.Remain.Nullable) formatInfoElement;

                string = nullable.getDefaultValue();
            } else {
                final CommandTree.Element element = elements.get(i);
                Preconditions.state(element instanceof CommandTree.ValueElement);
                final CommandTree.ValueElement valueElement = (CommandTree.ValueElement) element;

                string = valueElement.string;
            }
            referenceInfo.put(reference.getName(), new ReferenceInfo(parameterInfo, Arguments.deserialize(string)));
        }

        // 处理 option
        if (i != formatInfoElements.length) {
            final CommandLibConfiguration configuration = commandLib.getConfiguration();
            final CommandLibConfiguration.Option option = configuration.getOption();

            // 先收集后面的 option info
            final Map<String, OptionInfoKey> optionInfoKeys = new HashMap<>();
            for (int j = i; j < formatInfoElements.length; j++) {
                final FormatInfo.Element formatInfoElement = formatInfoElements[j];
                Preconditions.state(formatInfoElement instanceof FormatInfo.Element.Reference.Option);
                final FormatInfo.Element.Reference.Option optionElement = (FormatInfo.Element.Reference.Option) formatInfoElement;

                final OptionInfo info = (OptionInfo) command.getParameterInfo(optionElement.getName()).orElseThrow(IllegalStateException::new);
                optionInfoKeys.put(optionElement.getName(), new OptionInfoKey(info));
            }

            // 对照着设置数据
            for (int j = i; j < elements.size(); j++) {
                final CommandTree.Element element = elements.get(j);
                Preconditions.state(element instanceof CommandTree.OptionElement);
                final CommandTree.OptionElement optionElement = (CommandTree.OptionElement) element;

                final OptionInfo optionInfo = optionElement.getOptionInfo();
                String string = optionElement.getString();

                final OptionInfoKey optionInfoKey = optionInfoKeys.get(optionInfo.getName());
                Preconditions.stateNonNull(optionInfoKey);

                if (string.startsWith(option.getSplitter())) {
                    string = string.substring(option.getSplitter().length());
                } else if (string.isEmpty()) {
                    string = optionInfo.getDefaultValue();
                } else {
                    throw new IllegalStateException();
                }

                if (Objects.nonNull(optionInfoKey.getString())) {
                    commandLib.handleEvent(new ReassignOptionEvent(dispatchContext, optionInfo, optionInfoKey.getString(), string));
                    return false;
                }

                optionInfoKey.setString(string);
            }

            // 检查是否缺少选项以及选项取值
            for (OptionInfoKey optionInfoKey : optionInfoKeys.values()) {
                final OptionInfo optionInfo = optionInfoKey.getOptionInfo();
                final String string = optionInfoKey.getString();

                if (Objects.isNull(string)) {
                    final String defaultValue = optionInfo.getDefaultValue();
                    if (Objects.isNull(defaultValue)) {
                        commandLib.handleEvent(new LackRequiredOptionEvent(dispatchContext, command, optionInfo));
                        return false;
                    }

                    referenceInfo.put(optionInfo.getName(), new ReferenceInfo(optionInfo, Arguments.deserialize(defaultValue)));
                } else {
                    final Set<String> optionalValues = optionInfo.getOptionalValues();
                    if (cn.chuanwise.util.Collections.nonEmpty(optionalValues)) {
                        if (!optionalValues.contains(string) && !configuration.isAllowUndefinedOptionValue()) {
                            commandLib.handleEvent(new UndefinedOptionValueEvent(dispatchContext, optionInfo, string, command));
                            return false;
                        }
                    }
                    referenceInfo.put(optionInfo.getName(), new ReferenceInfo(optionInfo, Arguments.deserialize(string)));
                }
            }
        }

        final CommandContext commandContext = new CommandContext(dispatchContext.getCommandSender(), referenceInfo, command);
        return command.getExecutor().execute(commandContext);
    }

    public Optional<DispatchFork> dispatch(DispatchContext dispatchContext) {
        try {
            return dispatch0(dispatchContext);
        } catch (Throwable cause) {
            commandLib.handleException(cause);
            return Optional.empty();
        }
    }

    private Optional<DispatchFork> dispatch0(DispatchContext dispatchContext) throws Exception {
        Preconditions.argumentNonNull(dispatchContext, "dispatch context");

        final List<String> arguments = dispatchContext.getArguments();
        if (arguments.isEmpty()) {
            return Optional.empty();
        }

        // 进行第一次分支
        final List<DispatchFork> forks = new ArrayList<>();
        final String firstArgument = arguments.get(0);

        for (CommandTree son : sons) {
            final Optional<CommandTree.Element> optionalElement = son.accept(firstArgument);
            if (optionalElement.isPresent()) {
                final CommandTree.Element element = optionalElement.get();
                forks.add(new DispatchFork(son, element));
            }
        }

        // 进行后续调度
        List<DispatchFork> clonedForks = new ArrayList<>(forks);
        for (int i = 1; i < arguments.size(); i++) {
            final String argument = arguments.get(i);

            if (forks.isEmpty()) {
                commandLib.handleEvent(new WrongFormatEvent(dispatchContext, clonedForks));
                return Optional.empty();
            }

            final Set<DispatchFork> subForks = new HashSet<>();
            clonedForks = new ArrayList<>(forks);
            for (int j = 0; j < forks.size(); j++) {
                final DispatchFork fork = forks.get(j);

                subForks.addAll(fork.accept(argument));
                if (fork.isFailed()) {
                    forks.remove(j);
                    j--;
                }
            }

            forks.addAll(subForks);
        }

        if (forks.isEmpty()) {
            commandLib.handleEvent(new WrongFormatEvent(dispatchContext, clonedForks));
            return Optional.empty();
        }

        // 把所有可空孩子都放进来！
        final List<DispatchFork> nullableNextForks = new ArrayList<>();
        for (DispatchFork fork : forks) {
            for (CommandTree son : fork.getCommandTree().getSons()) {
                if (son instanceof NullableRemainParameterCommandTree) {
                    nullableNextForks.add(fork.forkWith(son, new CommandTree.ValueElement("")));
                }
            }
        }
        forks.addAll(nullableNextForks);

        if (forks.size() != 1) {
            // 剔除不完整选项列表
            forks.removeIf(x -> {
                if (x.getCommandTree() instanceof OptionCommandTree) {
                    final OptionCommandTree commandTree = (OptionCommandTree) x.getCommandTree();
                    final List<CommandTree.Element> elements = x.getElements();

                    // 收集后面的选项
                    final Set<String> optionNames = new HashSet<>();
                    for (int i = elements.size() - 1; i >= 0; i--) {
                        final CommandTree.Element element = elements.get(i);
                        if (element instanceof CommandTree.OptionElement) {
                            final CommandTree.OptionElement optionElement = (CommandTree.OptionElement) element;
                            optionNames.add(optionElement.optionInfo.getName());
                        } else {
                            break;
                        }
                    }

                    // 和必要参数比对
                    final Set<OptionInfo> optionInfo = commandTree.getOptionInfo();
                    for (OptionInfo info : optionInfo) {
                        // 没有默认值也没有写，则退出
                        if (!info.hasDefaultValue() && !optionNames.contains(info.getName())) {
                            return true;
                        }
                    }
                }
                return false;
            });

            if (forks.size() == 1) {
                return Optional.of(forks.get(0));
            }

            // 检查强匹配或弱匹配
            // 寻找唯一强分支
            DispatchFork singleStrongFork = null;
            if (!commandLib.getConfiguration().isStrongMatch()) {
                for (DispatchFork fork : forks) {
                    if (fork.isStrong()) {
                        if (Objects.nonNull(singleStrongFork)) {
                            singleStrongFork = null;
                            break;
                        } else {
                            singleStrongFork = fork;
                        }
                    }
                }
            }

            if (Objects.isNull(singleStrongFork)) {
                commandLib.handleEvent(new MultipleCommandsMatchedEvent(dispatchContext, forks));
                return Optional.empty();
            } else {
                return Optional.of(singleStrongFork);
            }
        }
        final DispatchFork fork = forks.get(0);
        return Optional.of(fork);
    }


    public Set<String> complete(DispatchContext dispatchContext, boolean uncompleted) {
        try {
            return complete0(dispatchContext, uncompleted);
        } catch (Throwable t) {
            final CompleteErrorEvent completeErrorEvent = new CompleteErrorEvent(dispatchContext, t);
            try {
                commandLib.handleEvent(completeErrorEvent);
            } catch (Throwable cause) {
                commandLib.handleException(cause);
            }
            return Collections.emptySet();
        }
    }

    protected Set<String> complete0(DispatchContext dispatchContext, boolean uncompleted) throws Exception {
        Preconditions.argumentNonNull(dispatchContext, "dispatch context");

        // 这段代码将 0 分支、1 分支和多分支分开讨论
        // 但觉得好像没有必要，故注释
//        final Set<String> set = new HashSet<>();
//        final List<String> arguments = dispatchContext.getArguments();
//        if (arguments.isEmpty() || (uncompleted && arguments.size() == 1)) {
//            final String string;
//            if (arguments.isEmpty()) {
//                string = "";
//            } else {
//                string = arguments.get(0);
//            }
//
////            // 构造调度分支
////            // 无参数时，调度分支就是 sons 本身
////            final List<DispatchFork> dispatchForks = Collections.unmodifiableList(sons.stream().map(DispatchFork::new).collect(Collectors.toList()));
////            final CompleteContext context = new CompleteContext(commandLib, dispatchForks, string);
////            for (CommandTree son : sons) {
////                set.addAll(son.complete(context));
////            }
////            return set;
//        }
//
//        // 进行第一次分支
//        final List<DispatchFork> dispatchForks = new ArrayList<>();
//        final String firstArgument = arguments.get(0);
//
//        for (CommandTree son : sons) {
//            final Optional<CommandTree.Element> optionalElement = son.accept(firstArgument);
//            if (optionalElement.isPresent()) {
//                final CommandTree.Element element = optionalElement.get();
//                dispatchForks.add(new DispatchFork(son, element));
//            }
//        }
//
//        // 进行后续调度
//        List<DispatchFork> clonedForks = new ArrayList<>(dispatchForks);
//        for (int i = 1; i < arguments.size(); i++) {
//            final String argument = arguments.get(i);
//
//            if (dispatchForks.isEmpty()) {
//                break;
//            }
//
//            final Set<DispatchFork> subForks = new HashSet<>();
//            clonedForks = new ArrayList<>(dispatchForks);
//            for (int j = 0; j < dispatchForks.size(); j++) {
//                final DispatchFork fork = dispatchForks.get(j);
//
//                subForks.addAll(fork.accept(argument));
//                if (fork.isFailed()) {
//                    dispatchForks.remove(j);
//                    j--;
//                }
//            }
//
//            dispatchForks.addAll(subForks);
//        }

        final Set<String> set = new HashSet<>();
        final List<String> arguments = dispatchContext.getArguments();

        if (arguments.isEmpty() || (uncompleted && arguments.size() == 1)) {
            final String uncompletedPart;
            if (arguments.isEmpty()) {
                uncompletedPart = "";
            } else {
                uncompletedPart = arguments.get(0);
            }

            final CompleteContext completeContext = new CompleteContext(commandLib, Collections.emptyList(), uncompletedPart);
            for (CommandTree son : sons) {
                set.addAll(son.complete(completeContext));
            }

            return Collections.unmodifiableSet(set.stream()
                    .filter(x -> x.startsWith(uncompletedPart))
                    .collect(Collectors.toSet()));
        }

        // 进行第一次分支
        final String firstArgument = arguments.get(0);
        final List<DispatchFork> dispatchForks = new ArrayList<>();
        for (CommandTree son : sons) {
            final Optional<CommandTree.Element> optionalElement = son.accept(firstArgument);
            if (optionalElement.isPresent()) {
                final CommandTree.Element element = optionalElement.get();
                dispatchForks.add(new DispatchFork(son, element));
            }
        }

        // 进行后续调度
        final List<DispatchFork> lastForks = new ArrayList<>(dispatchForks);
        for (int i = 1; i < arguments.size(); i++) {
            final String argument = arguments.get(i);

            if (dispatchForks.isEmpty()) {
                break;
            }

            final Set<DispatchFork> subForks = new HashSet<>();
            lastForks.clear();
            dispatchForks.stream()
                    .map(DispatchFork::clone)
                    .forEach(lastForks::add);

            for (int j = 0; j < dispatchForks.size(); j++) {
                final DispatchFork fork = dispatchForks.get(j);

                subForks.addAll(fork.accept(argument));
                if (fork.isFailed()) {
                    dispatchForks.remove(j);
                    j--;
                }
            }

            dispatchForks.addAll(subForks);
        }

//        System.out.println("uncompleted = " + uncompleted + ", last forks = " + lastForks + ", dispatch forks = " + dispatchForks);

//        final List<DispatchFork> forks = uncompleted ? lastForks : dispatchForks;
        final List<DispatchFork> forks = uncompleted ? lastForks : dispatchForks;
        final String uncompletedPart = uncompleted ? arguments.get(arguments.size() - 1) : "";
        final CompleteContext completeContext = new CompleteContext(commandLib, forks, uncompletedPart);

        final CommandLibConfiguration.Option option = commandLib.getConfiguration().getOption();
        for (DispatchFork fork : forks) {
            final CommandTree commandTree = fork.getCommandTree();

            if (commandTree instanceof OptionCommandTree) {
                // 导入选项
                final List<OptionInfo> assignedOptions = new ArrayList<>();
                final List<CommandTree.Element> elements = fork.getElements();
                for (int i = elements.size() - 1; i >= 0; i--) {
                    final CommandTree.Element element = elements.get(i);
                    if (element instanceof CommandTree.OptionElement) {
                        final CommandTree.OptionElement optionElement = (CommandTree.OptionElement) element;
                        assignedOptions.add(optionElement.optionInfo);
                    } else {
                        break;
                    }
                }

                commandTree.complete(completeContext)
                        .stream()
                        .filter(x -> {
                            for (OptionInfo assignedOption : assignedOptions) {
                                if (x.startsWith(option.getPrefix() + assignedOption.getName())) {
                                    return false;
                                }
                                for (String alias : assignedOption.getAliases()) {
                                    if (x.startsWith(option.getPrefix() + alias)) {
                                        return false;
                                    }
                                }
                            }
                            return true;
                        })
                        .forEach(set::add);
            } else {
                for (CommandTree son : commandTree.getSons()) {
                    set.addAll(son.complete(completeContext));
                }
            }
        }

        set.removeIf(x -> !x.startsWith(uncompletedPart));

        return Collections.unmodifiableSet(set);
    }

    @Data
    private static class CommandFork {

        protected final Command command;
        protected CommandTree commandTree;

        public CommandFork(Command command, CommandTree commandTree) {
            Preconditions.argumentNonNull(command, "command");
            Preconditions.argumentNonNull(commandTree, "command tree");

            this.commandTree = commandTree;
            this.command = command;
        }

        public void accept(FormatInfo.Element element) {
            Preconditions.argumentNonNull(element, "format element");

            if (element instanceof FormatInfo.Element.PlainTexts) {
                final FormatInfo.Element.PlainTexts plainTexts = (FormatInfo.Element.PlainTexts) element;

                commandTree = commandTree.createPlainTextSon(plainTexts.getTexts());
                return;
            }
            if (element instanceof FormatInfo.Element.Reference) {
                final FormatInfo.Element.Reference reference = (FormatInfo.Element.Reference) element;
                final ParameterInfo parameterInfo = command.getParameterInfo(reference.getName())
                        .orElseThrow(() -> new IllegalStateException("意外：找不到引用 " + reference.getName() + " 的信息"));

                if (element instanceof FormatInfo.Element.Reference.Simple) {
//                    final FormatInfo.Element.Reference.Simple simple = (FormatInfo.Element.Reference.Simple) element;

                    final SimpleParameterCommandTree commandTree = this.commandTree.createSimpleParameterSon();
                    this.commandTree = commandTree;
                    commandTree.parameterInfo.add(parameterInfo);
                    return;
                }
                if (element instanceof FormatInfo.Element.Reference.Remain) {
                    if (element instanceof FormatInfo.Element.Reference.Remain.Nullable) {
//                        final FormatInfo.Element.Reference.Remain.Nullable nullable = (FormatInfo.Element.Reference.Remain.Nullable) element;

                        final NullableRemainParameterCommandTree commandTree = this.commandTree.createNullableRemainParameterSon();
                        this.commandTree = commandTree;
                        commandTree.parameterInfo.add(parameterInfo);
                        return;
                    }
                    if (element instanceof FormatInfo.Element.Reference.Remain.NonNull) {
//                        final FormatInfo.Element.Reference.Remain.NonNull nonNull = (FormatInfo.Element.Reference.Remain.NonNull) element;

                        final NonNullRemainParameterCommandTree commandTree = this.commandTree.createNonNullRemainParameterSon();
                        this.commandTree = commandTree;
                        commandTree.parameterInfo.add(parameterInfo);
                        return;
                    }
                }
                if (element instanceof FormatInfo.Element.Reference.Option) {
//                    final FormatInfo.Element.Reference.Option option = (FormatInfo.Element.Reference.Option) element;

                    if (!(commandTree instanceof OptionCommandTree)) {
                        commandTree = commandTree.createOptionSon();
                    }
                    final OptionCommandTree tree = (OptionCommandTree) commandTree;
                    Preconditions.state(parameterInfo instanceof OptionInfo, "意外：引用项 " + parameterInfo.getName() + " 不是选项");

                    tree.optionInfo.add((OptionInfo) parameterInfo);
                    return;
                }
            }

            throw new NoSuchElementException("格式信息错误：" + element);
        }
    }

    /** 注册指令 */
    public void registerCommand(Command command, CommandInfoConfiguration commandInfoConfiguration) {
        Preconditions.argumentNonNull(command, "command");
        Preconditions.argumentNonNull(commandInfoConfiguration, "command info configuration");

        final CommandLibConfiguration configuration = commandLib.getConfiguration();

        // 更新相关设置
        final Optional<CommandInfo> optionalCommandInfo = commandInfoConfiguration.getCommandInfo(command.getName());
        if (optionalCommandInfo.isPresent()) {
            final CommandInfo commandInfo = optionalCommandInfo.get();
            command.setCommandInfo(commandInfo);
        } else {
            commandInfoConfiguration.setCommandInfo(command.getName(), command.getCommandInfo());
        }

        final List<CommandFork> forks = new ArrayList<>();
        for (FormatInfo formatInfo : command.getFormatInfo()) {
            final FormatInfo.Element[] elements = formatInfo.getElements();
            Preconditions.argument(Arrays.nonEmpty(elements), "format info is empty");
            final FormatInfo.Element firstElement = elements[0];

            // 查找或创建第一个子节点
            CommandTree commandTree = null;
            do {
                if (firstElement instanceof FormatInfo.Element.PlainTexts) {
                    final FormatInfo.Element.PlainTexts plainTexts = (FormatInfo.Element.PlainTexts) firstElement;

                    for (CommandTree son : sons) {
                        if (son instanceof PlainTextsCommandTree) {
                            final PlainTextsCommandTree plainTextsCommandTree = (PlainTextsCommandTree) son;
                            final List<String> texts = plainTextsCommandTree.getTexts();

                            // 要么完全不相干，要么完全相等
                            final List<String> clonedTexts = new ArrayList<>(texts);
                            clonedTexts.removeAll(plainTexts.getTexts());

                            final boolean related = clonedTexts.size() != texts.size();
                            if (related) {
                                final boolean equals = clonedTexts.isEmpty() && texts.size() == plainTexts.getTexts().size();
                                Preconditions.state(configuration.isMergeIntersectedForks() || equals, "交错的指令分支");

                                commandTree = son;
                                break;
                            }
                        }
                    }

                    if (Objects.isNull(commandTree)) {
                        commandTree = new PlainTextsCommandTree(plainTexts.getTexts(), commandLib);
                        sons.add(commandTree);
                    }
                    break;
                }
                if (firstElement instanceof FormatInfo.Element.Reference) {
                    final FormatInfo.Element.Reference reference = (FormatInfo.Element.Reference) firstElement;

                    final ParameterInfo parameterInfo = command.getParameterInfo(reference.getName()).orElseThrow(NoSuchElementException::new);
                    if (firstElement instanceof FormatInfo.Element.Reference.Simple) {
                        final FormatInfo.Element.Reference.Simple simple = (FormatInfo.Element.Reference.Simple) firstElement;

                        Preconditions.state(sons.isEmpty(), "普通参数不能有兄弟节点");
                        final SimpleParameterCommandTree simpleParameterCommandTree = new SimpleParameterCommandTree(commandLib);
                        commandTree = simpleParameterCommandTree;
                        simpleParameterCommandTree.parameterInfo.add(parameterInfo);
                        sons.add(commandTree);
                        break;
                    }
                    if (firstElement instanceof FormatInfo.Element.Reference.Remain) {
                        Preconditions.state(sons.isEmpty(), "剩余参数不能有兄弟节点");

                        if (firstElement instanceof FormatInfo.Element.Reference.Remain.Nullable) {
                            final FormatInfo.Element.Reference.Remain.Nullable nullable = (FormatInfo.Element.Reference.Remain.Nullable) firstElement;

                            final NullableRemainParameterCommandTree nullableRemainParameterCommandTree = new NullableRemainParameterCommandTree(commandLib);
                            commandTree = nullableRemainParameterCommandTree;
                            nullableRemainParameterCommandTree.parameterInfo.add(parameterInfo);
                            sons.add(commandTree);
                            break;
                        }
                        if (firstElement instanceof FormatInfo.Element.Reference.Remain.NonNull) {
                            final FormatInfo.Element.Reference.Remain.NonNull nonNull = (FormatInfo.Element.Reference.Remain.NonNull) firstElement;

                            final NonNullRemainParameterCommandTree nonNullRemainParameterCommandTree = new NonNullRemainParameterCommandTree(commandLib);
                            commandTree = nonNullRemainParameterCommandTree;
                            nonNullRemainParameterCommandTree.parameterInfo.add(parameterInfo);
                            sons.add(commandTree);
                            break;
                        }
                    }
                    if (firstElement instanceof FormatInfo.Element.Reference.Option) {
                        Preconditions.state(sons.isEmpty(), "选项列表不能有兄弟节点");

                        final FormatInfo.Element.Reference.Option option = (FormatInfo.Element.Reference.Option) firstElement;
                        final OptionCommandTree tree = (OptionCommandTree) commandTree;
                        final String name = option.getName();
                        final Optional<ParameterInfo> optionalParameterInfo = command.getParameterInfo(name);
                        Preconditions.state(optionalParameterInfo.isPresent(), "意外：找不到选项 " + name + " 的信息");
                        final ParameterInfo tempParameterInfo = optionalParameterInfo.get();
                        Preconditions.state(tempParameterInfo instanceof OptionInfo, "意外：引用项 " + name + " 不是选项");

                        final OptionCommandTree optionCommandTree = new OptionCommandTree(commandLib);
                        commandTree = optionCommandTree;
                        optionCommandTree.getOptionInfo().add((OptionInfo) tempParameterInfo);
                        sons.add(commandTree);
                    }
                }
                break;
            } while (true);

            Preconditions.stateNonNull(commandTree);

            // 开始分支
            final CommandFork fork = new CommandFork(command, commandTree);
            for (int i = 1; i < elements.length; i++) {
                final FormatInfo.Element element = elements[i];

                fork.accept(element);
            }

            forks.add(fork);
        }

        // 设置指令
        final CommandRegisterEvent commandRegisterEvent = new CommandRegisterEvent(command);
        try {
            commandLib.handleEvent(commandRegisterEvent);
        } catch (Throwable cause) {
            commandLib.handleException(cause);
        }

        if (commandRegisterEvent.isCancelled()) {
            return;
        }

        for (CommandFork fork : forks) {
            Preconditions.stateIsNull(fork.getCommandTree().getCommand(), "指令 " + command.getFormat() + " 已被注册");

            fork.getCommandTree().setCommand(command);
        }

        return;
    }

    public void registerCommand(Command command) {
        Preconditions.argumentNonNull(command, "command");

        registerCommand(command, CommandInfoConfiguration.empty());
    }

    public void registerCommands(Object source, CommandInfoConfiguration configuration) {
        Preconditions.argumentNonNull(source, "commands");

        final Method[] declaredMethods = Reflects.getDeclaredMethods(source.getClass());
        for (Method method : declaredMethods) {
            final Format[] formats = method.getAnnotationsByType(Format.class);
            if (Arrays.isEmpty(formats)) {
                continue;
            }

            final List<FormatInfo> formatInfo = Stream.of(formats)
                    .map(Format::value)
                    .map(FormatInfo::compile)
                    .collect(Collectors.toList());

            final String commandName;
            final Name name = method.getAnnotation(Name.class);
            if (Objects.nonNull(name)) {
                commandName = name.value();
            } else {
                commandName = method.getName();
            }

            final Command command = new Command(commandName, commandLib, formatInfo);
            command.setExecutor(new MethodCommandExecutor(command, source, method));
            registerCommand(command, configuration);
        }
    }

    public void registerCommands(Object source) {
        Preconditions.argumentNonNull(source, "source");

        registerCommands(source, CommandInfoConfiguration.empty());
    }

    /** 注册解析器 */
    public void registerParsers(Object source) {
        Preconditions.argumentNonNull(source);

        final Method[] declaredMethods = Reflects.getDeclaredMethods(source.getClass());
        for (Method method : declaredMethods) {
            if (method.isAnnotationPresent(Parser.class)) {
                final MethodParser parser = new MethodParser(source, method);
                final Pipeline pipeline = commandLib.pipeline();
                pipeline.runCatching(() -> {
                    pipeline.addLast(new HandlerAdapter() {
                        @Override
                        public Container<?> parse(ParserContext context) throws Exception {
                            return parser.parse(context);
                        }
                    });
                });
            }
        }
    }

    public void registerProviders(Object source) {
        Preconditions.argumentNonNull(source);

        final Method[] declaredMethods = Reflects.getDeclaredMethods(source.getClass());
        for (Method method : declaredMethods) {
            if (method.isAnnotationPresent(Provider.class)) {
                final MethodProvider provider = new MethodProvider(source, method);
                final Pipeline pipeline = commandLib.pipeline();
                pipeline.runCatching(() -> {
                    pipeline.addLast(new HandlerAdapter() {
                        @Override
                        public Container<?> provide(ProvideContext context) throws Exception {
                            return provider.provide(context);
                        }
                    });
                });
            }
        }
    }

    public void registerCompleters(Object source) {
        Preconditions.argumentNonNull(source);

        final Method[] declaredMethods = Reflects.getDeclaredMethods(source.getClass());
        for (Method method : declaredMethods) {
            if (method.isAnnotationPresent(Completer.class)) {
                final MethodProvider provider = new MethodProvider(source, method);
                final Pipeline pipeline = commandLib.pipeline();
                pipeline.runCatching(() -> {
                    pipeline.addLast(new HandlerAdapter() {
                        @Override
                        public Container<?> provide(ProvideContext context) throws Exception {
                            return provider.provide(context);
                        }
                    });
                });
            }
        }
    }

    public void registerEventHandlers(Object source) {
        Preconditions.argumentNonNull(source);

        final Method[] declaredMethods = Reflects.getDeclaredMethods(source.getClass());
        for (Method method : declaredMethods) {
            if (method.isAnnotationPresent(EventHandler.class)) {
                final MethodEventHandler eventHandler = new MethodEventHandler(source, method);
                final Pipeline pipeline = commandLib.pipeline();
                pipeline.runCatching(() -> {
                    pipeline.addLast(new HandlerAdapter() {
                        @Override
                        public boolean handleEvent(Object event) throws Exception {
                            return eventHandler.handleEvent(event);
                        }
                    });
                });
            }
        }
    }

    public void registerExceptionHandlers(Object source) {
        Preconditions.argumentNonNull(source);

        final Method[] declaredMethods = Reflects.getDeclaredMethods(source.getClass());
        for (Method method : declaredMethods) {
            if (method.isAnnotationPresent(ExceptionHandler.class)) {
                final MethodExceptionHandler exceptionHandler = new MethodExceptionHandler(source, method);
                final Pipeline pipeline = commandLib.pipeline();
                pipeline.runCatching(() -> {
                    pipeline.addLast(new HandlerAdapter() {
                        @Override
                        public boolean handleException(Throwable cause) throws Throwable {
                            return exceptionHandler.handleException(cause);
                        }
                    });
                });
            }
        }
    }

    public void register(Object source, CommandInfoConfiguration configuration) {
        Preconditions.argumentNonNull(source, "object");
        Preconditions.argumentNonNull(source, "configuration");

        final Pipeline pipeline = commandLib.pipeline();

        final Method[] declaredMethods = Reflects.getDeclaredMethods(source.getClass());
        for (Method method : declaredMethods) {
            final Format[] formats = method.getAnnotationsByType(Format.class);
            if (Arrays.nonEmpty(formats)) {
                final List<FormatInfo> formatInfo = Stream.of(formats)
                        .map(Format::value)
                        .map(FormatInfo::compile)
                        .collect(Collectors.toList());

                final String commandName;
                final Name name = method.getAnnotation(Name.class);
                if (Objects.nonNull(name)) {
                    commandName = name.value();
                } else {
                    commandName = method.getName();
                }

                final Command command = new Command(commandName, commandLib, formatInfo);
                command.setExecutor(new MethodCommandExecutor(command, source, method));
                registerCommand(command, configuration);
            }

            if (method.isAnnotationPresent(Parser.class)) {
                final cn.chuanwise.commandlib.parser.Parser parser = new MethodParser(source, method);
                pipeline.runCatching(() -> {
                    pipeline.addLast(new HandlerAdapter() {
                        @Override
                        public Container<?> parse(ParserContext context) throws Exception {
                            return parser.parse(context);
                        }
                    });
                });
            }

            if (method.isAnnotationPresent(Provider.class)) {
                final cn.chuanwise.commandlib.provider.Provider provider = new MethodProvider(source, method);
                pipeline.runCatching(() -> {
                    pipeline.addLast(new HandlerAdapter() {
                        @Override
                        public Container<?> provide(ProvideContext context) throws Exception {
                            return provider.provide(context);
                        }
                    });
                });
            }

            if (method.getAnnotationsByType(Completer.class).length != 0) {
                final cn.chuanwise.commandlib.completer.Completer completer = new MethodCompleter(source, method);
                pipeline.runCatching(() -> {
                    pipeline.addLast(new HandlerAdapter() {
                        @Override
                        public Set<String> complete(CompleteContext context) throws Exception {
                            return completer.complete(context);
                        }
                    });
                });
            }

            if (method.isAnnotationPresent(EventHandler.class)) {
                final MethodEventHandler eventHandler = new MethodEventHandler(source, method);
                pipeline.runCatching(() -> {
                    pipeline.addLast(new HandlerAdapter() {
                        @Override
                        public boolean handleEvent(Object event) throws Exception {
                            return eventHandler.handleEvent(event);
                        }
                    });
                });
            }

            if (method.isAnnotationPresent(ExceptionHandler.class)) {
                final MethodExceptionHandler exceptionHandler = new MethodExceptionHandler(source, method);
                pipeline.runCatching(() -> {
                    pipeline.addLast(new HandlerAdapter() {
                        @Override
                        public boolean handleException(Throwable cause) throws Throwable {
                            return exceptionHandler.handleException(cause);
                        }
                    });
                });
            }
        }
    }

    public void register(Object source) {
        Preconditions.argumentNonNull(source, "object");

        register(source, CommandInfoConfiguration.empty());
    }
}
