package cn.chuanwise.commandlib.event;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.object.SimpleCommandLibObject;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

@Data
public class UnhandledExceptionEvent
        extends SimpleCommandLibObject
        implements ErrorEvent {

    protected final Throwable cause;

    public UnhandledExceptionEvent(CommandLib commandLib, Throwable cause) {
        super(commandLib);

        Preconditions.argumentNonNull(cause, "cause");

        this.cause = cause;
    }
}
