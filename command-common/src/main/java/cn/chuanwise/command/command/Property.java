package cn.chuanwise.command.command;

import lombok.Data;

/**
 * 属性类型
 *
 * @param <T> 属性值类型
 * @author Chuanwise
 */
@Data
public class Property<T> {

    protected final String propertyName;

    @Override
    public String toString() {
        return propertyName;
    }
}
