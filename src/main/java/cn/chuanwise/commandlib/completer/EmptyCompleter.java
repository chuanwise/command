package cn.chuanwise.commandlib.completer;

import lombok.Data;

import java.util.Collections;

@Data
public class EmptyCompleter extends FixedCompleter {

    private static final EmptyCompleter INSTANCE = new EmptyCompleter();

    public static EmptyCompleter getInstance() {
        return INSTANCE;
    }

    private EmptyCompleter() {
        super(Collections.emptySet());
    }
}
