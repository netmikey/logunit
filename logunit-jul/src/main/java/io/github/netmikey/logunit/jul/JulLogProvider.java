package io.github.netmikey.logunit.jul;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Marker;
import org.slf4j.event.LoggingEvent;

import io.github.netmikey.logunit.api.LogCapturer;
import io.github.netmikey.logunit.core.BaseLogProvider;

/**
 * {@link LogCapturer} implementation based on JUL.
 */
public class JulLogProvider extends BaseLogProvider {

    private final ListHandler listHandler = new ListHandler();

    // We hold references to loggers we have intercepted to avoid them being garbage collected and reconstructed without
    // our handler in between beforeTestExecution and the actual test business logic.
    private final Map<String, Logger> loggers = new HashMap<>();

    private final Map<String, Level> originalLevels = new HashMap<>();

    @Override
    public void provideForType(Class<?> type, org.slf4j.event.Level level) {
        provideForLogger(type.getName(), level);
    }

    @Override
    public List<LoggingEvent> getEvents() {
        return StreamSupport.stream(listHandler.spliterator(), false)
            .map(this::mapEvent)
            .collect(Collectors.toList());
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        addAppenderToLoggingSources();
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        listHandler.flush();
        listHandler.close();
        detachAppenderFromLoggingSources();
    }

    private void addAppenderToLoggingSources() {
        getLoggerNames().forEach((loggerName, level) -> {
            addAppenderToLogger(loggerName, LevelMapper.mapLevel(level));
        });
    }

    private void detachAppenderFromLoggingSources() {
        getLoggerNames().keySet().forEach(this::detachAppenderFromLogger);
    }

    private void addAppenderToLogger(String name, Level level) {
        addAppenderToLogger((Logger) Logger.getLogger(name), level);
    }

    private void addAppenderToLogger(Logger logger, Level level) {
        logger.addHandler(listHandler);
        loggers.put(logger.getName(), logger);
        originalLevels.put(logger.getName(), logger.getLevel());
        logger.setLevel(level);
    }

    private void detachAppenderFromLogger(String name) {
        detachAppenderFromLogger((Logger) Logger.getLogger(name));
    }

    private void detachAppenderFromLogger(Logger logger) {
        logger.removeHandler(listHandler);
        loggers.remove(logger.getName());
        Level originalLevel = originalLevels.get(logger.getName());
        if (originalLevel != null) {
            logger.setLevel(originalLevel);
        }
    }

    private LoggingEvent mapEvent(LogRecord record) {
        return new LoggingEvent() {

            @Override
            public long getTimeStamp() {
                return record.getMillis();
            }

            @Override
            public Throwable getThrowable() {
                return record.getThrown();
            }

            @Override
            public String getThreadName() {
                return String.valueOf(record.getThreadID());
            }

            @Override
            public String getMessage() {
                return record.getMessage();
            }

            @Override
            public Marker getMarker() {
                return null;
            }

            @Override
            public String getLoggerName() {
                return record.getLoggerName();
            }

            @Override
            public org.slf4j.event.Level getLevel() {
                return LevelMapper.mapLevel(record.getLevel());
            }

            @Override
            public Object[] getArgumentArray() {
                return record.getParameters();
            }
        };
    }
}
