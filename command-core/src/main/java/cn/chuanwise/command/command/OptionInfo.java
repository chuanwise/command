package cn.chuanwise.command.command;

import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

import java.util.Set;

/**
 * 选项信息
 *
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class OptionInfo
    extends ParameterInfo {

    protected final Set<String> aliases;
    protected final Set<String> optionalValues;

    public OptionInfo(String name, Set<String> aliases, Set<String> optionalValues, String defaultValue) {
        super(name);

        Preconditions.objectNonNull(aliases, "aliases");
        Preconditions.objectNonNull(optionalValues, "optional values");

        this.aliases = aliases;
        this.optionalValues = optionalValues;
        this.defaultValue = defaultValue;
    }
}
