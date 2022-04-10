package cn.chuanwise.command.format;

import lombok.Data;

@Data
public abstract class ReferenceFormatElement extends FormatElement {
    protected final String name;
}
