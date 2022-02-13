package cn.chuanwise.commandlib.tree;

import cn.chuanwise.algorithm.LongestCommonSubsequence;
import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.annotation.Description;
import cn.chuanwise.commandlib.annotation.Format;
import cn.chuanwise.commandlib.annotation.Permission;
import cn.chuanwise.commandlib.annotation.Usage;
import cn.chuanwise.commandlib.command.*;
import cn.chuanwise.commandlib.completer.Completer;
import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.commandlib.context.CompleteContext;
import cn.chuanwise.commandlib.context.DispatchContext;
import cn.chuanwise.commandlib.context.ReferenceInfo;
import cn.chuanwise.commandlib.event.*;
import cn.chuanwise.commandlib.object.SimpleCommandLibObject;
import cn.chuanwise.exception.IllegalOperationException;
import cn.chuanwise.util.Arrays;
import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.Reflects;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class CommandManager
        extends SimpleCommandLibObject {

    protected final Set<CommandTree> sons = new HashSet<>();

    public CommandManager(CommandLib commandLib) {
        super(commandLib);
    }

    public Set<CommandTree> getExecutableSubCommandTrees() {
        final Set<CommandTree> set = new HashSet<>();

        for (CommandTree son : sons) {
            set.addAll(son.getExecutableSubCommandTrees());
        }

        return Collections.unmodifiableSet(set);
    }

    public Set<Command> getCommands() {
        return Collections.unmodifiableSet(getExecutableSubCommandTrees()
                .stream()
                .map(CommandTree::getCommand)
                .collect(Collectors.toSet()));
    }

    public boolean dispatch(DispatchContext dispatchContext) {
        try {
            return dispatch0(dispatchContext);
        } catch (Exception e) {
            final CommandDispatchErrorEvent event = new CommandDispatchErrorEvent(dispatchContext, e);
            try {
                commandLib.handleEvent(event);
            } catch (Exception exc) {
                commandLib.handleException(exc);
            }
            return true;
        }
    }

    protected boolean dispatch0(DispatchContext dispatchContext) throws Exception {
        Preconditions.argumentNonNull(dispatchContext, "dispatch context");

        final List<String> arguments = dispatchContext.getArguments();
        if (arguments.isEmpty()) {
            return false;
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
                return false;
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
            return false;
        }

        // 检查唯一性
        if (forks.size() != 1) {
            commandLib.handleEvent(new MultipleCommandsMatchedEvent(dispatchContext, forks));
            return false;
        }
        final DispatchFork fork = forks.get(0);

        final CommandTree commandTree = fork.commandTree;
        final List<CommandTree.Element> elements = fork.elements;

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
        for (FormatInfo formatInfo : command.getFormatInfo()) {
            final FormatInfo.Element[] formatInfoElements = formatInfo.getElements();
            boolean matched = true;
            for (int i = 0; i < formatInfoElements.length; i++) {
                final FormatInfo.Element formatInfoElement = formatInfoElements[i];

                if (i >= elements.size()) {
                    if (!(formatInfoElement instanceof FormatInfo.Element.Reference.Remain.Nullable)) {
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
            referenceInfo.put(reference.getName(), new ReferenceInfo(parameterInfo, string));
        }

        // 处理 option
        if (i != formatInfoElements.length) {
            Preconditions.state(i == elements.size() - 1);

            // 先收集后面的 option info
            final Map<String, OptionInfo> optionInfo = new HashMap<>();
            for (int j = i; j < formatInfoElements.length; j++) {
                final FormatInfo.Element formatInfoElement = formatInfoElements[i];
                Preconditions.state(formatInfoElement instanceof FormatInfo.Element.Reference.Option);
                final FormatInfo.Element.Reference.Option option = (FormatInfo.Element.Reference.Option) formatInfoElement;

                final OptionInfo info = (OptionInfo) command.getParameterInfo(option.getName()).orElseThrow(IllegalStateException::new);
                optionInfo.put(option.getName(), info);
                for (String alias : option.getAliases()) {
                    optionInfo.put(option.getName(), info);
                }
            }

            throw new IllegalOperationException("功能尚未完成，请尝试升级到最新 CommandLib");

            // 解析输入串
//            final CommandTree.Element element = elements.get(i);
//            if (element instanceof CommandTree.ValueElement) {
//                final String string = ((CommandTree.ValueElement) element).string;
//                final List<String> parts = new ArrayList<>();
//                final CommandLibConfiguration.Option option = commandLib.getConfiguration().getOption();
//
//                // 根据开头 -- 查找
//                int position = 0;
//                int nextPosition = string.indexOf(option.getSplitter());
//                while (nextPosition != -1) {
//                    // pos -> nextPos 之间是一段输入
//
//                    nextPosition = string.indexOf(option.getPrefix(), position);
//                }
//            } else {
//                // 全部使用默认值
//
//                // 失败时
//                commandLib.handleEvent(new UnhandledCommandEvent(dispatchContext, commandTree));
//                return false;
//            }
        }

        final CommandContext commandContext = new CommandContext(dispatchContext.getCommandSender(), referenceInfo, command);
        return command.getExecutor().execute(commandContext);
    }

    public List<String> complete0(DispatchContext dispatchContext, boolean uncompleted) throws Exception {
        final Set<String> strings = complete1(dispatchContext, uncompleted);
        if (uncompleted) {
            final List<String> arguments = dispatchContext.getArguments();
            final String uncompletedPart = arguments.get(arguments.size() - 1);

            return strings.stream()
                    .sorted((l, r) -> {
                        l = l.toLowerCase();
                        r = r.toLowerCase();

                        final int left = LongestCommonSubsequence.dp(uncompletedPart, l);
                        final int right = LongestCommonSubsequence.dp(uncompletedPart, r);

                        // 绝对数量 > 字典序 > 占比
                        final int valueCompare = Integer.compare(left, right);
                        if (valueCompare != 0) {
                            return -valueCompare;
                        }

                        return -l.compareTo(r);
                    })
                    .collect(Collectors.toList());
        } else {
            return Collections.unmodifiableList(strings.stream()
                    .sorted()
                    .collect(Collectors.toList()));
        }
    }

    public List<String> complete(DispatchContext dispatchContext, boolean uncompleted) {
        try {
            return complete0(dispatchContext, uncompleted);
        } catch (Exception exception) {
            final CompleteErrorEvent event = new CompleteErrorEvent(dispatchContext);
            try {
                commandLib.handleEvent(event);
            } catch (Exception e) {
                commandLib.handleException(e);
            }
            return Collections.emptyList();
        }
    }

    protected Set<String> complete1(DispatchContext dispatchContext, boolean uncompleted) throws Exception {
        Preconditions.argumentNonNull(dispatchContext, "dispatch context");

        final Set<String> set = new HashSet<>();
        final List<String> arguments = dispatchContext.getArguments();
        if (arguments.isEmpty() || (uncompleted && arguments.size() == 1)) {
            final String string;
            if (arguments.isEmpty()) {
                string = "";
            } else {
                string = arguments.get(0);
            }
            final CompleteContext context = new CompleteContext(commandLib, string);
            for (CommandTree son : sons) {
                set.addAll(son.complete(context));
            }
            return set;
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
                break;
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

        final CompleteContext completeContext = new CompleteContext(commandLib, uncompleted ? arguments.get(arguments.size() - 1) : "");
        final List<DispatchFork> parentForks = uncompleted ? clonedForks : forks;
        for (DispatchFork fork : parentForks) {
            for (CommandTree son : fork.getCommandTree().getSons()) {
                set.addAll(son.complete(completeContext));
            }
        }
        return set;
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
                    final FormatInfo.Element.Reference.Simple simple = (FormatInfo.Element.Reference.Simple) element;

                    final SimpleParameterCommandTree commandTree = this.commandTree.createSimpleParameterSon();
                    this.commandTree = commandTree;
                    commandTree.getParameterInfo().add(parameterInfo);
                    return;
                }
                if (element instanceof FormatInfo.Element.Reference.Remain) {
                    if (element instanceof FormatInfo.Element.Reference.Remain.Nullable) {
                        final FormatInfo.Element.Reference.Remain.Nullable nullable = (FormatInfo.Element.Reference.Remain.Nullable) element;

                        final NullableRemainParameterCommandTree commandTree = this.commandTree.createNullableRemainParameterSon();
                        this.commandTree = commandTree;
                        commandTree.getParameterInfo().add(parameterInfo);
                        return;
                    }
                    if (element instanceof FormatInfo.Element.Reference.Remain.NonNull) {
                        final FormatInfo.Element.Reference.Remain.NonNull nonNull = (FormatInfo.Element.Reference.Remain.NonNull) element;

                        final NonNullRemainParameterCommandTree commandTree = this.commandTree.createNonNullRemainParameterSon();
                        this.commandTree = commandTree;
                        commandTree.getParameterInfo().add(parameterInfo);
                        return;
                    }
                }
                if (element instanceof FormatInfo.Element.Reference.Option) {
                    final FormatInfo.Element.Reference.Option option = (FormatInfo.Element.Reference.Option) element;

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

    // 找到指令所在的位置
    public CommandManager registerCommand(Command command) {
        Preconditions.argumentNonNull(command, "command");

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

                            final List<String> clonedTexts = new ArrayList<>(texts);
                            clonedTexts.removeAll(plainTexts.getTexts());

                            if (clonedTexts.isEmpty() && texts.size() == plainTexts.getTexts().size()) {
                                commandTree = son;
                                break;
                            }

                            Preconditions.state(clonedTexts.size() == texts.size(), "交错的指令分支");
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
                        commandTree = new SimpleParameterCommandTree(commandLib);
                        sons.add(commandTree);

                        commandTree.getCompleters().addAll(parameterInfo.getCompleters());
                        break;
                    }
                    if (firstElement instanceof FormatInfo.Element.Reference.Remain) {
                        Preconditions.state(sons.isEmpty(), "剩余参数不能有兄弟节点");

                        if (firstElement instanceof FormatInfo.Element.Reference.Remain.Nullable) {
                            final FormatInfo.Element.Reference.Remain.Nullable nullable = (FormatInfo.Element.Reference.Remain.Nullable) firstElement;

                            commandTree = new NullableRemainParameterCommandTree(commandLib);
                            sons.add(commandTree);

                            commandTree.getCompleters().add(Completer.of(nullable.getDefaultValue()));
                            commandTree.getCompleters().addAll(parameterInfo.getCompleters());
                            break;
                        }
                        if (firstElement instanceof FormatInfo.Element.Reference.Remain.NonNull) {
                            final FormatInfo.Element.Reference.Remain.NonNull nonNull = (FormatInfo.Element.Reference.Remain.NonNull) firstElement;

                            commandTree = new NonNullRemainParameterCommandTree(commandLib);
                            sons.add(commandTree);

                            commandTree.getCompleters().addAll(parameterInfo.getCompleters());
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
        for (CommandFork fork : forks) {
            Preconditions.stateIsNull(fork.getCommandTree().getCommand(), "指令 " + command.getUsage() + " 已被注册");

            fork.getCommandTree().setCommand(command);
        }

        return this;
    }

    public CommandManager registerCommands(Object source) {
        Preconditions.argumentNonNull(source, "commands");

        final Method[] declaredMethods = Reflects.getDeclaredMethods(source.getClass());
        for (Method method : declaredMethods) {
            final Format[] formats = method.getAnnotationsByType(Format.class);
            if (Arrays.isEmpty(formats)) {
                continue;
            }

            final Description description = method.getAnnotation(Description.class);
            final Permission permission = method.getAnnotation(Permission.class);
            final Usage usage = method.getAnnotation(Usage.class);

            final List<FormatInfo> formatInfo = Collections.unmodifiableList(Stream.of(formats)
                    .map(Format::value)
                    .map(FormatInfo::compile)
                    .collect(Collectors.toList()));

            Map<String, ParameterInfo> parameterInfo = new HashMap<>();
            for (FormatInfo info : formatInfo) {
                for (FormatInfo.Element element : info.getElements()) {
                    if (element instanceof FormatInfo.Element.Reference) {
                        final FormatInfo.Element.Reference reference = (FormatInfo.Element.Reference) element;

                        final ParameterInfo tempParameterInfo;
                        final String name = reference.getName();
                        if (reference instanceof FormatInfo.Element.Reference.Option) {
                            final FormatInfo.Element.Reference.Option option = (FormatInfo.Element.Reference.Option) reference;
                            tempParameterInfo = new OptionInfo(option.getName(), option.getAliases(), option.getOptionalValues(), option.getDefaultValue());
                        } else {
                            tempParameterInfo = new ParameterInfo(name);
                        }

                        final ParameterInfo sameNameParameterInfo = parameterInfo.get(name);
                        if (Objects.isNull(sameNameParameterInfo)) {
                            parameterInfo.put(name, tempParameterInfo);
                        } else {
                            Preconditions.state(Objects.equals(sameNameParameterInfo, tempParameterInfo), "对引用 " + name + " 出现不同类型的定义");
                        }
                    }
                }
            }

            final Command command = new Command(commandLib, formatInfo, Collections.unmodifiableMap(parameterInfo));
            command.setExecutor(new MethodCommandExecutor(command, source, method));
            registerCommand(command);

            if (Objects.nonNull(description)) {
                command.setDescription(description.value());
            }
            if (Objects.nonNull(permission)) {
                command.setPermission(permission.value());
            }
            if (Objects.nonNull(usage)) {
                command.setUsage(usage.value());
            } else {
                command.setUsage(formatInfo.get(0).getCompletedUsage());
            }
        }

        return this;
    }
}
