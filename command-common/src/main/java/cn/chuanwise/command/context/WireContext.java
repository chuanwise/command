package cn.chuanwise.command.context;

import cn.chuanwise.command.command.Command;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * 装载上下文
 *
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class WireContext
        extends CommandContext {

    protected final Parameter parameter;

    public WireContext(Object commandSender,
                       Map<String, ReferenceInfo> referenceInfo,
                       Command command,
                       Parameter parameter) {
        super(commandSender, referenceInfo, command);

        Preconditions.namedArgumentNonNull(parameter, "parameter");

        this.parameter = parameter;
    }
}
