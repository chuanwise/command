package cn.chuanwise.command.tree;

import cn.chuanwise.command.Commander;
import cn.chuanwise.command.command.*;
import cn.chuanwise.command.configuration.CommanderConfiguration;
import cn.chuanwise.command.context.*;
import cn.chuanwise.command.event.*;
import cn.chuanwise.command.format.*;
import cn.chuanwise.command.object.AbstractCommanderObject;
import cn.chuanwise.common.util.Arrays;
import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.Strings;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
@SuppressWarnings("all")
public class CommandTree
        extends AbstractCommanderObject {
    
    protected final List<CommandTreeNode> sons = new ArrayList<>();
    
    public CommandTree(Commander commander) {
        super(commander);
    }
    
    public Set<CommandTreeNode> getExecutableCommandTrees() {
        final Set<CommandTreeNode> set = new HashSet<>();
        
        for (CommandTreeNode son : sons) {
            set.addAll(son.getExecutableSubCommandTrees());
        }
        
        return Collections.unmodifiableSet(set);
    }
    
    public Set<Command> getCommands() {
        return Collections.unmodifiableSet(getExecutableCommandTrees()
            .stream()
            .map(CommandTreeNode::getCommand)
            .collect(Collectors.toSet()));
    }
    
    public List<CommandTreeNode> getSons() {
        return Collections.unmodifiableList(sons);
    }
    
    public boolean removeSon(CommandTreeNode commandTreeNode) {
        return sons.remove(commandTreeNode);
    }
    
    public <T extends CommandTreeNode> List<T> addSon(T son) {
        return CommandTreeNodes.addSon(sons, son);
    }
    
    @Data
    private static class OptionInfoKey {
        
        protected final OptionInfo optionInfo;
        protected String string;
        protected boolean set;
        
        public void setString(String string) {
            this.string = string;
            set = true;
        }
    }
    
    public boolean execute(DispatchContext dispatchContext) throws Exception {
        final CommandTreeFork commandTreeFork = dispatchSerially(dispatchContext);
        if (Objects.isNull(commandTreeFork)) {
            return false;
        }
        
        final CommandTreeNode commandTreeNode = commandTreeFork.commandTreeNode;
        final List<CommandTreeNode.Element> elements = commandTreeFork.elements;
        
        // 检查指令接收器
        Command command = commandTreeNode.getCommand();
        if (Objects.isNull(command) || Objects.isNull(command.getExecutor())) {
            // 检查下一个位置是否是 NullableRemain
            if (commandTreeNode.getSons().size() == 1) {
                final CommandTreeNode son = commandTreeNode.getSons().iterator().next();
                if (son instanceof NullableOptionalParameterCommandTreeNode) {
                    command = son.getCommand();
                }
            }
        }
        if (Objects.isNull(command) || Objects.isNull(command.getExecutor())) {
            commander.getEventService().broadcastEvent(new UnhandledCommandEvent(dispatchContext, commandTreeNode));
            return false;
        }
        
        // 寻找一种匹配的组合
        FormatInfo matchedFormatInfo = null;
        if (command.getFormatInfo().size() == 1) {
            matchedFormatInfo = command.getFormatInfo().get(0);
        } else {
            for (FormatInfo formatInfo : command.getFormatInfo()) {
                final FormatElement[] formatInfoElements = formatInfo.getElements();
                boolean matched = true;
                for (int i = 0; i < formatInfoElements.length; i++) {
                    final FormatElement formatInfoElement = formatInfoElements[i];
    
                    if (i >= elements.size()) {
                        if (!(formatInfoElement instanceof NullableOptionalReferenceFormatElement)
                            && !(formatInfoElement instanceof OptionReferenceFormatElement)) {
                            matched = false;
                            break;
                        }
                    } else {
                        final CommandTreeNode.Element element = elements.get(i);
                        if (element instanceof CommandTreeNode.ValueElement != formatInfoElement instanceof ReferenceFormatElement) {
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
        final FormatElement[] formatInfoElements = matchedFormatInfo.getElements();
        int i = 0;
        for (; i < formatInfoElements.length; i++) {
            final FormatElement formatInfoElement = formatInfoElements[i];
            if (!(formatInfoElement instanceof ReferenceFormatElement)) {
                continue;
            }
            final ReferenceFormatElement reference = (ReferenceFormatElement) formatInfoElement;
    
            final ParameterInfo parameterInfo = command.getParameterInfo(reference.getName());
            Preconditions.stateNonNull(parameterInfo, "指令被添加在指令树上错误的位置，无法找到引用：" + reference.getName());
            if (parameterInfo instanceof OptionInfo) {
                break;
            }
    
            final String string;
            if (i >= elements.size()) {
                Preconditions.state(formatInfoElement instanceof NullableOptionalReferenceFormatElement);
                final NullableOptionalReferenceFormatElement nullable = (NullableOptionalReferenceFormatElement) formatInfoElement;
    
                string = nullable.getDefaultValue();
            } else {
                final CommandTreeNode.Element element = elements.get(i);
                Preconditions.state(element instanceof CommandTreeNode.ValueElement);
                final CommandTreeNode.ValueElement valueElement = (CommandTreeNode.ValueElement) element;
    
                string = valueElement.string;
            }
            referenceInfo.put(reference.getName(), new ReferenceInfo(parameterInfo, string));
        }
        
        // 处理 option
        if (i != formatInfoElements.length) {
            final CommanderConfiguration configuration = commander.getCommanderConfiguration();
            final CommanderConfiguration.Option option = configuration.getOption();
    
            // 先收集后面的 option info
            final Map<String, OptionInfoKey> optionInfoKeys = new HashMap<>();
            for (int j = i; j < formatInfoElements.length; j++) {
                final FormatElement formatInfoElement = formatInfoElements[j];
                Preconditions.state(formatInfoElement instanceof OptionReferenceFormatElement);
                final OptionReferenceFormatElement optionElement = (OptionReferenceFormatElement) formatInfoElement;
    
                final OptionInfo info = (OptionInfo) command.getParameterInfo(optionElement.getName());
                optionInfoKeys.put(optionElement.getName(), new OptionInfoKey(info));
            }
    
            // 对照着设置数据
            for (int j = i; j < elements.size(); j++) {
                final CommandTreeNode.Element element = elements.get(j);
                Preconditions.state(element instanceof CommandTreeNode.OptionElement);
                final CommandTreeNode.OptionElement optionElement = (CommandTreeNode.OptionElement) element;
    
                final OptionInfo optionInfo = optionElement.getOptionInfo();
                String string = optionElement.getString();
    
                final OptionInfoKey optionInfoKey = optionInfoKeys.get(optionInfo.getName());
                Preconditions.stateNonNull(optionInfoKey);
    
                if (optionInfoKey.isSet()) {
                    commander.getEventService().broadcastEvent(new ReassignOptionEvent(dispatchContext, optionInfo, optionInfoKey.getString(), string));
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
                        commander.getEventService().broadcastEvent(new LackRequiredOptionEvent(dispatchContext, command, optionInfo));
                        return false;
                    }
        
                    referenceInfo.put(optionInfo.getName(), new ReferenceInfo(optionInfo, defaultValue));
                } else {
                    final Set<String> optionalValues = optionInfo.getOptionalValues();
                    if (cn.chuanwise.common.util.Collections.nonEmpty(optionalValues)) {
                        if (!optionalValues.contains(string) && !configuration.isAllowUndefinedOptionValue()) {
                            commander.getEventService().broadcastEvent(new UndefinedOptionValueEvent(dispatchContext, optionInfo, string, command));
                            return false;
                        }
                    }
                    referenceInfo.put(optionInfo.getName(), new ReferenceInfo(optionInfo, string));
                }
            }
        }
        
        final CommandContext commandContext = new CommandContext(dispatchContext.getCommandSender(), referenceInfo, command);
        return command.getExecutor().execute(commandContext);
    }
    
    /**
     * 只调度出最可能执行的分支
     */
    public CommandTreeFork dispatchSerially(DispatchContext dispatchContext) throws Exception {
        // 剔除那些不可执行的节点
        final List<CommandTreeFork> forks = dispatch(dispatchContext);
        if (forks.isEmpty()) {
            // if failed, event will be broadcasted
//            final MismatchedFormatEvent event = new MismatchedFormatEvent(dispatchContext, forks);
//            commander.getEventService().broadcastEvent(event);
            return null;
        }
    
        final List<CommandTreeFork> copiedForks = new ArrayList<>(forks);
    
        if (copiedForks.size() != 1) {
            // 剔除不完整选项列表
            copiedForks.removeIf(x -> {
                if (x.getCommandTreeNode() instanceof OptionCommandTreeNode) {
                    final OptionCommandTreeNode commandTree = (OptionCommandTreeNode) x.getCommandTreeNode();
                    final List<CommandTreeNode.Element> elements = x.getElements();
    
                    // 收集后面的选项
                    final Set<String> optionNames = new HashSet<>();
                    for (int i = elements.size() - 1; i >= 0; i--) {
                        final CommandTreeNode.Element element = elements.get(i);
                        if (element instanceof CommandTreeNode.OptionElement) {
                            final CommandTreeNode.OptionElement optionElement = (CommandTreeNode.OptionElement) element;
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
        
            if (copiedForks.size() == 1) {
                return copiedForks.get(0);
            }
        
            // 检查强匹配或弱匹配
            // 寻找唯一强分支
            CommandTreeFork singleStrongFork = null;
            if (!commander.getCommanderConfiguration().isStrongMatch()) {
                for (CommandTreeFork fork : copiedForks) {
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
                commander.getEventService().broadcastEvent(new MultipleCommandsMatchedEvent(dispatchContext, forks));
                return null;
            } else {
                return singleStrongFork;
            }
        }
        final CommandTreeFork fork = copiedForks.get(0);
        return fork;
    }
    
    /**
     * 调度出所有可能的分支
     */
    public List<CommandTreeFork> dispatch(DispatchContext dispatchContext) throws Exception {
        Preconditions.objectNonNull(dispatchContext, "dispatch context");
    
        final List<String> arguments = dispatchContext.getArguments();
        if (arguments.isEmpty()) {
            return Collections.emptyList();
        }
    
        // 进行第一次分支
        final List<CommandTreeFork> forks = new ArrayList<>();
        final String firstArgument = arguments.get(0);
    
        for (CommandTreeNode son : sons) {
            final Optional<CommandTreeNode.Element> optionalElement = son.accept(firstArgument);
            if (optionalElement.isPresent()) {
                final CommandTreeNode.Element element = optionalElement.get();
                forks.add(new CommandTreeFork(son, element));
            }
        }
    
        // 进行后续调度
        List<CommandTreeFork> clonedForks = new ArrayList<>(forks);
        for (int i = 1; i < arguments.size(); i++) {
            final String argument = arguments.get(i);
    
            if (forks.isEmpty()) {
                commander.getEventService().broadcastEvent(new MismatchedFormatEvent(dispatchContext, clonedForks));
                return Collections.emptyList();
            }
    
            final Set<CommandTreeFork> subForks = new HashSet<>();
            clonedForks = new ArrayList<>(forks);
            for (int j = 0; j < forks.size(); j++) {
                final CommandTreeFork fork = forks.get(j);
    
                subForks.addAll(fork.accept(argument));
                if (fork.isFailed()) {
                    forks.remove(j);
                    j--;
                }
            }
    
            forks.addAll(subForks);
        }
    
        if (forks.isEmpty()) {
            commander.getEventService().broadcastEvent(new MismatchedFormatEvent(dispatchContext, clonedForks));
            return Collections.emptyList();
        }
    
        // 把所有可空孩子都放进来！
        final List<CommandTreeFork> nullableNextForks = new ArrayList<>();
        for (CommandTreeFork fork : forks) {
            for (CommandTreeNode son : fork.getCommandTreeNode().getSons()) {
                if (son instanceof NullableOptionalParameterCommandTreeNode) {
                    nullableNextForks.add(fork.forkWith(son, new CommandTreeNode.ValueElement("")));
                    continue;
                }
                if (son instanceof OptionCommandTreeNode) {
                    nullableNextForks.add(fork.fork(son));
                    continue;
                }
            }
        }
        forks.addAll(nullableNextForks);
    
        // 删除不可执行孩子
        forks.removeIf(x -> !x.commandTreeNode.isExecutable());
        if (forks.isEmpty()) {
            commander.getEventService().broadcastEvent(new MismatchedFormatEvent(dispatchContext, clonedForks));
            return Collections.emptyList();
        }
    
        return Collections.unmodifiableList(forks);
    }
    
    /**
     * 指令补全
     */
    public Set<String> complete(DispatchContext dispatchContext, boolean uncompleted) throws Exception {
        Preconditions.objectNonNull(dispatchContext, "dispatch context");
    
        final Set<String> set = new HashSet<>();
        final List<String> arguments = dispatchContext.getArguments();
        final CommanderConfiguration configuration = commander.getCommanderConfiguration();
    
        if (arguments.isEmpty() || (uncompleted && arguments.size() == 1)) {
            final String uncompletedPart;
            if (arguments.isEmpty()) {
                uncompletedPart = "";
            } else {
                uncompletedPart = arguments.get(0);
            }
        
            for (CommandTreeNode son : sons) {
                final CompleteContext context = new CompleteContext(
                    commander,
                    dispatchContext.getCommandSender(),
                    dispatchContext.getArguments(),
                    new CommandTreeFork(son),
                    uncompletedPart
                );
            
                final Set<String> complete = son.complete(context);
                if (complete.isEmpty()) {
                    continue;
                }
            
                final Set<String> strings = new HashSet<>(complete);
                final Iterator<String> it = strings.iterator();
                while (it.hasNext()) {
                    if (!configuration.getCompleterFilter().filter(it.next(), context)) {
                        it.remove();
                    }
                }
            
                set.addAll(strings);
            }
        
            return set;
        }
    
        // 进行第一次分支
        final String firstArgument = arguments.get(0);
        final List<CommandTreeFork> commandTreeForks = new ArrayList<>();
        for (CommandTreeNode son : sons) {
            final Optional<CommandTreeNode.Element> optionalElement = son.accept(firstArgument);
            if (optionalElement.isPresent()) {
                final CommandTreeNode.Element element = optionalElement.get();
                commandTreeForks.add(new CommandTreeFork(son, element));
            }
        }
    
        // 进行后续调度
        final List<CommandTreeFork> lastForks = new ArrayList<>(commandTreeForks);
        for (int i = 1; i < arguments.size(); i++) {
            final String argument = arguments.get(i);
    
            if (commandTreeForks.isEmpty()) {
                break;
            }
    
            final Set<CommandTreeFork> subForks = new HashSet<>();
            lastForks.clear();
            commandTreeForks.stream()
                .map(CommandTreeFork::clone)
                .forEach(lastForks::add);
    
            for (int j = 0; j < commandTreeForks.size(); j++) {
                final CommandTreeFork fork = commandTreeForks.get(j);
        
                subForks.addAll(fork.accept(argument));
                if (fork.isFailed()) {
                    commandTreeForks.remove(j);
                    j--;
                }
            }
    
            commandTreeForks.addAll(subForks);
        }

//        System.out.println("uncompleted = " + uncompleted + ", last forks = " + lastForks + ", command tree forks = " + dispatchForks);

//        final List<DispatchFork> forks = uncompleted ? lastForks : dispatchForks;
        final String uncompletedPart = uncompleted ? arguments.get(arguments.size() - 1) : "";
    
        final List<CommandTreeFork> finalForks = uncompleted ? lastForks : commandTreeForks;
        final List<CommandTreeFork> attemptedForks = new ArrayList<>(finalForks.size());
        for (int i = 0; i < finalForks.size(); i++) {
            final CommandTreeFork commandTreeFork = finalForks.get(i);
            attemptedForks.addAll(commandTreeFork.attempt(uncompletedPart));
    
            if (commandTreeFork.failed) {
                finalForks.remove(i);
                i--;
            }
        }
        finalForks.addAll(attemptedForks);
    
        if (finalForks.isEmpty()) {
            return Collections.emptySet();
        }
    
        final CommanderConfiguration.Option option = configuration.getOption();
        for (CommandTreeFork fork : finalForks) {
            final CommandTreeNode commandTreeNode = fork.getCommandTreeNode();
            final CompleteContext completeContext = new CompleteContext(
                commander,
                dispatchContext.getCommandSender(),
                dispatchContext.getArguments(),
                fork,
                uncompletedPart
            );
    
            final Set<String> currentComplete;
            if (commandTreeNode instanceof OptionCommandTreeNode) {
                final OptionCommandTreeNode tree = (OptionCommandTreeNode) commandTreeNode;
                final Set<String> complete = tree.complete(completeContext);
    
                // 导入选项
                final List<CommandTreeNode.Element> elements = fork.getElements();
                final List<OptionInfo> assignedOptions = new ArrayList<>();
                if (!elements.isEmpty()) {
                    final CommandTreeNode.Element lastElement = elements.get(elements.size() - 1);
                    if (!uncompleted && lastElement instanceof CommandTreeNode.OptionElement) {
                        final CommandTreeNode.OptionElement element = (CommandTreeNode.OptionElement) lastElement;
                        if (Strings.nonEmpty(element.string)) {
                            assignedOptions.add(element.optionInfo);
                        }
                    }
    
                    for (int i = elements.size() - 2; i >= 0; i--) {
                        final CommandTreeNode.Element element = elements.get(i);
                        if (element instanceof CommandTreeNode.OptionElement) {
                            final CommandTreeNode.OptionElement optionElement = (CommandTreeNode.OptionElement) element;
                            assignedOptions.add(optionElement.optionInfo);
                        } else {
                            break;
                        }
                    }
                }
    
                currentComplete = complete.stream()
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
                    .collect(Collectors.toSet());
            } else {
                currentComplete = fork.commandTreeNode.complete(completeContext);
            }
    
            final Set<String> copiedComplete = new HashSet<>(currentComplete);
            final Iterator<String> it = copiedComplete.iterator();
            while (it.hasNext()) {
                if (!configuration.getCompleterFilter().filter(it.next(), completeContext)) {
                    it.remove();
                }
            }
    
            set.addAll(copiedComplete);
        }
    
        return set;
    }
    
    public List<String> sortedComplete(DispatchContext dispatchContext, boolean uncompleted) throws Exception {
        final String uncompletedPart;
        if (uncompleted) {
            final List<String> arguments = dispatchContext.getArguments();
            Preconditions.argumentNonEmpty(arguments, "未完成项目补全时，最后一个参数应该是未完成的参数");
            uncompletedPart = arguments.get(arguments.size() - 1);
        } else {
            uncompletedPart = "";
        }
        
        return Collections.unmodifiableList(
            commander.getCommanderConfiguration().getCompleterSorter().sort(
                complete(dispatchContext, uncompleted),
                uncompletedPart
            )
        );
    }
    
    @Data
    private static class CommandRegisterFork {
        
        protected final Command command;
        protected CommandTreeNode commandTreeNode;
        
        public CommandRegisterFork(Command command, CommandTreeNode commandTreeNode) {
            Preconditions.objectNonNull(command, "command");
            Preconditions.objectNonNull(commandTreeNode, "command tree");
            
            this.commandTreeNode = commandTreeNode;
            this.command = command;
        }
        
        public List<CommandRegisterFork> accept(FormatElement element) {
            Preconditions.objectNonNull(element, "format element");
            
            if (element instanceof PlainTextsFormatElement) {
                final PlainTextsFormatElement plainTexts = (PlainTextsFormatElement) element;
                
                final List<PlainTextsCommandTreeNode> newSons = commandTreeNode.createPlainTextSon(plainTexts.getTexts());
                commandTreeNode = newSons.get(0);
                
                if (newSons.size() == 1) {
                    return Collections.emptyList();
                }
                
                final List<CommandRegisterFork> sonForks = new ArrayList<>(newSons.size() - 1);
                for (int i = 2; i < newSons.size(); i++) {
                    final PlainTextsCommandTreeNode commandTree = newSons.get(i);
                    sonForks.add(new CommandRegisterFork(command, commandTree));
                }
                return Collections.unmodifiableList(sonForks);
            }
            if (element instanceof ReferenceFormatElement) {
                final ReferenceFormatElement reference = (ReferenceFormatElement) element;
                final ParameterInfo parameterInfo = command.getParameterInfo(reference.getName());
                Preconditions.stateNonNull(parameterInfo, "意外：找不到引用 " + reference.getName() + " 的信息");
    
                if (element instanceof RequiredReferenceFormatElement) {
//                    final FormatInfo.Element.Reference.Simple simple = (FormatInfo.Element.Reference.Simple) element;
        
                    final SimpleParameterCommandTreeNode commandTree = this.commandTreeNode.createSimpleParameterSon();
                    this.commandTreeNode = commandTree;
                    commandTree.parameterInfo.add(parameterInfo);
                    return Collections.emptyList();
                }
                if (element instanceof OptionalReferenceFormatElement) {
                    if (element instanceof NullableOptionalReferenceFormatElement) {
//                        final FormatInfo.Element.Reference.Remain.Nullable nullable = (FormatInfo.Element.Reference.Remain.Nullable) element;
    
                        final NullableOptionalParameterCommandTreeNode commandTree = this.commandTreeNode.createNullableRemainParameterSon();
                        this.commandTreeNode = commandTree;
                        commandTree.parameterInfo.add(parameterInfo);
                        return Collections.emptyList();
                    }
                    if (element instanceof NonNullOptionalReferenceFormatElement) {
//                        final FormatInfo.Element.Reference.Remain.NonNull nonNull = (FormatInfo.Element.Reference.Remain.NonNull) element;
    
                        final NonNullOptionalParameterCommandTreeNode commandTree = this.commandTreeNode.createNonNullRemainParameterSon();
                        this.commandTreeNode = commandTree;
                        commandTree.parameterInfo.add(parameterInfo);
                        return Collections.emptyList();
                    }
                }
                if (element instanceof OptionReferenceFormatElement) {
//                    final FormatInfo.Element.Reference.Option option = (FormatInfo.Element.Reference.Option) element;
    
                    if (!(commandTreeNode instanceof OptionCommandTreeNode)) {
                        commandTreeNode = commandTreeNode.createOptionSon();
                    }
                    final OptionCommandTreeNode tree = (OptionCommandTreeNode) commandTreeNode;
                    Preconditions.state(parameterInfo instanceof OptionInfo, "意外：引用项 " + parameterInfo.getName() + " 不是选项");
    
                    tree.optionInfo.add((OptionInfo) parameterInfo);
                    return Collections.emptyList();
                }
            }
            
            throw new NoSuchElementException("格式信息错误：" + element);
        }
    }
    
    /**
     * 注册指令
     */
    public void registerCommand(Command command) {
        Preconditions.objectNonNull(command, "command");
    
        // 激发事件
        final CommandRegisterEvent commandRegisterEvent = new CommandRegisterEvent(command);
        commander.getEventService().broadcastEvent(commandRegisterEvent);
    
        if (commandRegisterEvent.isCancelled()) {
            return;
        }
    
        final List<CommandRegisterFork> forks = new ArrayList<>();
        for (FormatInfo formatInfo : command.getFormatInfo()) {
    
            final List<CommandRegisterFork> thisFormatForks = new ArrayList<>();
    
            final FormatElement[] elements = formatInfo.getElements();
            Preconditions.argument(Arrays.nonEmpty(elements), "format info is empty");
            final FormatElement firstElement = elements[0];
    
            // 查找或创建第一个子节点
            do {
                if (firstElement instanceof PlainTextsFormatElement) {
                    final PlainTextsFormatElement plainTexts = (PlainTextsFormatElement) firstElement;
                    CommandTreeNodes.addSon(sons, new PlainTextsCommandTreeNode(plainTexts.getTexts(), commander))
                        .stream()
                        .map(x -> new CommandRegisterFork(command, x))
                        .forEach(thisFormatForks::add);
                    break;
                }
                if (firstElement instanceof ReferenceFormatElement) {
                    final ReferenceFormatElement reference = (ReferenceFormatElement) firstElement;
    
                    final ParameterInfo parameterInfo = command.getParameterInfo(reference.getName());
                    if (firstElement instanceof RequiredReferenceFormatElement) {
                        final RequiredReferenceFormatElement simple = (RequiredReferenceFormatElement) firstElement;
    
                        final SimpleParameterCommandTreeNode tree = CommandTreeNodes.addSon(sons, new SimpleParameterCommandTreeNode(commander)).get(0);
                        tree.parameterInfo.add(parameterInfo);
                        thisFormatForks.add(new CommandRegisterFork(command, tree));
                        break;
                    }
                    if (firstElement instanceof OptionalReferenceFormatElement) {
                        Preconditions.state(sons.isEmpty(), "剩余参数不能有兄弟节点");
    
                        if (firstElement instanceof NullableOptionalReferenceFormatElement) {
                            final NullableOptionalReferenceFormatElement nullable = (NullableOptionalReferenceFormatElement) firstElement;
        
                            final NullableOptionalParameterCommandTreeNode tree = CommandTreeNodes.addSon(sons, new NullableOptionalParameterCommandTreeNode(commander)).get(0);
                            tree.parameterInfo.add(parameterInfo);
                            thisFormatForks.add(new CommandRegisterFork(command, tree));
                            break;
                        }
                        if (firstElement instanceof NonNullOptionalReferenceFormatElement) {
                            final NonNullOptionalReferenceFormatElement nonNull = (NonNullOptionalReferenceFormatElement) firstElement;
    
                            final NonNullOptionalParameterCommandTreeNode tree = CommandTreeNodes.addSon(sons, new NonNullOptionalParameterCommandTreeNode(commander)).get(0);
                            tree.parameterInfo.add(parameterInfo);
                            thisFormatForks.add(new CommandRegisterFork(command, tree));
                            break;
                        }
                    }
                    if (firstElement instanceof OptionReferenceFormatElement) {
                        final OptionReferenceFormatElement option = (OptionReferenceFormatElement) firstElement;
                        final String name = option.getName();
                        final ParameterInfo tempParameterInfo = command.getParameterInfo(name);
                        Preconditions.stateNonNull(tempParameterInfo, "意外：找不到选项 " + name + " 的信息");
                        Preconditions.state(tempParameterInfo instanceof OptionInfo, "意外：引用项 " + name + " 不是选项");
    
                        final OptionCommandTreeNode tree = CommandTreeNodes.addSon(sons, new OptionCommandTreeNode(commander)).get(0);
                        thisFormatForks.add(new CommandRegisterFork(command, tree));
                        tree.optionInfo.add((OptionInfo) tempParameterInfo);
                    }
                }
                break;
            } while (true);
    
            // 开始分支
            for (int i = 0; i < thisFormatForks.size(); i++) {
                final CommandRegisterFork fork = thisFormatForks.get(i);
                for (int j = 1; j < elements.length; j++) {
                    final FormatElement element = elements[j];
    
                    thisFormatForks.addAll(fork.accept(element));
                }
            }
    
            forks.addAll(thisFormatForks);
        }
    
        for (CommandRegisterFork fork : forks) {
            final Command sameCommand = fork.getCommandTreeNode().getCommand();
            if (Objects.nonNull(sameCommand) && sameCommand != command) {
                throw new IllegalStateException("指令 " + command.getFormat() + " 已被注册到 " + sameCommand.getFormat());
            } else {
                fork.getCommandTreeNode().setCommand(command);
            }
        }
    }
}