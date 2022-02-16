package cn.chuanwise.commandlib.command;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.configuration.CommandInfo;
import cn.chuanwise.commandlib.object.SimpleCommandLibObject;
import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.Maps;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class Command
        extends SimpleCommandLibObject {

    protected String name;

    protected final List<FormatInfo> formatInfo = new ArrayList<>();
    protected final Map<String, ParameterInfo> parameterInfo = new HashMap<>();

    protected final Map<Property<?>, Object> properties = new HashMap<>();

    protected CommandExecutor executor;

    public Command(String name,
                   CommandLib commandLib,
                   List<FormatInfo> formatInfo) {
        super(commandLib);

        Preconditions.argumentNonEmpty(formatInfo, "format info");
        Preconditions.argumentNonEmpty(name, "command name");

        this.name = name;

        setFormatInfo(formatInfo);
    }

    private void setFormatInfo(List<FormatInfo> formatInfo) {
        this.formatInfo.clear();
        this.formatInfo.addAll(formatInfo);

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

        this.parameterInfo.clear();
        this.parameterInfo.putAll(parameterInfo);
    }

    public Map<String, ParameterInfo> getParameterInfo() {
        return Collections.unmodifiableMap(parameterInfo);
    }

    public String getFormat() {
        return formatInfo.get(0).getSimpleFormat();
    }

    public Optional<ParameterInfo> getParameterInfo(String name) {
        return Optional.ofNullable(parameterInfo.get(name));
    }

    public CommandInfo getCommandInfo() {
        return new CommandInfo(
                formatInfo.stream()
                        .map(FormatInfo::getFormat)
                        .collect(Collectors.toList()),
                new HashMap<>(properties)
        );
    }

    public void setCommandInfo(CommandInfo commandInfo) {
        Preconditions.argumentNonNull(commandInfo, "command info");

        final List<FormatInfo> newFormatInfo = commandInfo.getFormats()
                .stream()
                .map(FormatInfo::compile)
                .collect(Collectors.toList());
        setFormatInfo(newFormatInfo);

        properties.clear();
        properties.putAll(commandInfo.getProperties());
    }

    @SuppressWarnings("all")
    public <T> Container<T> getProperty(Property<T> property) {
        Preconditions.argumentNonNull(property, "property");

        return (Container<T>) Maps.get(properties, property);
    }

    public <T> void setProperty(Property<T> property, T value) {
        Preconditions.argumentNonNull(property, "property");

        properties.put(property, value);
    }

    public Map<Property<?>, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @SuppressWarnings("all")
    public <T> Container<T> removeProperty(Property<T> property) {
        Preconditions.argumentNonNull(property, "property");

        return (Container<T>) Maps.remove(properties, property);
    }

    public void clearProperties() {
        properties.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Command command = (Command) o;
        return Objects.equals(formatInfo, command.formatInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formatInfo);
    }

    @Override
    public String toString() {
        return name + " : " + getFormat();
    }
}
