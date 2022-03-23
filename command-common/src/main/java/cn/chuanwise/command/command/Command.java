package cn.chuanwise.command.command;

import cn.chuanwise.command.Commander;
import cn.chuanwise.command.configuration.CommandInfo;
import cn.chuanwise.command.format.FormatElement;
import cn.chuanwise.command.format.FormatInfo;
import cn.chuanwise.command.format.OptionReferenceFormatElement;
import cn.chuanwise.command.format.ReferenceFormatElement;
import cn.chuanwise.command.object.AbstractCommanderObject;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表示一个指令
 *
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class Command
        extends AbstractCommanderObject {
    
    /**
     * 指令名
     */
    protected String name;
    
    /**
     * 格式串
     */
    protected final List<FormatInfo> formatInfo = new ArrayList<>();
    
    /**
     * 参数信息
     */
    protected final Map<String, ParameterInfo> parameterInfo = new HashMap<>();
    
    /**
     * 属性
     */
    protected final Map<Property<?>, Object> properties = new HashMap<>();
    
    /**
     * 执行器
     */
    protected CommandExecutor executor;
    
    /**
     * 构造一个指令
     *
     * @param name 指令名
     * @param commander 所属 {@link Commander} 对象
     * @param formatInfo 格式信息
     */
    public Command(String name,
                   Commander commander,
                   List<FormatInfo> formatInfo) {
        super(commander);

        Preconditions.argumentNonEmpty(formatInfo, "format info");
        Preconditions.argumentNonEmpty(name, "command name");

        this.name = name;

        setFormatInfo(formatInfo);
    }
    
    /**
     * 修改格式信息
     *
     * @param formatInfo 格式信息
     */
    private void setFormatInfo(List<FormatInfo> formatInfo) {
        this.formatInfo.clear();
        this.formatInfo.addAll(formatInfo);

        Map<String, ParameterInfo> parameterInfo = new HashMap<>();
        for (FormatInfo info : formatInfo) {
            for (FormatElement element : info.getElements()) {
                if (element instanceof ReferenceFormatElement) {
                    final ReferenceFormatElement reference = (ReferenceFormatElement) element;

                    final ParameterInfo tempParameterInfo;
                    final String name = reference.getName();
                    if (reference instanceof OptionReferenceFormatElement) {
                        final OptionReferenceFormatElement option = (OptionReferenceFormatElement) reference;
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
    
    /**
     * 获取参数信息
     *
     * @return 参数信息
     */
    public Map<String, ParameterInfo> getParameterInfo() {
        return Collections.unmodifiableMap(parameterInfo);
    }
    
    /**
     * 获取格式字符串
     *
     * @return 格式字符串
     */
    public String getFormat() {
        return formatInfo.get(0).getSimpleFormat();
    }
    
    /**
     * 获取某参数信息
     *
     * @param name 参数名
     * @return 当存在该参数时返回参数信息，否则发牛 null
     */
    public ParameterInfo getParameterInfo(String name) {
        return parameterInfo.get(name);
    }
    
    /**
     * 构造指令信息对象
     *
     * @return 指令信息对象
     */
    public CommandInfo getCommandInfo() {
        return new CommandInfo(
                formatInfo.stream()
                        .map(FormatInfo::getFormat)
                        .collect(Collectors.toList()),
                new HashMap<>(properties)
        );
    }
    
    /**
     * 设置指令信息对象
     *
     * @param commandInfo 指令信息对象
     */
    public void setCommandInfo(CommandInfo commandInfo) {
        Preconditions.namedArgumentNonNull(commandInfo, "command info");

        final List<String> formats = commandInfo.getFormats();
        final List<FormatInfo> newFormatInfo = new ArrayList<>(formats.size());
        for (String format : formats) {
            final FormatInfo formatInfo;
            try {
                formatInfo = commander.getCommanderConfiguration().getFormatCompiler().compile(format);
            } catch (Exception e) {
                commander.handleException(e);
                return;
            }
            newFormatInfo.add(formatInfo);
        }
        setFormatInfo(newFormatInfo);

        properties.clear();
        properties.putAll(commandInfo.getProperties());
    }
    
    /**
     * 获取属性值
     *
     * @param property 属性类型
     * @param <T> 属性值类型
     * @return 该属性值，或 null
     */
    @SuppressWarnings("all")
    public <T> T getProperty(Property<T> property) {
        Preconditions.namedArgumentNonNull(property, "property");

        return (T) properties.get(property);
    }
    
    /**
     * 设置属性值
     *
     * @param property 属性类型
     * @param value 属性值
     * @param <T> 属性值类型
     */
    public <T> T setProperty(Property<T> property, T value) {
        Preconditions.namedArgumentNonNull(property, "property");
    
        return (T) properties.put(property, value);
    }
    
    /**
     * 获取所有属性
     *
     * @return 属性
     */
    public Map<Property<?>, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }
    
    /**
     * 删除属性值
     *
     * @param property 属性类型
     * @param <T> 属性值类型
     * @return 被删除的属性值
     */
    @SuppressWarnings("all")
    public <T> T removeProperty(Property<T> property) {
        Preconditions.namedArgumentNonNull(property, "property");

        return (T) properties.remove(property);
    }
    
    /**
     * 清空属性值
     */
    public void clearProperties() {
        properties.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
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
