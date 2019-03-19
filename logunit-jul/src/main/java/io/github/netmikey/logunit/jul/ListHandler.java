package io.github.netmikey.logunit.jul;

import java.util.Spliterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * A {@link Handler} implementation that captures {@link LogRecord}s in a
 * thread-safe list.
 */
public class ListHandler extends Handler {

    private ConcurrentLinkedQueue<LogRecord> list = new ConcurrentLinkedQueue<>();

    @Override
    public void publish(LogRecord record) {
        list.add(record);
    }

    @Override
    public void flush() {
        // Nothing to do.
    }

    @Override
    public void close() throws SecurityException {
        // Nothing to do.
    }

    /**
     * Get the items.
     * 
     * @return A {@link Spliterator} over the items.
     */
    public Spliterator<LogRecord> spliterator() {
        return list.spliterator();
    }
}
