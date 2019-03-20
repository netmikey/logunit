package io.github.netmikey.logunit.log4j2;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.event.LoggingEvent;

import io.github.netmikey.logunit.api.LogCapturer;
import io.github.netmikey.logunit.core.BaseLogProvider;

/**
 * {@link LogCapturer} implementation based on Log4j.
 */
public class Log4j2LogProvider extends BaseLogProvider {

    private static final Random RAND = new Random();

    private final ListAppender listAppender;

    /**
     * Default constructor.
     */
    public Log4j2LogProvider() {
        super();
        listAppender = ListAppender.create("LogUnitListAppender" + RAND.nextInt());
    }

    @Override
    public List<LoggingEvent> getEvents() {
        return StreamSupport.stream(listAppender.spliterator(), false)
            .collect(Collectors.toList());
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        // Register ListAppender instance with log4j2
        getLoggerContext().getConfiguration().addAppender(listAppender);
        createLoggersAndAddAppender();
        listAppender.start();
        getLoggerContext().updateLoggers();
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        listAppender.stop();
        removeLoggers();
        getLoggerContext().updateLoggers();
    }

    private LoggerContext getLoggerContext() {
        return (LoggerContext) LogManager.getContext(false);
    }

    private void createLoggersAndAddAppender() {
        getLoggerTypes().forEach((loggerType, level) -> {
            createLoggerAndAddAppender(loggerType.getName(), LevelMapper.mapLevel(level));
        });
        getLoggerNames().forEach((loggerName, level) -> {
            createLoggerAndAddAppender(loggerName, LevelMapper.mapLevel(level));
        });
    }

    private void removeLoggers() {
        getLoggerTypes().keySet().forEach(loggerType -> {
            removeLogger(loggerType.getName());
        });
        getLoggerNames().keySet().forEach(this::removeLogger);
    }

    private void createLoggerAndAddAppender(String loggerName, Level level) {
        LoggerContext ctx = getLoggerContext();
        Configuration cfg = ctx.getConfiguration();
        AppenderRef ref = AppenderRef.createAppenderRef(listAppender.getName(), null, null);
        AppenderRef[] refs = new AppenderRef[] { ref };
        LoggerConfig loggerConfig = LoggerConfig.createLogger(true, level, loggerName, "true", refs, null, cfg, null);
        loggerConfig.addAppender(listAppender, level, null);
        cfg.addLogger(loggerConfig.getName(), loggerConfig);
    }

    private void removeLogger(String loggerName) {
        getLoggerContext().getConfiguration().removeLogger(loggerName);
    }

}
