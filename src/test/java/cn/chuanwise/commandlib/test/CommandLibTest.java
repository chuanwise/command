package cn.chuanwise.commandlib.test;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.annotation.*;
import cn.chuanwise.commandlib.context.CompleteContext;
import cn.chuanwise.commandlib.context.DispatchContext;
import cn.chuanwise.commandlib.context.ParserContext;
import cn.chuanwise.commandlib.tree.CommandTree;
import cn.chuanwise.commandlib.tree.Dispatcher;
import cn.chuanwise.commandlib.util.Arguments;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommandLibTest {
    int code;

    @BeforeEach
    void beforeEach() {
        code = 0;
    }

    class MergeRelatedFork {
        @Format("ha")
        void fork1() {
            code = 1;
        }

        @Format("ha|l [arg]")
        void fork2(@Refer("arg") String arg) {
            code = 2;
        }
    }

    @Test
    void testMerge() throws Exception {
        final CommandLib commandLib = new CommandLib();
        final Dispatcher dispatcher = commandLib.dispatcher();

        dispatcher.registerCommands(new MergeRelatedFork());

        dispatcher.execute(new DispatchContext(commandLib, new Object(), Collections.singletonList("ha")));
        Assertions.assertEquals(1, code);

        dispatcher.execute(new DispatchContext(commandLib, new Object(), Arrays.asList("ha", "zzz")));
        Assertions.assertEquals(2, code);
    }

    @Test
    void testMergeError() throws Exception {
        final CommandLib commandLib = new CommandLib();
        final Dispatcher dispatcher = commandLib.dispatcher();
        commandLib.getConfiguration().setMergeIntersectedForks(false);

        Assertions.assertThrows(IllegalStateException.class, () -> {
            dispatcher.registerCommands(new MergeRelatedFork());
        });
    }

    class WeekMatch {
        @Format("l")
        void fork1() {
            code = 1;
        }

        @Format("l [zzz~]")
        void fork2() {
            code = 2;
        }

        @Format("l q orz")
        void fork3() {
            code = 3;
        }
    }

    @Test
    void testWeekMatch() {
        final CommandLib commandLib = new CommandLib();
        final Dispatcher dispatcher = commandLib.dispatcher();

        dispatcher.registerCommands(new WeekMatch());

        final CommandTree commandTree = dispatcher.getSons().iterator().next();
        Assertions.assertEquals(2, commandTree.getSons().size());

        dispatcher.execute(new DispatchContext(commandLib, new Object(), Collections.singletonList("l")));
        Assertions.assertEquals(1, code);
    }

    class Option {
        @Format("test [zz] [-t] [-u|qq=zz|orr?qwq]")
        void fork(@Refer("zz") String zz,
                  @Refer("t") String t,
                  @Refer("u") String u) {
            System.out.println("zz = " + zz + ", " +
                    "t = " + t + ", " +
                    "u = " + u);
        }
    }

    @Test
    void testOption() {
        final CommandLib commandLib = new CommandLib();
        final Dispatcher dispatcher = commandLib.dispatcher();

        dispatcher.registerCommands(new Option());
        dispatcher.execute(new DispatchContext(commandLib, commandLib, Arrays.asList("test", "org", "--t=9516")));
    }

    class MultipleOptions {
        @Format("bl [-t] [-s]")
        void fork1(@Refer("t") String t) {
            code = 1;
            System.out.println("one method called");
        }

        @Format("bl [-t]")
        void fork2(@Refer("t") CommandLib commandLib) {
            code = 2;
            System.out.println("cmdlib = " + commandLib);
        }

        @Format("bl [name]")
        void name(@Refer("name") String name) {
            System.out.println("name is = " + name);
        }

        @Format("bl op")
        void op1() {}

        @Format("bl op 2")
        void op2() {}

        @Parser(CommandLib.class)
        CommandLib parse(ParserContext context) {
            System.out.println("parse method called, e = " + context.getParsingReferenceInfo().getString());
            return context.getCommandLib();
        }

        @Completer(String.class)
        Set<String> complete(CompleteContext context) {
            System.out.println("str cmp called!");
            return Collections.singleton("hey!");
        }

        @EventHandler
        void handle(Object evt) {
            System.err.println(evt);
        }
    }

    @Test
    void testMultipleOptions() {
        final CommandLib commandLib = new CommandLib();
        final Dispatcher dispatcher = commandLib.dispatcher();
        dispatcher.register(new MultipleOptions());

//        dispatcher.execute(new DispatchContext(commandLib, commandLib, Arguments.split("bl --t=\"dependency is handsome chuanwise\"")));
        System.out.println(dispatcher.complete(new DispatchContext(commandLib, commandLib, Arguments.split("bl ")), false));
    }
}
