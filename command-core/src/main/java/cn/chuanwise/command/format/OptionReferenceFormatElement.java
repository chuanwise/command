package cn.chuanwise.command.format;

import cn.chuanwise.common.util.Collections;
import lombok.Data;

import java.util.Set;

@Data
public class OptionReferenceFormatElement extends ReferenceFormatElement {
    protected final Set<String> aliases;
    protected final String defaultValue;
    protected final Set<String> optionalValues;

    public OptionReferenceFormatElement(String name, Set<String> aliases, String defaultValue, Set<String> optionalValues) {
        super(name);

        this.aliases = aliases;
        this.defaultValue = defaultValue;
        this.optionalValues = optionalValues;
    }

    @Override
    public String getCompletedFormat() {
        if (Collections.isEmpty(optionalValues)) {
            return "[-" + name + "]";
        } else {
            return "[-" + name + "=" + String.join("|", optionalValues) + "]";
        }
    }

    @Override
    public String getSimpleFormat() {
        return "[-" + name + "]";
    }
}
