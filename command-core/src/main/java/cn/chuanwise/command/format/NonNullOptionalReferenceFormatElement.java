package cn.chuanwise.command.format;

import lombok.Data;

@Data
public class NonNullOptionalReferenceFormatElement extends OptionalReferenceFormatElement {
    public NonNullOptionalReferenceFormatElement(String name) {
        super(name);
    }

    @Override
    public String getCompletedFormat() {
        return "[" + name + "~]";
    }

    @Override
    public String getSimpleFormat() {
        return getCompletedFormat();
    }
}
