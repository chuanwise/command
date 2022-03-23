package cn.chuanwise.command.format;

import cn.chuanwise.common.util.Arrays;
import lombok.Data;

@Data
@SuppressWarnings("all")
public class FormatInfo {

    protected final String format;
    protected final FormatElement[] elements;

    public String getCompletedFormat() {
        return Arrays.toString(elements, FormatElement::getCompletedFormat, " ");
    }

    public String getSimpleFormat() {
        return Arrays.toString(elements, FormatElement::getSimpleFormat, " ");
    }
}
