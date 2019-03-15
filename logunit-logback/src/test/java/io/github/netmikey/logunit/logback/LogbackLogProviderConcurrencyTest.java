package io.github.netmikey.logunit.logback;

import java.util.ConcurrentModificationException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import ch.qos.logback.core.read.ListAppender;

/**
 * There have been sightings of {@link ConcurrentModificationException}s caused
 * by Logback's {@link ListAppender}. This test aims at reproducing this and
 * testing the implemented {@link ConcurrentListAppender} solution.
 */
public class LogbackLogProviderConcurrencyTest {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LogbackLogProviderConcurrencyTest.class);

    private static final int LOG_COUNT = 100;

    private LogbackLogProvider tested;

    private Thread loggingThread;

    /**
     * JUnit's beforeEach method.
     */
    @BeforeEach
    public void beforeEach() {
        // Prepare unit under test
        tested = new LogbackLogProvider();
        tested.provideForLogger(LOGGER.getName(), Level.TRACE);
        tested.beforeTestExecution(null);

        // Prepare a thread that is going to write logs
        loggingThread = new Thread(() -> {
            for (int i = 0; i < LOG_COUNT; i++) {
                LOGGER.trace("It makes BOOM!");
            }
        });
    }

    /**
     * JUnit's afterEach method.
     * 
     * @throws Exception
     *             An unexpected exception occured.
     */
    @AfterEach
    public void afterEach() throws Exception {
        // Wait for the thread to terminate
        loggingThread.join();

        // Cleanup
        tested.afterTestExecution(null);
    }

    /**
     * Test the {@link LogbackLogProvider} for concurrency.
     * 
     * @throws Exception
     *             An unexpected exception occurred.
     */
    @Test
    @RepeatedTest(value = 15)
    public void testConcurrency() throws Exception {
        // Start the logging thread, and...
        loggingThread.start();

        // ... try to fetc log events concurrently
        for (int i = 0; i < LOG_COUNT; i++) {
            tested.getEvents();
        }
    }
}
