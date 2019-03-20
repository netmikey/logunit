package io.github.netmikey.logunit.log4j2;

import java.io.Serializable;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.LogRecord;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.slf4j.event.LoggingEvent;
import org.slf4j.event.SubstituteLoggingEvent;

/**
 * An {@link Appender} implementation that captures {@link LogRecord}s in a
 * thread-safe list.
 */
public class ListAppender extends AbstractAppender {

    private ConcurrentLinkedQueue<LoggingEvent> list = new ConcurrentLinkedQueue<>();

    /**
     * Create a new instance.
     * 
     * @param name
     *            The new appender's name within the Log4j2 context.
     * 
     * @return A new instance.
     */
    public static ListAppender create(String name) {
        return new ListAppender(name, null, PatternLayout.createDefaultLayout(), true, Property.EMPTY_ARRAY);
    }

    /**
     * Overridden constructor from log4j2 superclass.
     * 
     * @param name
     *            The Appender name.
     * @param filter
     *            The Filter to associate with the Appender.
     * @param layout
     *            The layout to use to format the event.
     * @param ignoreExceptions
     *            If true, exceptions will be logged and suppressed. If false
     *            errors will be logged and then passed to the application.
     * @param properties
     *            The filter properties.
     */
    protected ListAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions,
        Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }

    @Override
    public void append(LogEvent event) {
        /*
         * Log4j seems to reuse the LogEvent object in subsequent event
         * propagations. If we merely hold a reference to the event itself, we
         * will end up with all event references pointing to the last LogEvent.
         * Make sure we copy the event's values immediately.
         */
        list.add(mapEvent(event));
    }

    private LoggingEvent mapEvent(LogEvent iEvent) {
        SubstituteLoggingEvent e = new SubstituteLoggingEvent();
        e.setTimeStamp(iEvent.getTimeMillis());
        e.setThrowable(iEvent.getThrown());
        e.setThreadName(iEvent.getThreadName());
        e.setMessage(iEvent.getMessage().getFormattedMessage());
        e.setLoggerName(iEvent.getLoggerName());
        e.setLevel(LevelMapper.mapLevel(iEvent.getLevel()));
        return e;
    }

    /**
     * Get the items.
     * 
     * @return A {@link Spliterator} over the items.
     */
    public Spliterator<LoggingEvent> spliterator() {
        return list.spliterator();
    }

}
