package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

@Data
public class ParseErrorEvent
        extends CommandExecuteEvent {

    protected final int parameterIndex;
    protected final Class<?> requiredClass;
    protected final Object parsedValue;

    public ParseErrorEvent(CommandContext commandContext, int parameterIndex, Class<?> requiredClass, Object parsedValue) {
        super(commandContext);

        Preconditions.argument(parameterIndex >= 0, "parameter index");
        Preconditions.argumentNonNull(requiredClass, "required class");
        Preconditions.argumentNonNull(parsedValue, "null can be assign to any class");

        this.parameterIndex = parameterIndex;
        this.requiredClass = requiredClass;
        this.parsedValue = parsedValue;
    }
}
