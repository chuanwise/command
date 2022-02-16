package cn.chuanwise.commandlib.handler;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.context.CommandContext;
import cn.chuanwise.commandlib.context.CompleteContext;
import cn.chuanwise.commandlib.context.ParserContext;
import cn.chuanwise.commandlib.context.ProvideContext;
import cn.chuanwise.commandlib.event.UnhandledExceptionEvent;
import cn.chuanwise.commandlib.object.SimpleCommandLibObject;
import cn.chuanwise.commandlib.provider.Provider;
import cn.chuanwise.function.ExceptionRunnable;
import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.CollectionUtil;
import cn.chuanwise.util.Preconditions;

import java.util.*;

public class Pipeline
        extends SimpleCommandLibObject
        implements List<Handler> {

    protected HandlerContext head, tail;

    public Pipeline(CommandLib commandLib) {
        super(commandLib);
    }

    @Override
    public int size() {
        HandlerContext cursor = head;
        int size = 0;
        while (Objects.nonNull(cursor)) {
            cursor = cursor.next();
            size++;
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        return Objects.isNull(head);
    }

    @Override
    public boolean contains(Object o) {
        if (Objects.isNull(head)
                || (Objects.nonNull(o) && !(o instanceof Handler))) {
            return false;
        }

        HandlerContext cursor = head;
        while (Objects.nonNull(cursor)) {
            if (Objects.equals(o, cursor.getHandler())) {
                return true;
            }
            cursor = cursor.next();
        }

        return false;
    }

    @Override
    public Iterator<Handler> iterator() {
        return new PipelineIterator(head, 0);
    }

    @Override
    public Object[] toArray() {
        return new ArrayList<>(this).toArray();
    }

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    @Override
    @SuppressWarnings("all")
    public <T> T[] toArray(T[] a) {
        // Estimate size of array; be prepared to see more or fewer elements
        int size = size();
        T[] r = a.length >= size ? a :
                (T[]) java.lang.reflect.Array
                        .newInstance(a.getClass().getComponentType(), size);
        Iterator<Handler> it = iterator();

        for (int i = 0; i < r.length; i++) {
            if (!it.hasNext()) { // fewer elements than expected
                if (a == r) {
                    r[i] = null; // null-terminate
                } else if (a.length < i) {
                    return Arrays.copyOf(r, i);
                } else {
                    System.arraycopy(r, 0, a, 0, i);
                    if (a.length > i) {
                        a[i] = null;
                    }
                }
                return a;
            }
            r[i] = (T)it.next();
        }

        if (!it.hasNext()) {
            return r;
        }

        int i = r.length;
        while (it.hasNext()) {
            int cap = r.length;
            if (i == cap) {
                int newCap = cap + (cap >> 1) + 1;
                // overflow-conscious code
                Preconditions.argument(newCap < MAX_ARRAY_SIZE);
                r = Arrays.copyOf(r, newCap);
            }
            r[i++] = (T)it.next();
        }
        // trim if overallocated
        return (i == r.length) ? r : Arrays.copyOf(r, i);
    }

    @Override
    public boolean add(Handler handler) {
        if (Objects.isNull(head)) {
            head = tail = new HandlerContext(this, handler);
        } else {
            tail.addNext(handler);
        }
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (Objects.isNull(head)
                || (Objects.nonNull(o) && !(o instanceof Handler))) {
            return false;
        }

        if (!isEmpty() && head == tail) {
            if (Objects.equals(head.getHandler(), o)) {
                head.remove();
                return true;
            } else {
                return false;
            }
        }

        HandlerContext prev = head;
        HandlerContext cursor = head.next();
        boolean removed = false;
        while (Objects.nonNull(cursor)) {
            if (Objects.equals(cursor.getHandler(), o)) {
                removed = true;

                final HandlerContext next = cursor.next();
                cursor.remove();
                if (Objects.isNull(next)) {
                    return removed;
                }

                prev = next.prev();
                cursor = next;
            } else {
                prev = cursor;
                cursor = cursor.next();
            }
        }

        return removed;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        Preconditions.argumentNonNull(c, "collection");

        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Handler> c) {
        Preconditions.argumentNonNull(c, "collection");

        c.forEach(this::add);

        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Handler> c) {
        Preconditions.index(index >= 0);
        Preconditions.argumentNonNull(c, "collection");

        if (c.isEmpty()) {
            return false;
        }

        if (Objects.isNull(head)) {
            Preconditions.index(index == 0);

            final Iterator<? extends Handler> iterator = c.iterator();
            head = tail = new HandlerContext(this, iterator.next());

            for (Handler handler : c) {
                tail.addNext(handler);
            }
        } else {
            HandlerContext next = head;
            for (int i = 0; i < index; i++) {
                Preconditions.indexNonNull(next);
                next = next.next();
            }

            for (Handler handler : c) {
                next = next.addNext(handler);
            }
        }

        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        Preconditions.argumentNonNull(c, "collection");

        if (c.isEmpty()) {
            return false;
        }

        return removeIf(c::contains);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Preconditions.argumentNonNull(c, "collection");

        if (c.isEmpty()) {
            if (!isEmpty()) {
                clear();
            }
            return true;
        }

        boolean removed = false;
        Iterator<Handler> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                removed = true;
            }
        }
        return removed;
    }

    @Override
    public void clear() {
        HandlerContext cursor = head;

        if (Objects.nonNull(cursor)) {
            final HandlerContext finalCursor = cursor;
            cursor = cursor.next();

            finalCursor.remove();
        }

        head = tail = null;
    }

    @Override
    public Handler get(int index) {
        Preconditions.index(index >= 0);

        HandlerContext cursor = head;
        while (Objects.nonNull(cursor)) {
            if (index == 0) {
                return cursor.getHandler();
            } else {
                index--;
                cursor = cursor.next();
            }
        }

        throw new IndexOutOfBoundsException();
    }

    @Override
    public Handler set(int index, Handler element) {
        Preconditions.index(index >= 0);
        Preconditions.index(!isEmpty());

        HandlerContext cursor = head;
        while (index != 0) {
            index--;
            Preconditions.indexNonNull(cursor);
            cursor = cursor.next();
        }

        cursor.setHandler(element);
        return element;
    }

    @Override
    public void add(int index, Handler element) {
        Preconditions.index(index >= 0);

        // 这种情况在下面已经处理
//        if (index == 0) {
//            head.prev = new HandlerContext(this, element);
//            head.prev.next = head;
//            head = head.prev;
//            return;
//        }

        HandlerContext next = head;
        while (index != 0) {
            index--;
            Preconditions.index(Objects.nonNull(next) || index == 0);
            next = next.next();
        }

        if (Objects.isNull(next)) {
            tail.addNext(element);
        } else {
            next.addPrev(element);
        }
    }

    @Override
    public Handler remove(int index) {
        Preconditions.index(index >= 0 && !isEmpty());

        if (head == tail) {
            Preconditions.index(index == 0);
            final HandlerContext head = this.head;
            this.head = tail = null;

            head.remove();

            return head.getHandler();
        }

        if (index == 0) {
            head.remove();
            return head.getHandler();
        }

        HandlerContext prev = head;
        while (index != 0) {
            index--;
            Preconditions.indexNonNull(prev);
            prev = prev.next();
        }

        final HandlerContext cursor = prev.next();
        Preconditions.indexNonNull(cursor);

        cursor.remove();
        return cursor.getHandler();
    }

    @Override
    public int indexOf(Object o) {
        if (isEmpty()
        || (Objects.nonNull(o) && !(o instanceof Handler))) {
            return -1;
        }

        int index = 0;
        HandlerContext cursor = head;
        while (Objects.nonNull(cursor)) {
            index++;
            if (Objects.equals(cursor.getHandler(), o)) {
                return index;
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (isEmpty()
                || (Objects.nonNull(o) && !(o instanceof Handler))) {
            return -1;
        }

        int index = 0;
        int lastIndex = -1;
        HandlerContext cursor = head;
        while (Objects.nonNull(cursor)) {
            index++;
            if (Objects.equals(cursor.getHandler(), o)) {
                lastIndex = index;
            }
        }

        return lastIndex;
    }

    @Override
    public ListIterator<Handler> listIterator() {
        return new PipelineIterator(head, 0);
    }

    @Override
    public ListIterator<Handler> listIterator(int index) {
        Preconditions.index(index >= 0);

        HandlerContext cursor = head;
        int cursorIndex = 0;

        while (cursorIndex != index) {
            cursor = cursor.next();
            Preconditions.indexNonNull(cursor);
            cursorIndex++;
        }

        return new PipelineIterator(cursor, index);
    }

    @Override
    public Pipeline subList(int fromIndex, int toIndex) {
        Preconditions.index(fromIndex >= 0 && toIndex >= fromIndex);

        HandlerContext cursor = head;
        int cursorIndex = 0;

        while (cursorIndex != fromIndex) {
            cursor = cursor.next();
            Preconditions.indexNonNull(cursor);
            cursorIndex++;
        }

        if (fromIndex == toIndex) {
            final Pipeline pipeline = new Pipeline(commandLib);
            pipeline.add(cursor.getHandler());
            return pipeline;
        }

        final Pipeline pipeline = new Pipeline(commandLib);
        while (cursorIndex != toIndex) {
            pipeline.add(cursor.getHandler());

            cursor = cursor.next();
            Preconditions.indexNonNull(cursor);
            cursorIndex++;
        }

        return pipeline;
    }

    public void addFirst(Handler handler) {
        if (isEmpty()) {
            head = tail = new HandlerContext(this, handler);
        } else {
            head.addPrev(handler);
        }
    }

    public void addLast(Handler handler) {
        if (isEmpty()) {
            head = tail = new HandlerContext(this, handler);
        } else {
            tail.addNext(handler);
        }
    }

    public boolean runCatching(ExceptionRunnable runnable) {
        Preconditions.argumentNonNull(runnable);

        try {
            runnable.exceptRun();
            return true;
        } catch (Throwable e) {
            handleException(e);
            return false;
        }
    }

    public boolean handleException(Throwable cause) {
        for (Handler handler : this) {
            try {
                if (handler.handleException(cause)) {
                    return true;
                }
            } catch (Throwable t) {
                cause = t;
            }
        }

        final UnhandledExceptionEvent event = new UnhandledExceptionEvent(commandLib, cause);
        try {
            if (handleEvent(event)) {
                return true;
            } else {
                cause.printStackTrace();
                return false;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean handleEvent(Object event) throws Exception {
        Preconditions.argumentNonNull(event, "event");

        for (Handler handler: this) {
            if (handler.handleEvent(event)) {
                return true;
            }
        }

        return false;
    }

    public Set<String> handleComplete(CompleteContext context) throws Exception {
        Preconditions.argumentNonNull(context, "complete context");

        if (isEmpty()) {
            return Collections.emptySet();
        }

        final Set<String> set = new HashSet<>();
        for (Handler handler : this) {
            set.addAll(handler.complete(context));
        }

        return Collections.unmodifiableSet(set);
    }

    public Container<?> handleParse(ParserContext context) throws Exception {
        Preconditions.argumentNonNull(context, "parser context");

        if (isEmpty()) {
            return Container.empty();
        }

        for (Handler handler : this) {
            final Container<?> parse = handler.parse(context);
            if (parse.isSet()) {
                return parse;
            }
        }

        return Container.empty();
    }

    public Container<?> handleProvide(ProvideContext context) throws Exception {
        Preconditions.argumentNonNull(context, "provide context");

        for (Handler handler : this) {
            final Container<?> container = handler.provide(context);
            if (container.isSet() && context.getParameter().getType().isInstance(container.get())) {
                return container;
            }
        }

        return Container.empty();
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        } else {
            return "[" + CollectionUtil.toString(this, ", ") + "]";
        }
    }
}
