package io.github.netmikey.logunit.api;

import java.util.List;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

/**
 * Service provider interface for the component that provides the logging
 * framework's log events to logunit.
 */
public interface LogProvider {

    /**
     * Configure the {@link LogProvider} to capture log events for the specified
     * {@link Logger} Type at the specified {@link Level}.
     * 
     * @param type
     *            The {@link Logger} Type to capture log events for.
     * @param level
     *            The Level up to which log events should be captured for the
     *            specified {@link Logger} type.
     */
    public void provideForType(Class<?> type, org.slf4j.event.Level level);

    /**
     * Configure the {@link LogProvider} to capture log events for the specified
     * named {@link Logger} at the specified {@link Level}.
     * 
     * @param name
     *            The name of the {@link Logger} to capture log events for.
     * @param level
     *            The Level up to which log events should be captured for the
     *            specified {@link Logger}.
     */
    public void provideForLogger(String name, Level level);

    /**
     * Return the captured {@link LoggingEvent}s.
     * 
     * @return The potentially empty list of {@link LoggingEvent}.
     */
    List<LoggingEvent> getEvents();

    /**
     * Callback function that gets executed before the test. Used to set up the
     * {@link LogProvider} within the logging framework.
     * 
     * @param context
     *            JUnit's {@link ExtensionContext}.
     */
    void beforeTestExecution(ExtensionContext context);

    /**
     * Callback function that gets executed afte the test. Used to reset the
     * logging framework.
     * 
     * @param context
     *            JUnit's {@link ExtensionContext}.
     */
    void afterTestExecution(ExtensionContext context);

}
