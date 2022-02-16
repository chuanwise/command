package cn.chuanwise.commandlib.test;

import cn.chuanwise.commandlib.util.Arguments;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SerializeTest {
    @Test
    void testSerialize() {
        Assertions.assertEquals("qwq", Arguments.serialize("qwq"));
        Assertions.assertEquals("\"qwq z\"", Arguments.serialize("qwq z"));
    }

    @Test
    void testDeserialize() {
        Assertions.assertEquals("qwq", Arguments.serialize("qwq"));
        Assertions.assertEquals("qwq z", Arguments.deserialize("\"qwq z\""));
    }
}
