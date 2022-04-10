package cn.chuanwise.command.format;

import lombok.Data;

@Data
public class RequiredReferenceFormatElement extends ReferenceFormatElement {
    public RequiredReferenceFormatElement(String name) {
        super(name);
    }

    @Override
    public String getCompletedFormat() {
        return "[" + name + "]";
    }

    @Override
    public String getSimpleFormat() {
        return "[" + name + "]";
    }
}
