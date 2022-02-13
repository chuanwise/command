package cn.chuanwise.commandlib.context;

import cn.chuanwise.commandlib.command.ParameterInfo;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

@Data
public class ReferenceInfo {

    protected final String name;
    protected final String string;

    protected final ParameterInfo parameterInfo;

    public ReferenceInfo(ParameterInfo parameterInfo, String string) {
        Preconditions.argumentNonNull(parameterInfo, "parameter info");
        Preconditions.argumentNonNull(string, "reference string");

        this.name = parameterInfo.getName();
        this.string = string;
        this.parameterInfo = parameterInfo;
    }
}
