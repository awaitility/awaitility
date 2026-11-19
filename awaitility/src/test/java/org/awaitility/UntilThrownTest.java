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

import org.awaitility.core.ConditionTimeoutException;
import org.junit.After;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.awaitility.Awaitility.await;

/**
 * Tests for untilThrown and untilThrownExactly (issue 129).
 */
public class UntilThrownTest {

    private static final Duration GENEROUS = Duration.ofSeconds(5);
    private static final Duration SHORT = Duration.ofMillis(300);
    private static final Duration FAST_POLL = Duration.ofMillis(10);

    @After
    public void reset_awaitility_after_each_test() {
        Awaitility.reset();
    }

    @Test(timeout = 2000)
    public void until_thrown_returns_the_exception_thrown_on_the_third_call() {
        AtomicInteger calls = new AtomicInteger();
        IOException expected = new IOException("expected");

        IOException actual = await().atMost(GENEROUS).pollInterval(FAST_POLL).untilThrown(() -> {
            if (calls.incrementAndGet() == 3) {
                throw expected;
            }
            return calls.get();
        }, IOException.class);

        assertThat(actual).isSameAs(expected);
        assertThat(calls).hasValue(3);
    }

    @Test(timeout = 2000)
    public void until_thrown_with_throwing_runnable_returns_the_exception_thrown_on_the_third_call() {
        AtomicInteger calls = new AtomicInteger();

        IllegalStateException actual = await().atMost(GENEROUS).pollInterval(FAST_POLL).untilThrown(() -> {
            if (calls.incrementAndGet() == 3) {
                throw new IllegalStateException("third");
            }
        }, IllegalStateException.class);

        assertThat(actual).hasMessage("third");
        assertThat(calls).hasValue(3);
    }

    @Test(timeout = 2000)
    public void until_thrown_matches_a_subclass_of_the_expected_type() {
        AtomicInteger calls = new AtomicInteger();

        IOException actual = await().atMost(GENEROUS).pollInterval(FAST_POLL).untilThrown(() -> {
            if (calls.incrementAndGet() == 3) {
                throw new FileNotFoundException("subclass");
            }
            return calls.get();
        }, IOException.class);

        assertThat(actual).isInstanceOf(FileNotFoundException.class).hasMessage("subclass");
    }

    @Test(timeout = 2000)
    public void until_thrown_exactly_does_not_match_a_subclass_so_the_subclass_propagates_like_any_other_exception() {
        AtomicInteger calls = new AtomicInteger();

        Throwable thrown = catchThrowable(() -> await().atMost(GENEROUS).pollInterval(FAST_POLL).untilThrownExactly(() -> {
            calls.incrementAndGet();
            throw new FileNotFoundException("subclass");
        }, IOException.class));

        assertThat(thrown).isInstanceOf(FileNotFoundException.class).hasMessage("subclass");
        assertThat(calls).hasValue(1);
    }

    @Test(timeout = 2000)
    public void until_thrown_exactly_keeps_polling_past_an_ignored_subclass_until_the_exact_type_is_thrown() {
        AtomicInteger calls = new AtomicInteger();

        IOException actual = await().atMost(GENEROUS).pollInterval(FAST_POLL).ignoreException(FileNotFoundException.class).untilThrownExactly(() -> {
            if (calls.incrementAndGet() < 3) {
                throw new FileNotFoundException("subclass");
            }
            throw new IOException("exact");
        }, IOException.class);

        assertThat(actual.getClass()).isEqualTo(IOException.class);
        assertThat(actual).hasMessage("exact");
        assertThat(calls).hasValue(3);
    }

    @Test(timeout = 2000)
    public void until_thrown_propagates_a_non_matching_exception_immediately_when_it_is_not_ignored() {
        AtomicInteger calls = new AtomicInteger();

        Throwable thrown = catchThrowable(() -> await().atMost(GENEROUS).pollInterval(FAST_POLL).untilThrown(() -> {
            calls.incrementAndGet();
            throw new IllegalArgumentException("not the one");
        }, IOException.class));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessage("not the one");
        assertThat(calls).hasValue(1);
    }

    @Test(timeout = 2000)
    public void until_thrown_keeps_polling_when_a_non_matching_exception_is_ignored() {
        AtomicInteger calls = new AtomicInteger();

        IOException actual = await().atMost(GENEROUS).pollInterval(FAST_POLL).ignoreException(IllegalArgumentException.class).untilThrown(() -> {
            if (calls.incrementAndGet() < 3) {
                throw new IllegalArgumentException("not yet");
            }
            throw new IOException("now");
        }, IOException.class);

        assertThat(actual).hasMessage("now");
        assertThat(calls).hasValue(3);
    }

    @Test(timeout = 2000)
    public void the_expected_exception_wins_even_when_all_exceptions_are_ignored() {
        IOException actual = await().atMost(GENEROUS).pollInterval(FAST_POLL).ignoreExceptions().untilThrown(() -> {
            throw new IOException("expected");
        }, IOException.class);

        assertThat(actual).hasMessage("expected");
    }

    @Test(timeout = 2000)
    public void timeout_message_names_the_expected_exception_and_the_last_returned_value() {
        AtomicInteger calls = new AtomicInteger();

        Throwable thrown = catchThrowable(() -> await().atMost(SHORT).pollInterval(FAST_POLL).untilThrown(calls::incrementAndGet, IOException.class));

        assertThat(thrown).isInstanceOf(ConditionTimeoutException.class);
        assertThat(thrown.getMessage())
                .startsWith("Condition with Lambda expression in " + UntilThrownTest.class.getName())
                .contains(" was expected to throw java.io.IOException but it returned ")
                .endsWith(" within 300 milliseconds.");
    }

    @Test(timeout = 2000)
    public void timeout_message_for_throwing_runnable_says_it_returned_normally() {
        Throwable thrown = catchThrowable(() -> await().atMost(SHORT).pollInterval(FAST_POLL).untilThrown(() -> {
        }, IOException.class));

        assertThat(thrown).isInstanceOf(ConditionTimeoutException.class);
        assertThat(thrown.getMessage()).contains(" was expected to throw java.io.IOException but it returned normally within 300 milliseconds.");
    }

    @Test(timeout = 2000)
    public void timeout_message_includes_the_last_ignored_exception_and_uses_it_as_cause() {
        Throwable thrown = catchThrowable(() -> await().atMost(SHORT).pollInterval(FAST_POLL).ignoreException(FileNotFoundException.class).untilThrownExactly(() -> {
            throw new FileNotFoundException("subclass");
        }, IOException.class));

        assertThat(thrown).isInstanceOf(ConditionTimeoutException.class);
        assertThat(thrown.getMessage()).contains(" was expected to throw exactly java.io.IOException but it threw java.io.FileNotFoundException: subclass within 300 milliseconds.");
        assertThat(thrown.getCause()).isInstanceOf(FileNotFoundException.class);
    }

    @Test(timeout = 2000)
    public void timeout_message_with_alias() {
        Throwable thrown = catchThrowable(() -> await("connection").atMost(SHORT).pollInterval(FAST_POLL).untilThrown(() -> 42, IOException.class));

        assertThat(thrown).isInstanceOf(ConditionTimeoutException.class);
        assertThat(thrown.getMessage())
                .startsWith("Condition with alias 'connection' didn't complete within 300 milliseconds because condition with Lambda expression in " + UntilThrownTest.class.getName())
                .endsWith(" was expected to throw java.io.IOException but it returned 42.");
    }
}
