package cn.chuanwise.command.context;

import cn.chuanwise.command.command.ParameterInfo;
import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

/**
 * 引用信息
 *
 * @author Chuanwise
 */
@Data
public class ReferenceInfo {

    protected final String name;
    protected final String string;

    protected final ParameterInfo parameterInfo;

    public ReferenceInfo(ParameterInfo parameterInfo, String string) {
        Preconditions.objectNonNull(parameterInfo, "parameter info");
        Preconditions.objectNonNull(string, "reference string");

        this.name = parameterInfo.getName();
        this.string = string;
        this.parameterInfo = parameterInfo;
    }
}
