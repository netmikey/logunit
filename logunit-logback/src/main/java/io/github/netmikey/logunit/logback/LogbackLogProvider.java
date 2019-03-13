package io.github.netmikey.logunit.logback;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.event.LoggingEvent;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.read.ListAppender;
import io.github.netmikey.logunit.api.LogCapturer;
import io.github.netmikey.logunit.api.LogProvider;

/**
 * {@link LogCapturer} implementation based on Logback.
 */
public class LogbackLogProvider implements LogProvider {

    private static final Map<org.slf4j.event.Level, ch.qos.logback.classic.Level> LEVEL_MAPPING;

    private static final Map<ch.qos.logback.classic.Level, org.slf4j.event.Level> LEVEL_MAPPING_REVERSE;

    static {
        Map<org.slf4j.event.Level, ch.qos.logback.classic.Level> levelMapping = new HashMap<>();
        levelMapping.put(org.slf4j.event.Level.TRACE, ch.qos.logback.classic.Level.TRACE);
        levelMapping.put(org.slf4j.event.Level.DEBUG, ch.qos.logback.classic.Level.DEBUG);
        levelMapping.put(org.slf4j.event.Level.INFO, ch.qos.logback.classic.Level.INFO);
        levelMapping.put(org.slf4j.event.Level.WARN, ch.qos.logback.classic.Level.WARN);
        levelMapping.put(org.slf4j.event.Level.ERROR, ch.qos.logback.classic.Level.ERROR);

        LEVEL_MAPPING = Collections.unmodifiableMap(levelMapping);

        Map<ch.qos.logback.classic.Level, org.slf4j.event.Level> levelMappingReverse = new HashMap<>();
        levelMapping.forEach((key, value) -> levelMappingReverse.put(value, key));
        levelMappingReverse.put(ch.qos.logback.classic.Level.ALL, org.slf4j.event.Level.TRACE);
        levelMappingReverse.put(ch.qos.logback.classic.Level.OFF, org.slf4j.event.Level.ERROR);

        LEVEL_MAPPING_REVERSE = Collections.unmodifiableMap(levelMappingReverse);
    }

    private final ListAppender<ILoggingEvent> listAppender = new ListAppender<ILoggingEvent>();

    private final Map<Class<?>, Level> loggerTypes = new HashMap<>();

    private final Map<String, Level> loggerNames = new HashMap<>();

    private final Map<String, Level> originalLevels = new HashMap<>();

    @Override
    public void provideForType(Class<?> type, org.slf4j.event.Level level) {
        if (loggerTypes.containsKey(type)) {
            throw new IllegalArgumentException("LogProvider already providing LogEvents for Logger of type "
                + type.getName() + ". Each logger must only be captured once!");
        }
        loggerTypes.put(type, mapLevel(level));
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
        return listAppender.list.stream().map(this::mapEvent).collect(Collectors.toList());
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        addAppenderToLoggingSources();
        listAppender.start();
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        listAppender.stop();
        detachAppenderFromLoggingSources();
    }

    private void addAppenderToLoggingSources() {
        for (Map.Entry<Class<?>, Level> logSource : loggerTypes.entrySet()) {
            addAppenderToType(logSource.getKey(), logSource.getValue());
        }
        for (Map.Entry<String, Level> logSource : loggerNames.entrySet()) {
            addAppenderToLogger(logSource.getKey(), logSource.getValue());
        }
    }

    private void detachAppenderFromLoggingSources() {
        for (Map.Entry<Class<?>, Level> logSource : loggerTypes.entrySet()) {
            detachAppenderFromType(logSource.getKey());
        }
        for (Map.Entry<String, Level> logSource : loggerNames.entrySet()) {
            detachAppenderFromLogger(logSource.getKey());
        }
    }

    private void addAppenderToType(Class<?> type, Level level) {
        addAppenderToLogger((Logger) LoggerFactory.getLogger(type), level);
    }

    private void addAppenderToLogger(String name, Level level) {
        addAppenderToLogger((Logger) LoggerFactory.getLogger(name), level);
    }

    private void addAppenderToLogger(Logger logger, Level level) {
        logger.addAppender(listAppender);
        originalLevels.put(logger.getName(), logger.getLevel());
        logger.setLevel(level);
    }

    private void detachAppenderFromType(Class<?> type) {
        detachAppenderFromLogger((Logger) LoggerFactory.getLogger(type));
    }

    private void detachAppenderFromLogger(String name) {
        detachAppenderFromLogger((Logger) LoggerFactory.getLogger(name));
    }

    private void detachAppenderFromLogger(Logger logger) {
        logger.detachAppender(listAppender);
        Level originalLevel = originalLevels.get(logger.getName());
        if (originalLevel != null) {
            logger.setLevel(originalLevel);
        }
    }

    private LoggingEvent mapEvent(ILoggingEvent iEvent) {
        return new LoggingEvent() {

            @Override
            public long getTimeStamp() {
                return iEvent.getTimeStamp();
            }

            @Override
            public Throwable getThrowable() {
                IThrowableProxy throwableProxy = iEvent.getThrowableProxy();
                if (throwableProxy == null) {
                    return null;
                } else if (throwableProxy instanceof ThrowableProxy) {
                    return ((ThrowableProxy) throwableProxy).getThrowable();
                } else {
                    throw new IllegalStateException(
                        "Don't know how to extract the actual Throwable from " + throwableProxy.getClassName());
                }
            }

            @Override
            public String getThreadName() {
                return iEvent.getThreadName();
            }

            @Override
            public String getMessage() {
                return iEvent.getFormattedMessage();
            }

            @Override
            public Marker getMarker() {
                return iEvent.getMarker();
            }

            @Override
            public String getLoggerName() {
                return iEvent.getLoggerName();
            }

            @Override
            public org.slf4j.event.Level getLevel() {
                return mapLevel(iEvent.getLevel());
            }

            @Override
            public Object[] getArgumentArray() {
                return iEvent.getArgumentArray();
            }
        };
    }

    private Level mapLevel(org.slf4j.event.Level level) {
        Level result = LEVEL_MAPPING.get(level);
        if (result == null) {
            throw new IllegalArgumentException("Cannot map log level " + level + " to a Logback log level");
        }
        return result;
    }

    private org.slf4j.event.Level mapLevel(Level level) {
        org.slf4j.event.Level result = LEVEL_MAPPING_REVERSE.get(level);
        if (result == null) {
            throw new IllegalArgumentException("Cannot map Logback log level " + level + " to an slf4j log level");
        }
        return result;
    }
}
