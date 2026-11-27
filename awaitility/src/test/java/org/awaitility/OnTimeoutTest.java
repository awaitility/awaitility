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
import org.awaitility.core.ConditionFactory;
import org.awaitility.core.ConditionTimeoutException;
import org.awaitility.core.EvaluatedCondition;
import org.awaitility.core.TerminalFailureException;
import org.awaitility.core.TimeoutEvent;
import org.junit.After;
import org.junit.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

/**
 * Tests for {@link ConditionFactory#onTimeout(java.util.function.Consumer)}, issue 283 / pull request 310.
 */
public class OnTimeoutTest {

    @After
    public void reset_awaitility_after_each_test() {
        Awaitility.reset();
    }

    private static ConditionFactory shortAwait() {
        return await().pollInterval(Duration.ofMillis(10)).atMost(Duration.ofMillis(100));
    }

    @Test(timeout = 2000)
    public void on_timeout_is_called_exactly_once_with_an_event_that_describes_the_timeout() {
        List<TimeoutEvent> events = new CopyOnWriteArrayList<>();

        ConditionTimeoutException e = expectTimeout(() -> shortAwait().alias("my alias")
                .onTimeout(events::add)
                .until(() -> false));

        assertThat(events, hasSize(1));
        assertThat(e.getMessage(), equalTo(events.get(0).getDescription()));
        assertThat(events.get(0).getAlias(), equalTo("my alias"));
        assertThat(events.get(0).isConditionIsFulfilled(), is(false));
    }

    @Test(timeout = 2000)
    public void on_timeout_is_not_called_when_the_condition_is_fulfilled() {
        AtomicInteger calls = new AtomicInteger();

        await().atMost(Duration.ofSeconds(1)).onTimeout(event -> calls.incrementAndGet()).until(() -> true);

        assertThat(calls.get(), is(0));
    }

    @Test(timeout = 2000)
    public void on_timeout_survives_a_later_condition_evaluation_listener() {
        AtomicInteger listenerCalls = new AtomicInteger();
        AtomicInteger callbackCalls = new AtomicInteger();

        expectTimeout(() -> shortAwait()
                .onTimeout(event -> callbackCalls.incrementAndGet())
                .conditionEvaluationListener(timeoutCountingListener(listenerCalls))
                .until(() -> false));

        assertThat(listenerCalls.get(), is(1));
        assertThat(callbackCalls.get(), is(1));
    }

    @Test(timeout = 2000)
    public void on_timeout_survives_an_earlier_condition_evaluation_listener_and_is_called_after_it() {
        List<String> calls = new CopyOnWriteArrayList<>();

        expectTimeout(() -> shortAwait()
                .conditionEvaluationListener(new ConditionEvaluationListener<Boolean>() {
                    @Override
                    public void conditionEvaluated(EvaluatedCondition<Boolean> condition) {
                    }

                    @Override
                    public void onTimeout(TimeoutEvent timeoutEvent) {
                        calls.add("listener");
                    }
                })
                .onTimeout(event -> calls.add("callback"))
                .until(() -> false));

        assertThat(calls, equalTo(asList("listener", "callback")));
    }

    @Test(timeout = 2000)
    public void on_timeout_survives_a_later_call_to_logging_with_a_log_printer() {
        AtomicInteger calls = new AtomicInteger();
        List<String> logged = new CopyOnWriteArrayList<>();

        expectTimeout(() -> shortAwait()
                .onTimeout(event -> calls.incrementAndGet())
                .logging(logged::add)
                .until(() -> false));

        assertThat(calls.get(), is(1));
        assertThat(logged, not(empty()));
    }

    @Test(timeout = 2000)
    public void on_timeout_survives_a_later_call_to_logging() {
        AtomicInteger calls = new AtomicInteger();

        expectTimeout(() -> shortAwait()
                .onTimeout(event -> calls.incrementAndGet())
                .logging()
                .until(() -> false));

        assertThat(calls.get(), is(1));
    }

    @Test(timeout = 2000)
    public void on_timeout_survives_an_earlier_call_to_logging() {
        AtomicInteger calls = new AtomicInteger();
        List<String> logged = new CopyOnWriteArrayList<>();

        expectTimeout(() -> shortAwait()
                .logging(logged::add)
                .onTimeout(event -> calls.incrementAndGet())
                .until(() -> false));

        assertThat(calls.get(), is(1));
        assertThat(logged, not(empty()));
    }

    @Test(timeout = 2000)
    public void calling_on_timeout_again_replaces_the_callback() {
        AtomicInteger first = new AtomicInteger();
        AtomicInteger second = new AtomicInteger();

        expectTimeout(() -> shortAwait()
                .onTimeout(event -> first.incrementAndGet())
                .onTimeout(event -> second.incrementAndGet())
                .until(() -> false));

        assertThat(first.get(), is(0));
        assertThat(second.get(), is(1));
    }

    /**
     * An atLeast violation goes through the same timeout path as ConditionEvaluationListener#onTimeout,
     * so the callback is called with an event whose condition is fulfilled.
     */
    @Test(timeout = 2000)
    public void on_timeout_is_called_when_the_condition_is_fulfilled_earlier_than_at_least() {
        List<TimeoutEvent> events = new CopyOnWriteArrayList<>();

        ConditionTimeoutException e = expectTimeout(() -> await()
                .atLeast(Duration.ofMillis(500))
                .atMost(Duration.ofSeconds(1))
                .onTimeout(events::add)
                .until(() -> true));

        assertThat(events, hasSize(1));
        assertThat(events.get(0).isConditionIsFulfilled(), is(true));
        assertThat(e.getMessage(), equalTo(events.get(0).getDescription()));
    }

    @Test(timeout = 2000)
    public void on_timeout_is_not_called_when_a_fail_fast_condition_fails() {
        AtomicInteger calls = new AtomicInteger();

        try {
            await().atMost(Duration.ofSeconds(1))
                    .failFast("terminal", () -> true)
                    .onTimeout(event -> calls.incrementAndGet())
                    .until(() -> false);
            fail("Expected TerminalFailureException");
        } catch (TerminalFailureException expected) {
            assertThat(calls.get(), is(0));
        }
    }

    @Test(timeout = 2000)
    public void on_timeout_is_not_called_when_the_condition_throws_an_exception_that_is_not_ignored() {
        AtomicInteger calls = new AtomicInteger();

        try {
            await().atMost(Duration.ofSeconds(1))
                    .onTimeout(event -> calls.incrementAndGet())
                    .until(() -> {
                        throw new IllegalStateException("boom");
                    });
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage(), equalTo("boom"));
            assertThat(calls.get(), is(0));
        }
    }

    @Test(timeout = 2000)
    public void condition_timeout_exception_is_thrown_with_the_callback_exception_as_suppressed_when_the_callback_throws() {
        IllegalStateException callbackException = new IllegalStateException("callback failed");

        ConditionTimeoutException e = expectTimeout(() -> shortAwait()
                .onTimeout(event -> {
                    throw callbackException;
                })
                .until(() -> false));

        assertThat(e.getSuppressed(), arrayContaining((Throwable) callbackException));
    }

    @Test(timeout = 2000)
    public void condition_timeout_exception_keeps_its_cause_when_the_callback_throws() {
        ConditionTimeoutException e = expectTimeout(() -> shortAwait()
                .onTimeout(event -> {
                    throw new IllegalStateException("callback failed");
                })
                .untilAsserted(() -> {
                    throw new AssertionError("not yet");
                }));

        assertThat(e.getCause(), instanceOf(AssertionError.class));
        assertThat(e.getCause().getMessage(), equalTo("not yet"));
        assertThat(e.getSuppressed(), arrayWithSize(1));
    }

    // The next three tests are based on tests by Burak KALAYCI in pull request 310

    @Test(timeout = 2000)
    public void on_timeout_is_called_when_the_listener_on_timeout_throws_and_the_listener_exception_is_thrown() {
        IllegalStateException listenerException = new IllegalStateException("listener failed");
        AtomicInteger calls = new AtomicInteger();

        try {
            shortAwait()
                    .conditionEvaluationListener(throwingOnTimeoutListener(listenerException))
                    .onTimeout(event -> calls.incrementAndGet())
                    .until(() -> false);
            fail("Expected the listener's exception");
        } catch (IllegalStateException e) {
            assertThat(e, sameInstance(listenerException));
            assertThat(e.getSuppressed(), arrayWithSize(0));
            assertThat(calls.get(), is(1));
        }
    }

    @Test(timeout = 2000)
    public void callback_exception_is_suppressed_by_the_listener_exception_when_both_throw() {
        IllegalStateException listenerException = new IllegalStateException("listener failed");
        IllegalArgumentException callbackException = new IllegalArgumentException("callback failed");

        try {
            shortAwait()
                    .conditionEvaluationListener(throwingOnTimeoutListener(listenerException))
                    .onTimeout(event -> {
                        throw callbackException;
                    })
                    .until(() -> false);
            fail("Expected the listener's exception");
        } catch (IllegalStateException e) {
            assertThat(e, sameInstance(listenerException));
            assertThat(e.getSuppressed(), arrayContaining((Throwable) callbackException));
        }
    }

    @Test(timeout = 2000)
    public void an_exception_thrown_by_both_the_listener_and_the_callback_is_not_suppressed_on_itself() {
        IllegalStateException sharedException = new IllegalStateException("failed");

        try {
            shortAwait()
                    .conditionEvaluationListener(throwingOnTimeoutListener(sharedException))
                    .onTimeout(event -> {
                        throw sharedException;
                    })
                    .until(() -> false);
            fail("Expected the listener's exception");
        } catch (IllegalStateException e) {
            assertThat(e, sameInstance(sharedException));
            assertThat(e.getSuppressed(), arrayWithSize(0));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void on_timeout_does_not_accept_null() {
        await().onTimeout(null);
    }

    private static ConditionEvaluationListener<Boolean> timeoutCountingListener(AtomicInteger timeouts) {
        return new ConditionEvaluationListener<Boolean>() {
            @Override
            public void conditionEvaluated(EvaluatedCondition<Boolean> condition) {
            }

            @Override
            public void onTimeout(TimeoutEvent timeoutEvent) {
                timeouts.incrementAndGet();
            }
        };
    }

    private static ConditionEvaluationListener<Boolean> throwingOnTimeoutListener(RuntimeException exception) {
        return new ConditionEvaluationListener<Boolean>() {
            @Override
            public void conditionEvaluated(EvaluatedCondition<Boolean> condition) {
            }

            @Override
            public void onTimeout(TimeoutEvent timeoutEvent) {
                throw exception;
            }
        };
    }

    private static ConditionTimeoutException expectTimeout(Runnable await) {
        try {
            await.run();
        } catch (ConditionTimeoutException e) {
            return e;
        }
        throw new AssertionError("Expected ConditionTimeoutException");
    }
}
