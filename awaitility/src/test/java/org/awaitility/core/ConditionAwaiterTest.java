
package org.awaitility.core;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ConditionAwaiterTest {

    /**
     * Asserts that https://github.com/awaitility/awaitility/issues/95 is resolved
     */
    @Test void calculates_a_duration_of_1_nano_when_system_nano_time_is_skewed() {
        Duration duration = ConditionAwaiter.calculateConditionEvaluationDuration(Duration.ofNanos(10000000L), System.nanoTime(), 0, Duration.ofNanos(0), Duration.ofNanos(0));

        assertThat(duration.toNanos(), is(1L));
    }

    @Test void atLeastIncludesPollDelayWithPollInterval() {
        long start = System.currentTimeMillis();
        await()
                .pollDelay(Duration.ofMillis(100))
                .pollInterval(Duration.ofMillis(50))
                .atLeast(Duration.ofMillis(1000))
                .until(() -> System.currentTimeMillis() - start > 1050);
    }

    /**
     * Asserts that https://github.com/awaitility/awaitility/issues/152 is resolved
     */
    @Test void originalUncaughtExceptionHandlerIsSetBackAfterConditionEvaluation() {
        Thread.UncaughtExceptionHandler originalUncaughtExceptionHandler = (t, e) -> {};
        Thread.setDefaultUncaughtExceptionHandler(originalUncaughtExceptionHandler);

        final AtomicInteger count = new AtomicInteger(0);
        await().until(() -> {
            count.incrementAndGet();
            return count.get() > 1;
        });

        assertThat(Thread.getDefaultUncaughtExceptionHandler(), is(originalUncaughtExceptionHandler));
    }

    @Test void shouldHandleImmediateResultWithAtMost(){
        await().atMost(Duration.ofMillis(10)).pollInterval(Duration.ofMillis(5)).until(() -> true);
    }
}