package cn.chuanwise.commandlib.handler;

public class CommandLibHandlerAdapter implements CommandLibHandler {
    @Override
    public boolean handleException(Throwable cause) throws Exception {
        return false;
    }

    @Override
    public boolean handleEvent(Object event) throws Exception {
        return false;
    }
}
