package org.awaitility;

import org.awaitility.pollinterval.FibonacciPollInterval;
import org.awaitility.pollinterval.FixedPollInterval;
import org.awaitility.pollinterval.PollInterval;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AwaitilityDefaultGettersTest {

    @Before
    public void setUp() {
        Awaitility.reset();
    }

    @After
    public void tearDown() {
        Awaitility.reset();
    }

    @Test
    public void timeoutGetterReturnsBuiltInDefaultThenSetValueThenDefaultAfterReset() {
        assertEquals(Duration.ofSeconds(10), Awaitility.getDefaultTimeout());
        Awaitility.setDefaultTimeout(Duration.ofSeconds(3));
        assertEquals(Duration.ofSeconds(3), Awaitility.getDefaultTimeout());
        Awaitility.setDefaultTimeout(250, TimeUnit.MILLISECONDS);
        assertEquals(Duration.ofMillis(250), Awaitility.getDefaultTimeout());
        Awaitility.reset();
        assertEquals(Duration.ofSeconds(10), Awaitility.getDefaultTimeout());
    }

    @Test
    public void pollIntervalGetterReturnsBuiltInDefaultThenSetValueThenDefaultAfterReset() {
        assertFixed(Duration.ofMillis(100), Awaitility.getDefaultPollInterval());
        Awaitility.setDefaultPollInterval(Duration.ofMillis(7));
        assertFixed(Duration.ofMillis(7), Awaitility.getDefaultPollInterval());
        Awaitility.setDefaultPollInterval(9, TimeUnit.MILLISECONDS);
        assertFixed(Duration.ofMillis(9), Awaitility.getDefaultPollInterval());
        Awaitility.reset();
        assertFixed(Duration.ofMillis(100), Awaitility.getDefaultPollInterval());
    }

    @Test
    public void pollIntervalGetterReturnsTheSameNonFixedPollIntervalThatWasSet() {
        PollInterval fibonacci = FibonacciPollInterval.fibonacci();
        Awaitility.setDefaultPollInterval(fibonacci);
        assertSame(fibonacci, Awaitility.getDefaultPollInterval());
    }

    @Test
    public void pollDelayGetterIsNullUntilSetThenReturnsSetValueThenNullAfterReset() {
        assertNull(Awaitility.getDefaultPollDelay());
        Awaitility.setDefaultPollDelay(Duration.ofMillis(5));
        assertEquals(Duration.ofMillis(5), Awaitility.getDefaultPollDelay());
        Awaitility.setDefaultPollDelay(6, TimeUnit.MILLISECONDS);
        assertEquals(Duration.ofMillis(6), Awaitility.getDefaultPollDelay());
        Awaitility.reset();
        assertNull(Awaitility.getDefaultPollDelay());
    }

    private static void assertFixed(Duration expected, PollInterval actual) {
        assertTrue("Expected FixedPollInterval but was " + actual, actual instanceof FixedPollInterval);
        assertEquals(expected, actual.next(1, Duration.ZERO));
    }
}
