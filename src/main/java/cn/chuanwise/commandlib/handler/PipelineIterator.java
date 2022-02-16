package cn.chuanwise.commandlib.handler;

import cn.chuanwise.util.Preconditions;

import java.util.ListIterator;
import java.util.Objects;

public class PipelineIterator
        implements ListIterator<Handler> {

    protected HandlerContext cursor;
    protected int index;

    public PipelineIterator(HandlerContext cursor, int index) {
        Preconditions.argument(index >= 0);

        this.cursor = cursor;
        this.index = index;
    }

    @Override
    public boolean hasNext() {
        return Objects.nonNull(cursor);
    }

    @Override
    public Handler next() {
        final HandlerContext cursor = this.cursor;
        this.cursor = this.cursor.next();
        index++;
        return cursor.getHandler();
    }

    @Override
    public boolean hasPrevious() {
        return Objects.nonNull(cursor) && cursor.hasPrev();
    }

    @Override
    public Handler previous() {
        final HandlerContext cursor = this.cursor;
        this.cursor = this.cursor.prev();
        index--;
        return cursor.getHandler();
    }

    @Override
    public int nextIndex() {
        return index + 1;
    }

    @Override
    public int previousIndex() {
        return 0;
    }

    @Override
    public void remove() {
        Preconditions.operationNonNull(cursor);
        cursor.remove();
    }

    @Override
    public void set(Handler handler) {
        cursor.setHandler(handler);
    }

    @Override
    public void add(Handler handler) {
        cursor.addPrev(handler);
        cursor = cursor.prev();
    }
}
