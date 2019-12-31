
package org.awaitility.core;

import static org.awaitility.Awaitility.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.Duration;

import org.junit.Test;

public class ConditionAwaiterTest {


    /**
     * Asserts that https://github.com/awaitility/awaitility/issues/95 is resolved
     */
    @Test public void
    calculates_a_duration_of_1_nano_when_system_nano_time_is_skewed() {
        Duration duration = ConditionAwaiter.calculateConditionEvaluationDuration(Duration.ofNanos(10000000L), System.nanoTime());

        assertThat(duration.toNanos(), is(1L));
    }

    @Test public void atLeastIncludesPollDelayWithPollInterval() {
        long start = System.currentTimeMillis();
        await()
            .pollDelay(Duration.ofMillis(100))
            .pollInterval(Duration.ofMillis(50))
            .atLeast(Duration.ofMillis(1000))
            .until(() -> System.currentTimeMillis() - start > 1050);
    }
}
