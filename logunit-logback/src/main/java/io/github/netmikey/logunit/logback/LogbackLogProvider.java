package io.github.netmikey.logunit.logback;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.LoggingEvent;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import io.github.netmikey.logunit.api.LogCapturer;
import io.github.netmikey.logunit.core.BaseLogProvider;

/**
 * {@link LogCapturer} implementation based on Logback.
 */
public class LogbackLogProvider extends BaseLogProvider {

    private final ConcurrentListAppender<ILoggingEvent> listAppender = new ConcurrentListAppender<ILoggingEvent>();

    private final Map<String, Level> originalLevels = new HashMap<>();

    @Override
    public List<LoggingEvent> getEvents() {
        return StreamSupport.stream(listAppender.spliterator(), false)
            .map(this::mapEvent)
            .collect(Collectors.toList());
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
        for (Map.Entry<Class<?>, org.slf4j.event.Level> logSource : getLoggerTypes().entrySet()) {
            addAppenderToType(logSource.getKey(), LevelMapper.mapLevel(logSource.getValue()));
        }
        for (Map.Entry<String, org.slf4j.event.Level> logSource : getLoggerNames().entrySet()) {
            addAppenderToLogger(logSource.getKey(), LevelMapper.mapLevel(logSource.getValue()));
        }
    }

    private void detachAppenderFromLoggingSources() {
        for (Map.Entry<Class<?>, org.slf4j.event.Level> logSource : getLoggerTypes().entrySet()) {
            detachAppenderFromType(logSource.getKey());
        }
        for (Map.Entry<String, org.slf4j.event.Level> logSource : getLoggerNames().entrySet()) {
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
            public List<Marker> getMarkers() {
                return iEvent.getMarkerList();
            }

            @Override
            public String getLoggerName() {
                return iEvent.getLoggerName();
            }

            @Override
            public org.slf4j.event.Level getLevel() {
                return LevelMapper.mapLevel(iEvent.getLevel());
            }

            @Override
            public Object[] getArgumentArray() {
                return iEvent.getArgumentArray();
            }

            @Override
            public List<Object> getArguments() {
                return Arrays.asList(iEvent.getArgumentArray());
            }

            @Override
            public List<KeyValuePair> getKeyValuePairs() {
                return iEvent.getKeyValuePairs();
            }
        };
    }
}
