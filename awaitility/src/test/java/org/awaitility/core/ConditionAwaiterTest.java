package org.awaitility.core;

import org.awaitility.Duration;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ConditionAwaiterTest {


    /**
     * Asserts that https://github.com/awaitility/awaitility/issues/95 is resolved
     */
    @Test public void
    calculates_a_duration_of_1_millis_when_system_time_millis_is_skewed() {
        Duration duration = ConditionAwaiter.calculateConditionEvaluationDuration(new Duration(1000, TimeUnit.MILLISECONDS), System.currentTimeMillis() + 1000L);

        assertThat(duration.getValueInMS(), is(1L));
    }
}