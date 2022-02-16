package cn.chuanwise.commandlib.test;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.handler.HandlerAdapter;
import cn.chuanwise.commandlib.handler.HandlerContext;
import cn.chuanwise.commandlib.handler.Pipeline;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PipelineTest {
    @Data
    class StringHandler
            extends HandlerAdapter {

        protected final String string;

        public StringHandler(String string) {
            this.string = string;
        }

        public StringHandler() {
            this("default");
        }

        @Override
        public void handlerAdded(HandlerContext context) throws Exception {
            System.out.println(string + ": added");
        }

        @Override
        public void handlerRemoved(HandlerContext context) throws Exception {
            System.out.println(string + ": removed");
        }
    }

    @Test
    void testAdd() {
        final Pipeline pipeline = new CommandLib().pipeline();

        Assertions.assertTrue(pipeline.isEmpty());

        final StringHandler oneHandler = new StringHandler("1");
        pipeline.add(oneHandler);
        Assertions.assertEquals(1, pipeline.size());
        Assertions.assertEquals(oneHandler, pipeline.get(0));

        final StringHandler twoHandler = new StringHandler("2");
        pipeline.add(twoHandler);
        Assertions.assertEquals(2, pipeline.size());
        Assertions.assertEquals(twoHandler, pipeline.get(1));

        pipeline.remove(0);
        Assertions.assertEquals(1, pipeline.size());
        Assertions.assertEquals(twoHandler, pipeline.get(0));

        pipeline.add(twoHandler);
        pipeline.add(twoHandler);
        pipeline.add(twoHandler);

        pipeline.remove(oneHandler);
        Assertions.assertEquals(4, pipeline.size());
    }
}
