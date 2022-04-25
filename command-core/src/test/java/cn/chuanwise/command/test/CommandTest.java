package cn.chuanwise.command.test;

import cn.chuanwise.command.Commander;
import cn.chuanwise.command.annotation.Command;
import cn.chuanwise.command.context.DispatchContext;
import org.junit.jupiter.api.Test;

public class CommandTest {
    
    class Commands {
        
        @Command("my command [-t?]")
        void command() {
            System.out.println("called");
        }
    }
    
    @Test
    void testCompile() {
        final Commander commander = new Commander();
        commander.register(new Commands());
    
        System.out.println(commander.execute(new DispatchContext(commander, "my command")));
    }
}
