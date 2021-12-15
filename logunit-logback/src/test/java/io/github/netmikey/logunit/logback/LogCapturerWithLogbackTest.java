package io.github.netmikey.logunit.logback;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import io.github.netmikey.logunit.api.LogCapturer;

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
     * <li>both loggers and their capturers don't affeact each other</li>
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

    private void logEverythingOnce(Logger logger) {
        logger.trace("Some trace message");
        logger.debug("Some debug message");
        logger.info("Some info message");
        logger.warn("Some warn message");
        logger.error("Some error message");
    }
}
