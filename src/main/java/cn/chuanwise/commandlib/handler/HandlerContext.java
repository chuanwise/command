package cn.chuanwise.commandlib.handler;

import cn.chuanwise.util.Preconditions;

import java.util.Objects;

public class HandlerContext {

    private Pipeline pipeline;
    private HandlerContext prev, next;

    private Handler handler;

    public HandlerContext(Pipeline pipeline, Handler handler) {
        Preconditions.argumentNonNull(pipeline, "pipeline");
        Preconditions.argumentNonNull(handler, "handler");

        this.pipeline = pipeline;

        setHandler(handler);
    }

    public Pipeline pipeline() {
        return pipeline;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        Preconditions.argumentNonNull(handler, "handler");

        final Handler elderHandler = this.handler;
        if (elderHandler == handler) {
            return;
        }

        if (Objects.nonNull(elderHandler)) {
            pipeline.runCatching(() -> elderHandler.handlerRemoved(this));
        }

        pipeline.runCatching(() -> handler.handlerAdded(this));

        this.handler = handler;
    }

    public HandlerContext next() {
        return next;
    }

    public HandlerContext prev() {
        return prev;
    }

    public boolean hasNext() {
        return Objects.nonNull(next);
    }

    public boolean hasPrev() {
        return Objects.nonNull(prev);
    }

    public boolean remove() {
        if (prev == next && Objects.isNull(prev)) {
            return false;
        }

        if (isHead()) {
            pipeline.head = next;
        } else {
            prev.next = next;
        }
        if (isTail()) {
            pipeline.tail = prev;
        } else {
            next.prev = prev;
        }

        prev = null;
        next = null;

        if (Objects.nonNull(handler)) {
            pipeline.runCatching(() -> handler.handlerRemoved(this));
        }

        pipeline = null;

        return true;
    }

    public boolean isHead() {
        return Objects.isNull(prev);
    }

    public boolean isTail() {
        return Objects.isNull(next);
    }

    public HandlerContext addPrev(Handler handler) {
        Preconditions.argumentNonNull(handler, "handler");

        final HandlerContext context = new HandlerContext(pipeline, handler);
        if (isHead()) {
            this.prev = context;
            this.prev.next = this;
            pipeline.head = context;
            return context;
        }

        this.prev.next = context;
        this.prev.next.prev = this.prev;
        this.prev = this.prev.next;

        return context;
    }

    public HandlerContext addNext(Handler handler) {
        Preconditions.argumentNonNull(handler, "handler");

        final HandlerContext context = new HandlerContext(pipeline, handler);
        if (isTail()) {
            this.next = context;
            this.next.prev = this;
            pipeline.tail = context;
            return context;
        }

        this.next.prev = context;
        this.next.prev.next = prev;
        this.next = prev.next;

        return context;
    }

    public HandlerContext addLast(Handler handler) {
        final HandlerContext tail = pipeline.tail;

        final HandlerContext context = new HandlerContext(pipeline, handler);

        tail.next = context;
        tail.next.prev = tail;
        pipeline.tail = tail.next;

        return context;
    }

    public HandlerContext addFirst(Handler handler) {
        final HandlerContext head = pipeline.head;

        final HandlerContext context = new HandlerContext(pipeline, handler);

        head.prev = context;
        head.prev.next = head;
        pipeline.head = head.prev;

        return context;
    }

    /** 为孤立的节点，或不在任何管线上 */
    public boolean isSingleton() {
        return prev == next;
    }
}
