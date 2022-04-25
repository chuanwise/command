package cn.chuanwise.command.configuration;

import cn.chuanwise.command.command.Property;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 指令格式信息
 *
 * @author Chuanwise
 */
@Data
public class CommandInfo {

    protected final List<String> formats;
    
    protected final Map<Property<?>, Object> properties;

    public CommandInfo() {
        this.formats = new ArrayList<>();
        this.properties = new HashMap<>();
    }

    public CommandInfo(List<String> formats, Map<Property<?>, Object> properties) {
        Preconditions.objectNonNull(formats, "format");
        Preconditions.objectNonNull(properties, "properties");

        this.formats = formats;
        this.properties = properties;
    }
}
