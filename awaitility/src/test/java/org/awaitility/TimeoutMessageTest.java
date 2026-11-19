/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.awaitility;

import org.awaitility.core.ConditionEvaluationListener;
import org.awaitility.core.ConditionTimeoutException;
import org.awaitility.core.EvaluatedCondition;
import org.awaitility.core.TimeoutEvent;
import org.junit.After;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

/**
 * Tests for {@link org.awaitility.core.ConditionFactory#timeoutMessage(Supplier)}.
 * <p>
 * Re-implements the idea of pull request https://github.com/awaitility/awaitility/pull/195 by James Agnew.
 */
public class TimeoutMessageTest {

    private static final Duration SHORT = Duration.ofMillis(200);
    private static final Duration LONG = Duration.ofSeconds(2);

    @After
    public void reset_awaitility_after_each_test() {
        Awaitility.reset();
    }

    @Test(timeout = 2000)
    public void supplier_is_not_called_when_the_condition_is_fulfilled() {
        AtomicInteger calls = new AtomicInteger();

        await().atMost(LONG)
                .timeoutMessage(countingSupplier(calls, "never shown"))
                .until(() -> true);

        assertThat(calls).hasValue(0);
    }

    @Test(timeout = 2000)
    public void supplier_is_called_once_on_timeout_and_its_text_is_appended_for_callable_boolean() {
        AtomicInteger calls = new AtomicInteger();

        ConditionTimeoutException e = catchThrowableOfType(ConditionTimeoutException.class, () ->
                await().atMost(SHORT)
                        .timeoutMessage(countingSupplier(calls, "the queue still had 3 items"))
                        .until(() -> false));

        assertThat(calls).hasValue(1);
        assertThat(e.getMessage())
                .startsWith("Condition with ")
                .contains("was not fulfilled within 200 milliseconds.\n")
                .endsWith(".\nthe queue still had 3 items");
    }

    @Test(timeout = 2000)
    public void supplier_is_evaluated_lazily_so_it_sees_the_state_at_the_time_of_failure() {
        AtomicInteger polls = new AtomicInteger();
        AtomicInteger pollsSeenBySupplier = new AtomicInteger(-1);

        ConditionTimeoutException e = catchThrowableOfType(ConditionTimeoutException.class, () ->
                await().atMost(SHORT)
                        .pollInterval(Duration.ofMillis(20))
                        .timeoutMessage(() -> {
                            int seen = polls.get();
                            pollsSeenBySupplier.set(seen);
                            return "polled " + seen + " times";
                        })
                        .until(() -> polls.incrementAndGet() < 0));

        // A poll that is cancelled at the timeout may still increment polls after the message is built,
        // so compare with what the supplier saw rather than with polls.get().
        assertThat(pollsSeenBySupplier.get()).isGreaterThan(1);
        assertThat(e.getMessage()).endsWith("\npolled " + pollsSeenBySupplier.get() + " times");
    }

    @Test(timeout = 2000)
    public void text_is_appended_after_the_matcher_mismatch_for_supplier_and_matcher() {
        AtomicInteger value = new AtomicInteger(1);

        ConditionTimeoutException e = catchThrowableOfType(ConditionTimeoutException.class, () ->
                await().atMost(SHORT)
                        .timeoutMessage(() -> "value is " + value.get())
                        .until(value::get, equalTo(2)));

        assertThat(e.getMessage())
                .contains("expected <2> but was <1> within 200 milliseconds.\n")
                .endsWith("\nvalue is 1");
    }

    @Test(timeout = 2000)
    public void text_is_appended_after_the_assertion_error_message_for_until_asserted() {
        ConditionTimeoutException e = catchThrowableOfType(ConditionTimeoutException.class, () ->
                await().atMost(SHORT)
                        .timeoutMessage(() -> "custom text")
                        .untilAsserted(() -> assertThat(1).isEqualTo(2)));

        assertThat(e.getMessage())
                .startsWith("Assertion condition defined as a ")
                .contains("expected:<[2]> but was:<[1]>")
                .endsWith(" within 200 milliseconds.\ncustom text");
        assertThat(e.getCause()).isInstanceOf(AssertionError.class);
    }

    @Test(timeout = 2000)
    public void text_is_appended_for_until_atomic() {
        AtomicInteger atomic = new AtomicInteger(1);

        ConditionTimeoutException e = catchThrowableOfType(ConditionTimeoutException.class, () ->
                await().atMost(SHORT)
                        .timeoutMessage(() -> "atomic is " + atomic.get())
                        .untilAtomic(atomic, equalTo(2)));

        assertThat(e.getMessage())
                .contains("expected <2> but was <1> within 200 milliseconds.\n")
                .endsWith("\natomic is 1");
    }

    @Test(timeout = 2000)
    public void works_together_with_alias_in_any_order() {
        ConditionTimeoutException aliasFirst = catchThrowableOfType(ConditionTimeoutException.class, () ->
                await("my await").atMost(SHORT)
                        .timeoutMessage(() -> "custom text")
                        .until(() -> false));
        ConditionTimeoutException aliasLast = catchThrowableOfType(ConditionTimeoutException.class, () ->
                await().atMost(SHORT)
                        .timeoutMessage(() -> "custom text")
                        .alias("my await")
                        .until(() -> false));

        for (ConditionTimeoutException e : new ConditionTimeoutException[]{aliasFirst, aliasLast}) {
            assertThat(e.getMessage())
                    .startsWith("Condition with alias 'my await' didn't complete within 200 milliseconds because ")
                    .endsWith(".\ncustom text");
        }
    }

    @Test(timeout = 2000)
    public void timeout_is_kept_and_supplier_exception_is_added_as_suppressed_when_supplier_throws() {
        IllegalStateException supplierFailure = new IllegalStateException("supplier failed");

        ConditionTimeoutException e = catchThrowableOfType(ConditionTimeoutException.class, () ->
                await().atMost(SHORT)
                        .timeoutMessage(() -> {
                            throw supplierFailure;
                        })
                        .untilAsserted(() -> assertThat(1).isEqualTo(2)));

        assertThat(e.getMessage())
                .startsWith("Assertion condition defined as a ")
                .endsWith(" within 200 milliseconds.")
                .doesNotContain("supplier failed");
        assertThat(e.getCause()).isInstanceOf(AssertionError.class);
        assertThat(e.getSuppressed()).containsExactly(supplierFailure);
    }

    @Test(timeout = 2000)
    public void nothing_is_appended_when_supplier_returns_null_or_empty() {
        ConditionTimeoutException withNull = catchThrowableOfType(ConditionTimeoutException.class, () ->
                await().atMost(SHORT).timeoutMessage(() -> null).until(() -> false));
        ConditionTimeoutException withEmpty = catchThrowableOfType(ConditionTimeoutException.class, () ->
                await().atMost(SHORT).timeoutMessage(() -> "").until(() -> false));

        assertThat(withNull.getMessage()).endsWith(" within 200 milliseconds.").doesNotContain("\n");
        assertThat(withEmpty.getMessage()).endsWith(" within 200 milliseconds.").doesNotContain("\n");
        assertThat(withNull.getSuppressed()).isEmpty();
    }

    @Test(timeout = 2000)
    public void null_supplier_removes_a_previously_set_supplier() {
        AtomicInteger calls = new AtomicInteger();

        ConditionTimeoutException e = catchThrowableOfType(ConditionTimeoutException.class, () ->
                await().atMost(SHORT)
                        .timeoutMessage(countingSupplier(calls, "custom text"))
                        .timeoutMessage(null)
                        .until(() -> false));

        assertThat(calls).hasValue(0);
        assertThat(e.getMessage()).doesNotContain("custom text");
    }

    @Test(timeout = 2000)
    public void later_dsl_calls_keep_the_timeout_message() {
        AtomicInteger calls = new AtomicInteger();

        ConditionTimeoutException e = catchThrowableOfType(ConditionTimeoutException.class, () ->
                await().timeoutMessage(countingSupplier(calls, "custom text"))
                        .pollInterval(Duration.ofMillis(20))
                        .pollDelay(Duration.ofMillis(10))
                        .atMost(SHORT)
                        .conditionEvaluationListener(condition -> { })
                        .ignoreExceptions()
                        .and()
                        .until(() -> false));

        assertThat(calls).hasValue(1);
        assertThat(e.getMessage()).endsWith("within 200 milliseconds.\ncustom text");
    }

    @Test(timeout = 2000)
    public void condition_evaluation_listener_on_timeout_receives_the_message_with_the_custom_text() {
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<String> listenerMessage = new AtomicReference<>();
        ConditionEvaluationListener<Object> listener = new ConditionEvaluationListener<Object>() {
            @Override
            public void conditionEvaluated(EvaluatedCondition<Object> condition) {
            }

            @Override
            public void onTimeout(TimeoutEvent timeoutEvent) {
                listenerMessage.set(timeoutEvent.getDescription());
            }
        };

        ConditionTimeoutException e = catchThrowableOfType(ConditionTimeoutException.class, () ->
                await().atMost(SHORT)
                        .conditionEvaluationListener(listener)
                        .timeoutMessage(countingSupplier(calls, "custom text"))
                        .until(() -> false));

        assertThat(calls).hasValue(1);
        assertThat(listenerMessage.get()).isEqualTo(e.getMessage()).endsWith("\ncustom text");
    }

    @Test(timeout = 2000)
    public void text_is_appended_when_the_at_least_constraint_is_violated() {
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<String> listenerMessage = new AtomicReference<>();

        ConditionTimeoutException e = catchThrowableOfType(ConditionTimeoutException.class, () ->
                await().atLeast(Duration.ofSeconds(1))
                        .atMost(LONG)
                        .conditionEvaluationListener(new ConditionEvaluationListener<Object>() {
                            @Override
                            public void conditionEvaluated(EvaluatedCondition<Object> condition) {
                            }

                            @Override
                            public void onTimeout(TimeoutEvent timeoutEvent) {
                                listenerMessage.set(timeoutEvent.getDescription());
                            }
                        })
                        .timeoutMessage(countingSupplier(calls, "custom text"))
                        .until(() -> true));

        assertThat(calls).hasValue(1);
        assertThat(e.getMessage())
                .startsWith("Condition was evaluated in ")
                .contains("which is earlier than expected minimum timeout 1 seconds\n")
                .endsWith("\ncustom text");
        assertThat(listenerMessage.get()).isEqualTo(e.getMessage());
    }

    private static Supplier<String> countingSupplier(AtomicInteger calls, String message) {
        return () -> {
            calls.incrementAndGet();
            return message;
        };
    }
}
