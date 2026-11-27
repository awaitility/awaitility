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

import org.awaitility.core.TerminalFailureException;
import org.awaitility.core.ThrowingRunnable;
import org.junit.After;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.awaitility.Awaitility.await;

/**
 * Fail fast with a failure reason that is created lazily from a {@link Supplier} (issue 240).
 */
public class FailFastReasonSupplierTest {

    private static final Duration GENEROUS = Duration.ofSeconds(5);
    private static final Duration FAST_POLL = Duration.ofMillis(10);

    // A typed variable, since a lambda that always throws would otherwise resolve to the Callable<Boolean> overload
    private static final ThrowingRunnable phaseWasFailed = () -> {
        throw new AssertionError("phase was Failed");
    };

    @After
    public void reset_awaitility_after_each_test() {
        Awaitility.reset();
    }

    @Test(timeout = 2000)
    public void reason_from_supplier_is_the_terminal_failure_exception_message_and_can_include_current_state() {
        AtomicInteger evaluations = new AtomicInteger();

        Throwable thrown = catchThrowable(() -> await().atMost(GENEROUS).pollInterval(FAST_POLL)
                .failFastWithReason(() -> "Gave up after " + evaluations.get() + " evaluations", () -> evaluations.incrementAndGet() >= 3)
                .until(() -> false));

        assertThat(thrown).isExactlyInstanceOf(TerminalFailureException.class).hasMessage("Gave up after 3 evaluations").hasNoCause();
    }

    @Test(timeout = 2000)
    public void reason_supplier_is_not_called_when_fail_fast_condition_never_triggers() {
        AtomicInteger supplierCalls = new AtomicInteger();
        AtomicInteger polls = new AtomicInteger();

        await().atMost(GENEROUS).pollInterval(FAST_POLL)
                .failFastWithReason(() -> {
                    supplierCalls.incrementAndGet();
                    return "should not be used";
                }, () -> false)
                .until(() -> polls.incrementAndGet() >= 3);

        assertThat(supplierCalls).hasValue(0);
    }

    @Test(timeout = 2000)
    public void reason_supplier_is_called_once_when_fail_fast_condition_triggers() {
        AtomicInteger supplierCalls = new AtomicInteger();
        AtomicInteger evaluations = new AtomicInteger();

        catchThrowable(() -> await().atMost(GENEROUS).pollInterval(FAST_POLL)
                .failFastWithReason(() -> "call " + supplierCalls.incrementAndGet(), () -> evaluations.incrementAndGet() >= 3)
                .until(() -> false));

        assertThat(supplierCalls).hasValue(1);
    }

    @Test(timeout = 2000)
    public void terminal_failure_exception_is_still_thrown_when_reason_supplier_throws() {
        IllegalStateException supplierFailure = new IllegalStateException("boom");

        Throwable thrown = catchThrowable(() -> await().atMost(GENEROUS).pollInterval(FAST_POLL)
                .failFastWithReason(() -> {
                    throw supplierFailure;
                }, () -> true)
                .until(() -> false));

        assertThat(thrown).isExactlyInstanceOf(TerminalFailureException.class)
                .hasMessage("Fail fast condition triggered (the fail fast failure reason supplier threw java.lang.IllegalStateException: boom)")
                .hasNoCause();
        assertThat(thrown.getSuppressed()).containsExactly(supplierFailure);
    }

    @Test(timeout = 2000)
    public void default_reason_is_used_when_reason_supplier_returns_null() {
        Throwable thrown = catchThrowable(() -> await().atMost(GENEROUS).pollInterval(FAST_POLL)
                .failFastWithReason(() -> null, () -> true)
                .until(() -> false));

        assertThat(thrown).isExactlyInstanceOf(TerminalFailureException.class).hasMessage("Fail fast condition triggered");
    }

    @Test(timeout = 2000)
    public void reason_from_supplier_is_used_for_fail_fast_assertion_and_the_assertion_error_is_the_cause() {
        AtomicInteger evaluations = new AtomicInteger();

        Throwable thrown = catchThrowable(() -> await().atMost(GENEROUS).pollInterval(FAST_POLL)
                .failFastWithReason(() -> "Gave up after " + evaluations.get() + " evaluations", () -> assertThat(evaluations.incrementAndGet()).isLessThan(3))
                .until(() -> false));

        assertThat(thrown).isExactlyInstanceOf(TerminalFailureException.class).hasMessage("Gave up after 3 evaluations")
                .hasCauseInstanceOf(AssertionError.class);
    }

    @Test(timeout = 2000)
    public void reason_supplier_is_not_called_when_fail_fast_assertion_never_fails() {
        AtomicInteger supplierCalls = new AtomicInteger();
        AtomicInteger polls = new AtomicInteger();

        await().atMost(GENEROUS).pollInterval(FAST_POLL)
                .failFastWithReason(() -> {
                    supplierCalls.incrementAndGet();
                    return "should not be used";
                }, () -> assertThat(true).isTrue())
                .until(() -> polls.incrementAndGet() >= 3);

        assertThat(supplierCalls).hasValue(0);
    }

    @Test(timeout = 2000)
    public void fail_fast_assertion_still_throws_terminal_failure_exception_when_reason_supplier_throws() {
        IllegalStateException supplierFailure = new IllegalStateException("boom");

        Throwable thrown = catchThrowable(() -> await().atMost(GENEROUS).pollInterval(FAST_POLL)
                .failFastWithReason(() -> {
                    throw supplierFailure;
                }, phaseWasFailed)
                .until(() -> false));

        assertThat(thrown).isExactlyInstanceOf(TerminalFailureException.class)
                .hasMessage("phase was Failed (the fail fast failure reason supplier threw java.lang.IllegalStateException: boom)")
                .hasCauseInstanceOf(AssertionError.class);
        assertThat(thrown.getSuppressed()).containsExactly(supplierFailure);
    }

    @Test(timeout = 2000)
    public void assertion_message_is_used_when_reason_supplier_for_fail_fast_assertion_returns_null() {
        Throwable thrown = catchThrowable(() -> await().atMost(GENEROUS).pollInterval(FAST_POLL)
                .failFastWithReason(() -> null, phaseWasFailed)
                .until(() -> false));

        assertThat(thrown).isExactlyInstanceOf(TerminalFailureException.class).hasMessage("phase was Failed");
    }

    @Test(timeout = 2000)
    public void null_reason_supplier_behaves_like_a_null_string_reason() {
        Supplier<String> noReason = null;

        Throwable callable = catchThrowable(() -> await().atMost(GENEROUS).pollInterval(FAST_POLL)
                .failFastWithReason(noReason, () -> true)
                .until(() -> false));
        Throwable assertion = catchThrowable(() -> await().atMost(GENEROUS).pollInterval(FAST_POLL)
                .failFastWithReason(noReason, phaseWasFailed)
                .until(() -> false));

        assertThat(callable).isExactlyInstanceOf(TerminalFailureException.class).hasMessage("Fail fast condition triggered");
        assertThat(assertion).isExactlyInstanceOf(TerminalFailureException.class).hasMessage("phase was Failed")
                .hasCauseInstanceOf(AssertionError.class);
    }

    @Test(timeout = 2000)
    public void string_reason_call_sites_with_inline_lambdas_still_resolve_to_the_string_overloads() {
        Throwable callable = catchThrowable(() -> await().atMost(GENEROUS).pollInterval(FAST_POLL)
                .failFast("static reason", () -> true)
                .until(() -> false));
        Throwable assertion = catchThrowable(() -> await().atMost(GENEROUS).pollInterval(FAST_POLL)
                .failFast("static reason", () -> assertThat(1).isEqualTo(2))
                .until(() -> false));

        assertThat(callable).isExactlyInstanceOf(TerminalFailureException.class).hasMessage("static reason");
        assertThat(assertion).isExactlyInstanceOf(TerminalFailureException.class).hasMessage("static reason");
    }

    @Test
    public void null_literal_string_reason_still_compiles_and_behaves_as_before_for_callable() {
        Callable<Boolean> condition = () -> true;

        // Regression guard: the plain null literal must stay unambiguous (it would not compile next to a Supplier<String> overload)
        Throwable plainNull = catchThrowable(() -> await().failFast(null, condition));
        Throwable castNull = catchThrowable(() -> await().failFast((String) null, condition));

        assertThat(plainNull).isInstanceOf(IllegalArgumentException.class).hasMessage("failFastFailureReason cannot be null");
        assertThat(castNull).isInstanceOf(IllegalArgumentException.class).hasMessage("failFastFailureReason cannot be null");
    }

    @Test(timeout = 2000)
    public void null_literal_string_reason_still_compiles_and_behaves_as_before_for_throwing_runnable() {
        // Regression guard: the plain null literal must stay unambiguous and use the assertion's message
        Throwable plainNull = catchThrowable(() -> await().atMost(GENEROUS).pollInterval(FAST_POLL)
                .failFast(null, phaseWasFailed)
                .until(() -> false));
        Throwable castNull = catchThrowable(() -> await().atMost(GENEROUS).pollInterval(FAST_POLL)
                .failFast((String) null, phaseWasFailed)
                .until(() -> false));

        assertThat(plainNull).isExactlyInstanceOf(TerminalFailureException.class).hasMessage("phase was Failed");
        assertThat(castNull).isExactlyInstanceOf(TerminalFailureException.class).hasMessage("phase was Failed");
    }
}
