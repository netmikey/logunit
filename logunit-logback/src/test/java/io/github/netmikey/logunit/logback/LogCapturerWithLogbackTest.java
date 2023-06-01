package io.github.netmikey.logunit.logback;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.Level;

import io.github.netmikey.logunit.api.LogCapturer;
import org.slf4j.event.LoggingEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Unit test that uses Logback, applies {@link LogCapturer}s and validates their
 * behavior.
 */
@TestMethodOrder(MethodName.class)
public class LogCapturerWithLogbackTest {

    @RegisterExtension
    LogCapturer testLoggerInfoCapturer = LogCapturer.create().captureForType(LogCapturerWithLogbackTest.class);

    @RegisterExtension
    LogCapturer namedLoggerWarnCapturer = LogCapturer.create().captureForLogger(LOGGER_NAME, Level.WARN);

    private static final String LOGGER_NAME = "CUSTOM_LOGGER";

    private Logger testLogger = LoggerFactory.getLogger(LogCapturerWithLogbackTest.class);

    private Logger namedLogger = LoggerFactory.getLogger(LOGGER_NAME);

    /**
     * Test
     * <ul>
     * <li>that the testLogger (by logger type) captures the INFO level and
     * above when no level is specified</li>
     * <li>that the namedLogger (by logger name) captures only the WARN level
     * and above as specified</li>
     * <li>both loggers and their capturers don't affect each other</li>
     * </ul>
     */
    @Test
    public void test1CaptureMessages() {
        logEverythingOnce(testLogger);
        logEverythingOnce(namedLogger);

        Assertions.assertEquals(3, testLoggerInfoCapturer.size(), "should contain each one of INFO, WARN and ERROR");
        testLoggerInfoCapturer.assertDoesNotContain("trace");
        testLoggerInfoCapturer.assertDoesNotContain("debug");
        testLoggerInfoCapturer.assertContains("info message");
        testLoggerInfoCapturer.assertContains("Some warn");
        testLoggerInfoCapturer.assertContains("error");

        Assertions.assertEquals(2, namedLoggerWarnCapturer.size(), "should contain each one of WARN and ERROR");
        namedLoggerWarnCapturer.assertDoesNotContain("trace");
        namedLoggerWarnCapturer.assertDoesNotContain("debug");
        namedLoggerWarnCapturer.assertDoesNotContain("info message");
        namedLoggerWarnCapturer.assertContains("Some warn");
        namedLoggerWarnCapturer.assertContains("error");
    }

    /**
     * Test that {@link LogCapturer}s are being reset after each test.
     */
    @Test
    public void test2CapturerReset() {
        Assertions.assertEquals(0, testLoggerInfoCapturer.size());
        Assertions.assertEquals(0, namedLoggerWarnCapturer.size());
    }

    /**
     * Test that custom predicate matching works.
     */
    @Test
    public void test3CustomPredicates() {
        logEverythingOnce(testLogger);

        testLoggerInfoCapturer.assertDoesNotContain(e -> LOGGER_NAME.equals(e.getLoggerName()),
            "doesn't contain any logs from the " + LOGGER_NAME + " logger");

        testLoggerInfoCapturer.assertContains(e -> Level.ERROR.equals(e.getLevel()), "contain some ERROR message");
        testLoggerInfoCapturer.assertContains(e -> e.getMessage().matches("^Some [^ ]+ message$"),
            "contain 'Some * message'");
    }

    /**
     * Test that the values of the new fluent interface can be accessed
     */
    @Test
    void test4FluentApi() {
        Marker marker1 = MarkerFactory.getMarker("Marker 1");
        Marker marker2 = MarkerFactory.getMarker("Marker 2");
        testLogger
            .atError()
            .setMessage(() -> "{}-Message {}")
            .addArgument("Test")
            .addArgument(42)
            .addKeyValue("key1", "value1")
            .addKeyValue("key2", "value2")
            .addMarker(marker1)
            .addMarker(marker2)
            .log();

        List<LoggingEvent> events = testLoggerInfoCapturer.getEvents();
        Assertions.assertEquals(events.size(), 1);

        LoggingEvent event = events.get(0);
        Assertions.assertEquals("Test-Message 42", event.getMessage());
        Assertions.assertEquals(Arrays.asList("Test", 42), event.getArguments());
        Assertions.assertEquals(Arrays.asList(marker1, marker2), event.getMarkers());

        List<KeyValuePair> pairs = event.getKeyValuePairs();
        Assertions.assertEquals(2, pairs.size());
        Assertions.assertEquals("key1=\"value1\"", pairs.get(0).toString());
        Assertions.assertEquals("key2=\"value2\"", pairs.get(1).toString());
    }

    private void logEverythingOnce(Logger logger) {
        logger.trace("Some trace message");
        logger.debug("Some debug message");
        logger.info("Some info message");
        logger.warn("Some warn message");
        logger.error("Some error message");
    }
}
