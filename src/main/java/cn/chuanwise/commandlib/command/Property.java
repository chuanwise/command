package cn.chuanwise.commandlib.command;

import lombok.Data;

@Data
public class Property<T> {

    protected final String propertyName;

    @Override
    public String toString() {
        return propertyName;
    }
}
