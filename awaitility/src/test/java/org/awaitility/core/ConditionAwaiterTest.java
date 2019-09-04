
package org.awaitility.core;

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
}