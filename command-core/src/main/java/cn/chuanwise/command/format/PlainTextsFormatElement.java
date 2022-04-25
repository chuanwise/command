package cn.chuanwise.command.format;

import cn.chuanwise.common.util.Joiner;
import lombok.Data;

import java.util.List;

@Data
public class PlainTextsFormatElement extends FormatElement {
    protected final List<String> texts;

    @Override
    public String getCompletedFormat() {
        return Joiner.builder().delimiter("|").build().plus(texts).join();
    }

    @Override
    public String getSimpleFormat() {
        return texts.get(0);
    }
}
