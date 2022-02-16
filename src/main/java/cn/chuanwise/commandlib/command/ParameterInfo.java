package cn.chuanwise.commandlib.command;

import cn.chuanwise.commandlib.completer.Completer;
import cn.chuanwise.commandlib.completer.SimpleCompleter;
import lombok.Data;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
public class ParameterInfo {

    protected final String name;

    protected String defaultValue;

    protected final Set<Class<?>> parameterClasses = new HashSet<>();
    protected final Set<Completer> specialCompleters = new HashSet<>();
    protected String description;

    public boolean hasDefaultValue() {
        return Objects.nonNull(defaultValue);
    }
}
