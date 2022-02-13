package cn.chuanwise.commandlib.command;

import cn.chuanwise.commandlib.completer.Completer;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class ParameterInfo {

    protected final String name;

    protected String defaultValue;

    protected final Set<Completer> completers = new HashSet<>();
    protected String description;
}
