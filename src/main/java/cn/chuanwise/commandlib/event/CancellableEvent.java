package cn.chuanwise.commandlib.event;

public interface CancellableEvent {
    boolean isCancelled();

    void setCancelled(boolean cancelled);
}
