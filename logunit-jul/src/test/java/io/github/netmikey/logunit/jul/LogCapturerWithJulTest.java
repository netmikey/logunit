package io.github.netmikey.logunit.jul;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;

import io.github.netmikey.logunit.api.LogCapturer;
import org.slf4j.event.LoggingEvent;

/**
 * Unit test that uses JUL, applies {@link LogCapturer}s and validates their
 * behavior.
 */
@TestMethodOrder(MethodName.class)
public class LogCapturerWithJulTest {

    @RegisterExtension
    LogCapturer testLoggerInfoCapturer = LogCapturer.create().captureForType(LoggingObject.class);

    @RegisterExtension
    LogCapturer namedLoggerWarnCapturer = LogCapturer.create().captureForLogger(LOGGER_NAME, Level.WARN);

    private static final String LOGGER_NAME = "CUSTOM_LOGGER";

    private Logger namedLogger = Logger.getLogger(LOGGER_NAME);

    /**
     * Test
     * <ul>
     * <li>that the testLogger (by logger type) captures the INFO level and
     * above when no level is specified</li>
     * <li>that the namedLogger (by logger name) captures only the WARN level
     * and above as specified</li>
     * <li>both loggers and their capturers don't affect each other</li>
     * <li>logger is not garbage collected, losing interception</li>
     * </ul>
     * 
     * @throws Exception
     *             Unexpected failure.
     */
    @Test
    public void test1CaptureMessages() throws Exception {
        System.gc();
        Thread.sleep(50);

        logEverythingOnce(new LoggingObject().testLogger);
        logEverythingOnce(namedLogger);

        Assertions.assertEquals(3, testLoggerInfoCapturer.size(),
            "should contain each one of INFO, WARNING and SEVERE");
        testLoggerInfoCapturer.assertDoesNotContain("finest");
        testLoggerInfoCapturer.assertDoesNotContain("finer");
        testLoggerInfoCapturer.assertDoesNotContain("fine");
        testLoggerInfoCapturer.assertDoesNotContain("config");
        testLoggerInfoCapturer.assertContains("info message");
        testLoggerInfoCapturer.assertContains("Some warning");
        testLoggerInfoCapturer.assertContains("severe");

        Assertions.assertEquals(2, namedLoggerWarnCapturer.size(), "should contain each one of WARNING and SEVERE");
        namedLoggerWarnCapturer.assertDoesNotContain("finest");
        namedLoggerWarnCapturer.assertDoesNotContain("finer");
        namedLoggerWarnCapturer.assertDoesNotContain("fine");
        namedLoggerWarnCapturer.assertDoesNotContain("config");
        namedLoggerWarnCapturer.assertDoesNotContain("info message");
        namedLoggerWarnCapturer.assertContains("Some warning");
        namedLoggerWarnCapturer.assertContains("severe");
    }

    /**
     * Test that {@link LogCapturer}s are being reset after each test.
     */
    @Test
    public void test2CapturerReset() {
        Assertions.assertEquals(0, testLoggerInfoCapturer.size());
        Assertions.assertEquals(0, namedLoggerWarnCapturer.size());
    }

    @Test
    void test3EventMethodsWithoutParameters() {
        namedLogger.warning(() -> "Test-Message");

        List<LoggingEvent> events = namedLoggerWarnCapturer.getEvents();
        Assertions.assertEquals(events.size(), 1);

        LoggingEvent event = events.get(0);
        Assertions.assertEquals("Test-Message", event.getMessage());
        Assertions.assertEquals(Level.WARN, event.getLevel());
        Assertions.assertSame(Collections.emptyList(), event.getArguments());
        Assertions.assertSame(Collections.emptyList(), event.getKeyValuePairs());
        Assertions.assertSame(Collections.emptyList(), event.getMarkers());
    }

    @Test
    void test4EventMethodsWithParameters() {
        namedLogger.log(java.util.logging.Level.SEVERE, "{0}-Message {1}", new Object[]{"Test", 42});

        List<LoggingEvent> events = namedLoggerWarnCapturer.getEvents();
        Assertions.assertEquals(events.size(), 1);

        LoggingEvent event = events.get(0);
        Assertions.assertEquals("{0}-Message {1}", event.getMessage(),
                "returns the raw message (*not* formatted)");
        Assertions.assertEquals(Level.ERROR, event.getLevel());
        Assertions.assertEquals(Arrays.asList("Test", 42), event.getArguments());
        Assertions.assertSame(Collections.emptyList(), event.getKeyValuePairs());
        Assertions.assertSame(Collections.emptyList(), event.getMarkers());
    }

    private void logEverythingOnce(Logger logger) {
        logger.finest("Some finest message");
        logger.finer("Some finer message");
        logger.fine("Some fine message");
        logger.config("Some config message");
        logger.info("Some info message");
        logger.warning("Some warning message");
        logger.severe("Some severe message");
    }

    /**
     * The logger will not be constructed until the object is. This tests that
     * log messages will be captured without allowing the intercepted Logger to
     * be garbage collected.
     */
    private static class LoggingObject {
        Logger testLogger = Logger.getLogger(LoggingObject.class.getName());
    }
}
