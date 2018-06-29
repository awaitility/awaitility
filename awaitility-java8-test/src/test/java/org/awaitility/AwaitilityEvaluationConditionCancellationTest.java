package org.awaitility;

import org.awaitility.core.ConditionEvaluationLogger;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;

/**
 * Fixes <a href="https://github.com/awaitility/awaitility/issues/109">issue 109</a>.
 */
public class AwaitilityEvaluationConditionCancellationTest {

    @Test public void
    doesnt_show_result_that_was_evaluated_after_timeout() {
        final Throwable throwable = catchThrowable(() -> {
            AtomicInteger ai = new AtomicInteger();
            await("Hello").atMost(999, MILLISECONDS).pollInterval(100, MILLISECONDS)
                    .conditionEvaluationListener(new ConditionEvaluationLogger())
                    .until(() -> {
                Thread.sleep(600L);
                if (ai.incrementAndGet() == 2) {
                    return 100;
                }
                return -1;
            }, is(100));
        });

        assertThat(throwable).isExactlyInstanceOf(ConditionTimeoutException.class).
                hasMessage("Condition with alias 'Hello' didn't complete within 999 milliseconds because lambda expression in " + this.getClass().getName() + " that uses java.util.concurrent.atomic.AtomicInteger: expected <100> but was <-1>.");
    }
}