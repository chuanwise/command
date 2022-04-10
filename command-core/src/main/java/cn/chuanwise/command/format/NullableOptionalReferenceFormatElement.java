package cn.chuanwise.command.format;

import lombok.Data;

@Data
public class NullableOptionalReferenceFormatElement extends OptionalReferenceFormatElement {
    protected final String defaultValue;

    public NullableOptionalReferenceFormatElement(String name, String defaultValue) {
        super(name);
        this.defaultValue = defaultValue;
    }

    @Override
    public String getCompletedFormat() {
        return "[" + name + "?" + defaultValue + "~]";
    }

    @Override
    public String getSimpleFormat() {
        return "[" + name + "]";
    }
}
