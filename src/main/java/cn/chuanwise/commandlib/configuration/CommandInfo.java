package cn.chuanwise.commandlib.configuration;

import cn.chuanwise.commandlib.command.Command;
import cn.chuanwise.commandlib.command.FormatInfo;
import cn.chuanwise.commandlib.command.Property;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class CommandInfo {

    protected final List<String> formats;
    protected final Map<Property<?>, Object> properties;

    public CommandInfo() {
        this.formats = new ArrayList<>();
        this.properties = new HashMap<>();
    }

    public CommandInfo(List<String> formats, Map<Property<?>, Object> properties) {
        Preconditions.argumentNonNull(formats, "format");
        Preconditions.argumentNonNull(properties, "properties");

        this.formats = formats;
        this.properties = properties;
    }
}
