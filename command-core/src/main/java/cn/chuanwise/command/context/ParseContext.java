package cn.chuanwise.command.context;

import cn.chuanwise.command.command.Command;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

import java.util.Map;

/**
 * 解析上下文
 *
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class ParseContext
        extends CommandContext {

    protected final Class<?> requiredClass;
    protected final ReferenceInfo parsingReferenceInfo;

    public ParseContext(Object commandSender,
                        Map<String, ReferenceInfo> referenceInfo,
                        Command command,
                        ReferenceInfo parsingReferenceInfo,
                        Class<?> requiredClass) {
        super(commandSender, referenceInfo, command);

        Preconditions.objectNonNull(parsingReferenceInfo, "parsing reference info");
        Preconditions.objectNonNull(requiredClass, "required class");

        this.parsingReferenceInfo = parsingReferenceInfo;
        this.requiredClass = requiredClass;
    }
}
