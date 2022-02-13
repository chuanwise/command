package cn.chuanwise.commandlib.test;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.annotation.Format;
import cn.chuanwise.commandlib.annotation.Reference;
import cn.chuanwise.commandlib.context.DispatchContext;
import cn.chuanwise.commandlib.tree.CommandManager;
import cn.chuanwise.util.Collections;
import org.junit.jupiter.api.Test;

public class CommandsTest {
    class Commands {
        @Format("hey")
        void hey() {
            System.out.println("hey invoked");
        }

        @Format("ha|l [arg]")
        void ha(@Reference("arg") String arg) {
            System.out.println("arg is " + arg);
        }
    }
    Commands commands = new Commands();

    @Test
    void testFormat() throws Exception {
        final CommandLib commandLib = new CommandLib();
        final CommandManager manager = commandLib.getCommandManager();

        commandLib.registerCompleter(String.class, context -> {
            return Collections.asSet("chuanwise", "is", "handsome");
        });
        manager.registerCommands(commands);

        final DispatchContext dispatchContext = new DispatchContext(commandLib, new Object(), Collections.asList("l", "ccc"));
        manager.dispatch(dispatchContext);
    }
}
