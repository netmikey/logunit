package io.github.netmikey.logunit.api;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.commons.util.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

import io.github.netmikey.logunit.core.LogProviderFactorySpiLoader;

/**
 * JUnit Extension for capturing log messages. This forms the main API surface
 * test authors will interact with.
 */
public class LogCapturer implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private LogProvider logProvider;

    private Level defaultLevel = Level.INFO;

    private LogCapturer() {
        // Do not instantiate directly.
    }

    /**
     * Create a new {@link LogCapturer}.
     * 
     * @return A new log capturer.
     */
    public static LogCapturer create() {
        LogCapturer logCapturer = new LogCapturer();
        logCapturer.setLogProvider(LogProviderFactorySpiLoader.getLogProviderFactory().create());
        return logCapturer;
    }

    /**
     * Set the default log level (default: {@link Level#INFO}).
     * 
     * @param level
     *            The level to be set as the new default.
     * @return A self-reference to this {@link LogCapturer}.
     */
    public LogCapturer forLevel(Level level) {
        this.defaultLevel = level;
        return this;
    }

    /**
     * Configure this {@link LogCapturer} to capture logs written by the
     * {@link Logger} of the specified type at the default log level.
     * 
     * @param type
     *            The {@link Logger} type to capture for.
     * @return A self-reference to this {@link LogCapturer}.
     */
    public LogCapturer captureForType(Class<?> type) {
        return captureForType(type, defaultLevel);
    }

    /**
     * Configure this {@link LogCapturer} to capture logs written by the
     * {@link Logger} of the specified type at the specified log level.
     * 
     * @param type
     *            The {@link Logger} type to capture for.
     * @param level
     *            The {@link Level} up to which logs should be captured.
     * @return A self-reference to this {@link LogCapturer}.
     */
    public LogCapturer captureForType(Class<?> type, Level level) {
        logProvider.provideForType(type, level);
        return this;
    }

    /**
     * Configure this {@link LogCapturer} to capture logs written by the
     * {@link Logger} of the specified name at the default log level.
     * 
     * @param name
     *            The {@link Logger} type to capture for.
     * @return A self-reference to this {@link LogCapturer}.
     */
    public LogCapturer captureForLogger(String name) {
        return captureForLogger(name, defaultLevel);
    }

    /**
     * Configure this {@link LogCapturer} to capture logs written by the
     * {@link Logger} of the specified name at the specified log level.
     * 
     * @param name
     *            The {@link Logger} type to capture for.
     * @param level
     *            The {@link Level} up to which logs should be captured.
     * @return A self-reference to this {@link LogCapturer}.
     */
    public LogCapturer captureForLogger(String name, Level level) {
        logProvider.provideForLogger(name, level);
        return this;
    }

    /**
     * Convenience method that searches all captured {@link LoggingEvent}s'
     * messages for the specified loggingStatement. This method uses
     * {@link String#contains(CharSequence)} for matching.
     * 
     * @param loggingStatement
     *            The statement to look for.
     * @return The first {@link LoggingEvent} found that matches the
     *         loggingStatement.
     */
    public LoggingEvent assertContains(String loggingStatement) {
        return assertContains(event -> event.getMessage().contains(loggingStatement),
            "Contain the string <" + loggingStatement + ">");
    }

    /**
     * Convenience method that makes sure none of the captured
     * {@link LoggingEvent}s' messages contains the specified loggingStatement.
     * This method uses {@link String#contains(CharSequence)} for matching.
     * 
     * @param loggingStatement
     *            The statement to look for.
     */
    public void assertDoesNotContain(String loggingStatement) {
        assertDoesNotContain(event -> event.getMessage().contains(loggingStatement),
            "Contain the string <" + loggingStatement + ">");
    }

    /**
     * Convenience method that asserts none of the captured
     * {@link LoggingEvent}s match the specified predicate.
     * 
     * @param predicate
     *            The predicate to be used for filtering
     * @param message
     *            A custom message to be used if the assertion fails.
     */
    public void assertDoesNotContain(Predicate<? super LoggingEvent> predicate, String message) {
        Optional<LoggingEvent> foundStatement = getEvents().stream()
            .filter(predicate)
            .findFirst();

        foundStatement.ifPresent(event -> {
            Assertions.fail(buildPrefix(message) + "Expected not to find any predicate "
                + "match but found one in LogEvent <" + loggingEventToString(event) + ">");
        });
    }

    /**
     * Convenience method that searches all captured {@link LoggingEvent}s for
     * any match using the specified predicate for filtering.
     * 
     * @param predicate
     *            The predicate to be used for filtering
     * @param message
     *            A custom message to be used if the assertion fails.
     * @return The first {@link LoggingEvent} found that matches the
     *         loggingStatement.
     */
    public LoggingEvent assertContains(Predicate<? super LoggingEvent> predicate, String message) {
        return getEvents().stream()
            .filter(predicate)
            .findFirst()
            .orElseGet(() -> {
                Assertions.fail(buildPrefix(message) + "None of the " + size()
                    + " captured log events matched the filter predicate");
                // appeasing the compiler: this line will never be executed.
                return null;
            });
    }

    /**
     * Return all captured {@link LoggingEvent}s.
     * 
     * @return The potentially empty list of {@link LoggingEvent}s caputred.
     */
    public List<LoggingEvent> getEvents() {
        return logProvider.getEvents();
    }

    /**
     * The total number of {@link LoggingEvent}s captured.
     * 
     * @return The total number of {@link LoggingEvent}s captured.
     */
    public int size() {
        return logProvider.getEvents().size();
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        logProvider.beforeTestExecution(context);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        logProvider.afterTestExecution(context);
    }

    /**
     * Get the logProvider.
     * 
     * @return Returns the logProvider.
     */
    public LogProvider getLogProvider() {
        return logProvider;
    }

    /**
     * Set the logProvider.
     * 
     * @param logProvider
     *            The logProvider to set.
     */
    public void setLogProvider(LogProvider logProvider) {
        this.logProvider = logProvider;
    }

    private String buildPrefix(String message) {
        return (StringUtils.isNotBlank(message) ? message + " ==> " : "");
    }

    private String loggingEventToString(LoggingEvent event) {
        ToStringBuilder builder = new ToStringBuilder(event);
        builder.append("loggerName", event.getLoggerName());
        builder.append("level", event.getLevel());
        builder.append("message", event.getMessage());
        return builder.toString();
    }

}
