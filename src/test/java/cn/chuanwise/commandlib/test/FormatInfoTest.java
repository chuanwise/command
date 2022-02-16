package cn.chuanwise.commandlib.test;

import cn.chuanwise.commandlib.command.FormatInfo;
import org.junit.jupiter.api.Test;

public class FormatInfoTest {
    @Test
    void testCompile() {
        final String compile = "bl op [-time|t] [-date|d=true|false] [-e?qwq]";
        final FormatInfo formatInfo = FormatInfo.compile(compile);
        for (FormatInfo.Element element : formatInfo.getElements()) {
            System.out.println(element);
        }
    }
}
