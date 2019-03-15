package io.github.netmikey.logunit.logback;

import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.read.ListAppender;

/**
 * Unfortunately, Logback's {@link ListAppender} isn't thread safe. This is a
 * thread-safe variant based on it. Technically speaking, it doesn't use a
 * {@link List} but a {@link ConcurrentLinkedQueue} and exposes items using a
 * {@link Spliterator}.
 * 
 * @see ListAppender
 * 
 * @param <E>
 *            The list element type.
 */
public class ConcurrentListAppender<E> extends AppenderBase<E> {

    private ConcurrentLinkedQueue<E> list = new ConcurrentLinkedQueue<E>();

    protected void append(E e) {
        list.add(e);
    }

    /**
     * Get the items.
     * 
     * @return A {@link Spliterator} over the items.
     */
    public Spliterator<E> spliterator() {
        return list.spliterator();
    }
}
