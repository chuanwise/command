package cn.chuanwise.commandlib.context;

import cn.chuanwise.commandlib.command.Command;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

import java.util.Map;

@Data
public class ParserContext
        extends CommandContext {

    protected final Class<?> requiredClass;
    protected final ReferenceInfo parsingReferenceInfo;

    public ParserContext(Object commandSender,
                         Map<String, ReferenceInfo> referenceInfo,
                         Command command,
                         ReferenceInfo parsingReferenceInfo,
                         Class<?> requiredClass) {
        super(commandSender, referenceInfo, command);

        Preconditions.argumentNonNull(parsingReferenceInfo, "parsing reference info");
        Preconditions.argumentNonNull(requiredClass, "required class");

        this.parsingReferenceInfo = parsingReferenceInfo;
        this.requiredClass = requiredClass;
    }
}
