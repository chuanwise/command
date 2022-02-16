package cn.chuanwise.commandlib.context;

import cn.chuanwise.commandlib.command.Command;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

import java.io.File;
import java.lang.reflect.Parameter;
import java.util.Map;

@Data
public class ProvideContext
        extends CommandContext {

    protected final Parameter parameter;

    public ProvideContext(Object commandSender,
                          Map<String, ReferenceInfo> referenceInfo,
                          Command command,
                          Parameter parameter) {
        super(commandSender, referenceInfo, command);

        Preconditions.argumentNonNull(parameter, "parameter");

        this.parameter = parameter;
    }
}
