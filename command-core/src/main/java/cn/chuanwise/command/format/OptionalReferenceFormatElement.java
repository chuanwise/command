package cn.chuanwise.command.format;

import lombok.Data;

@Data
public abstract class OptionalReferenceFormatElement extends ReferenceFormatElement {
    public OptionalReferenceFormatElement(String name) {
        super(name);
    }

}
