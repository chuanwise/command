package cn.chuanwise.command.command;

import cn.chuanwise.command.completer.Completer;
import lombok.Data;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 参数信息
 *
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class ParameterInfo {

    protected final String name;

    protected String defaultValue;

    protected final Set<Class<?>> requiredClass = new HashSet<>();
    
    protected final Set<Completer> specialCompleters = new HashSet<>();
    
    protected final Set<String> descriptions = new HashSet<>();

    public boolean hasDefaultValue() {
        return Objects.nonNull(defaultValue);
    }
}
