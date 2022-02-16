package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.commandlib.provider.Provider;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

@Data
public class ParseFailedEvent
        extends CommandExecuteEvent {

    protected final int parameterIndex;
    protected final Provider provider;

    public ParseFailedEvent(CommandContext commandContext, int parameterIndex, Provider provider) {
        super(commandContext);

        Preconditions.argument(parameterIndex >= 0);
        Preconditions.argumentNonNull(provider, "provider");

        this.parameterIndex = parameterIndex;
        this.provider = provider;
    }
}
