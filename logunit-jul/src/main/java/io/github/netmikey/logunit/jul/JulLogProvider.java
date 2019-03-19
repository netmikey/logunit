package io.github.netmikey.logunit.jul;

import java.util.Collections;
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
import io.github.netmikey.logunit.api.LogProvider;

/**
 * {@link LogCapturer} implementation based on JUL.
 */
public class JulLogProvider implements LogProvider {

    private static final Map<org.slf4j.event.Level, java.util.logging.Level> LEVEL_MAPPING;

    private static final Map<java.util.logging.Level, org.slf4j.event.Level> LEVEL_MAPPING_REVERSE;

    static {
        Map<org.slf4j.event.Level, java.util.logging.Level> levelMapping = new HashMap<>();
        levelMapping.put(org.slf4j.event.Level.TRACE, java.util.logging.Level.FINEST);
        levelMapping.put(org.slf4j.event.Level.DEBUG, java.util.logging.Level.FINE);
        levelMapping.put(org.slf4j.event.Level.INFO, java.util.logging.Level.INFO);
        levelMapping.put(org.slf4j.event.Level.WARN, java.util.logging.Level.WARNING);
        levelMapping.put(org.slf4j.event.Level.ERROR, java.util.logging.Level.SEVERE);

        LEVEL_MAPPING = Collections.unmodifiableMap(levelMapping);

        Map<java.util.logging.Level, org.slf4j.event.Level> levelMappingReverse = new HashMap<>();
        levelMapping.forEach((key, value) -> levelMappingReverse.put(value, key));
        levelMappingReverse.put(java.util.logging.Level.CONFIG, org.slf4j.event.Level.INFO);
        levelMappingReverse.put(java.util.logging.Level.FINER, org.slf4j.event.Level.DEBUG);

        LEVEL_MAPPING_REVERSE = Collections.unmodifiableMap(levelMappingReverse);
    }

    private final ListHandler listHandler = new ListHandler();

    private final Map<String, Level> loggerNames = new HashMap<>();

    private final Map<String, Level> originalLevels = new HashMap<>();

    @Override
    public void provideForType(Class<?> type, org.slf4j.event.Level level) {
        provideForLogger(type.getName(), level);
    }

    @Override
    public void provideForLogger(String name, org.slf4j.event.Level level) {
        if (loggerNames.containsKey(name)) {
            throw new IllegalArgumentException("LogProvider already providing LogEvents for Logger with name "
                + name + ". Each logger must only be captured once!");
        }
        loggerNames.put(name, mapLevel(level));
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
        for (Map.Entry<String, Level> logSource : loggerNames.entrySet()) {
            addAppenderToLogger(logSource.getKey(), logSource.getValue());
        }
    }

    private void detachAppenderFromLoggingSources() {
        for (Map.Entry<String, Level> logSource : loggerNames.entrySet()) {
            detachAppenderFromLogger(logSource.getKey());
        }
    }

    private void addAppenderToLogger(String name, Level level) {
        addAppenderToLogger((Logger) Logger.getLogger(name), level);
    }

    private void addAppenderToLogger(Logger logger, Level level) {
        logger.addHandler(listHandler);
        originalLevels.put(logger.getName(), logger.getLevel());
        logger.setLevel(level);
    }

    private void detachAppenderFromLogger(String name) {
        detachAppenderFromLogger((Logger) Logger.getLogger(name));
    }

    private void detachAppenderFromLogger(Logger logger) {
        logger.removeHandler(listHandler);
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
                return mapLevel(record.getLevel());
            }

            @Override
            public Object[] getArgumentArray() {
                return record.getParameters();
            }
        };
    }

    private Level mapLevel(org.slf4j.event.Level level) {
        Level result = LEVEL_MAPPING.get(level);
        if (result == null) {
            throw new IllegalArgumentException("Cannot map log level " + level + " to a JUL log level");
        }
        return result;
    }

    private org.slf4j.event.Level mapLevel(Level level) {
        org.slf4j.event.Level result = LEVEL_MAPPING_REVERSE.get(level);
        if (result == null) {
            throw new IllegalArgumentException("Cannot map JUL log level " + level + " to an slf4j log level");
        }
        return result;
    }
}
