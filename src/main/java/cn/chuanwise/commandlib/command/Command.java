package cn.chuanwise.commandlib.command;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.object.SimpleCommandLibObject;
import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.Strings;
import lombok.Data;

import java.util.*;

@Data
public class Command
        extends SimpleCommandLibObject {

    protected final List<FormatInfo> formatInfo;
    protected final Map<String, ParameterInfo> parameterInfo;

    protected String description, usage, permission;

    protected CommandExecutor executor;

    public Command(CommandLib commandLib,
                   List<FormatInfo> formatInfo,
                   Map<String, ParameterInfo> parameterInfo) {
        super(commandLib);

        Preconditions.argumentNonEmpty(formatInfo, "format info");

        this.formatInfo = formatInfo;
        this.parameterInfo = parameterInfo;
    }

    public Map<String, ParameterInfo> getParameterInfo() {
        return Collections.unmodifiableMap(parameterInfo);
    }

    public String getUsage() {
        if (Strings.isEmpty(usage)) {
            return formatInfo.get(0).getSimpleUsage();
        }
        return usage;
    }

    public Optional<ParameterInfo> getParameterInfo(String name) {
        return Optional.ofNullable(parameterInfo.get(name));
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
}
