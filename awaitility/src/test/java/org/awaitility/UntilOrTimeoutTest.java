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

import org.awaitility.classes.Asynch;
import org.awaitility.classes.ExceptionThrowingAsynch;
import org.awaitility.classes.FakeRepository;
import org.awaitility.classes.FakeRepositoryImpl;
import org.awaitility.core.*;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class UntilOrTimeoutTest {

    private static final Duration SHORT = Duration.ofMillis(200);
    private static final Duration LONG = Duration.ofSeconds(2);

    private FakeRepository fakeRepository;

    @Before
    public void setup() {
        fakeRepository = new FakeRepositoryImpl();
        Awaitility.reset();
    }

    // Matcher form

    @Test(timeout = 2000)
    public void untilOrTimeoutWithMatcherReturnsMatchedValueOnSuccess() {
        new Asynch(fakeRepository).perform();

        AwaitResult<Integer> result = await().atMost(LONG).untilOrTimeout(fakeRepository::getValue, equalTo(1));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(1);
        assertThat(result.getCause()).isNull();
        assertThat(result.orElseThrow()).isEqualTo(1);
    }

    @Test(timeout = 2000)
    public void untilOrTimeoutWithMatcherReturnsLastObservedValueOnTimeout() {
        AtomicInteger counter = new AtomicInteger();

        AwaitResult<Integer> result = await().atMost(SHORT).pollInterval(Duration.ofMillis(10)).untilOrTimeout(counter::incrementAndGet, greaterThan(100_000));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getValue()).isEqualTo(counter.get()).isGreaterThan(1);
        assertThat(result.getCause()).isExactlyInstanceOf(ConditionTimeoutException.class)
                .hasMessageContaining("but <" + result.getValue() + "> was less than");
    }

    @Test(timeout = 2000)
    public void untilOrTimeoutWithMatcherHasSameTimeoutMessageAsUntil() {
        Callable<Integer> supplier = () -> 1;

        AwaitResult<Integer> result = await().atMost(SHORT).untilOrTimeout(supplier, is(2));
        Throwable thrown = catchThrowable(() -> await().atMost(SHORT).until(supplier, is(2)));

        assertThat(result.getValue()).isEqualTo(1);
        assertThat(thrown).isExactlyInstanceOf(ConditionTimeoutException.class);
        assertThat(result.getCause()).hasMessage(thrown.getMessage());
    }

    @Test(timeout = 2000)
    public void untilOrTimeoutWithMatcherAndAliasHasSameTimeoutMessageAsUntil() {
        Callable<Integer> supplier = () -> 1;

        AwaitResult<Integer> result = await("my alias").atMost(SHORT).untilOrTimeout(supplier, is(2));
        Throwable thrown = catchThrowable(() -> await("my alias").atMost(SHORT).until(supplier, is(2)));

        assertThat(result.getCause()).hasMessage(thrown.getMessage()).hasMessageStartingWith("Condition with alias 'my alias'");
    }

    @Test(timeout = 2000)
    public void orElseThrowThrowsTheCauseOnFailure() {
        AwaitResult<Integer> result = await().atMost(SHORT).untilOrTimeout(() -> 1, is(2));

        Throwable thrown = catchThrowable(result::orElseThrow);

        assertThat(thrown).isSameAs(result.getCause());
    }

    // Predicate form

    @Test(timeout = 2000)
    public void untilOrTimeoutWithPredicateReturnsMatchedValueOnSuccess() {
        new Asynch(fakeRepository).perform();

        AwaitResult<Integer> result = await().atMost(LONG).untilOrTimeout(fakeRepository::getValue, value -> value == 1);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(1);
    }

    @Test(timeout = 2000)
    public void untilOrTimeoutWithPredicateHasSameTimeoutMessageAsUntil() {
        Callable<Integer> supplier = () -> 1;

        AwaitResult<Integer> result = await().atMost(SHORT).untilOrTimeout(supplier, value -> value == 2);
        Throwable thrown = catchThrowable(() -> await().atMost(SHORT).until(supplier, value -> value == 2));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getValue()).isEqualTo(1);
        assertThat(result.getCause()).hasMessage(thrown.getMessage())
                .hasMessageContaining("expected the predicate to return <true> but it returned <false> for input of <1>");
    }

    // Boolean form

    @Test(timeout = 2000)
    public void untilOrTimeoutWithBooleanConditionReturnsTrueOnSuccess() {
        new Asynch(fakeRepository).perform();

        AwaitResult<Boolean> result = await().atMost(LONG).untilOrTimeout(() -> fakeRepository.getValue() == 1);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isTrue();
    }

    @Test(timeout = 2000)
    public void untilOrTimeoutWithBooleanConditionReturnsLastBooleanAndSameMessageAsUntilOnTimeout() {
        Callable<Boolean> condition = () -> false;

        AwaitResult<Boolean> result = await().atMost(SHORT).untilOrTimeout(condition);
        Throwable thrown = catchThrowable(() -> await().atMost(SHORT).until(condition));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getValue()).isFalse();
        assertThat(thrown).isExactlyInstanceOf(ConditionTimeoutException.class);
        assertThat(result.getCause()).hasMessage(thrown.getMessage());
    }

    // Assertion form

    @Test(timeout = 2000)
    public void untilAssertedOrTimeoutReturnsSuccessWithNullValue() {
        new Asynch(fakeRepository).perform();

        AwaitResult<Void> result = await().atMost(LONG).untilAssertedOrTimeout(() -> assertThat(fakeRepository.getValue()).isEqualTo(1));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isNull();
        assertThat(result.getCause()).isNull();
    }

    @Test(timeout = 2000)
    public void untilAssertedOrTimeoutHasSameTimeoutMessageAsUntilAsserted() {
        ThrowingRunnable assertion = () -> assertThat(fakeRepository.getValue()).isEqualTo(1);

        AwaitResult<Void> result = await().atMost(SHORT).untilAssertedOrTimeout(assertion);
        Throwable thrown = catchThrowable(() -> await().atMost(SHORT).untilAsserted(assertion));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getValue()).isNull();
        assertThat(thrown).isExactlyInstanceOf(ConditionTimeoutException.class);
        assertThat(result.getCause()).hasMessage(thrown.getMessage());
    }

    @Test(timeout = 2000)
    public void untilAssertedOrTimeoutStillThrowsExceptionsThatAreNotAssertionErrors() {
        Throwable thrown = catchThrowable(() -> await().atMost(LONG).untilAssertedOrTimeout(() -> {
            throw new IllegalStateException("boom");
        }));

        assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class).hasMessage("boom");
    }

    // What still throws

    @Test(timeout = 2000)
    public void failFastStillThrowsTerminalFailureException() {
        Throwable thrown = catchThrowable(() -> await().atMost(LONG).failFast("terminal", () -> true).untilOrTimeout(() -> false));

        assertThat(thrown).isExactlyInstanceOf(TerminalFailureException.class).hasMessage("terminal");
    }

    @Test(timeout = 2000)
    public void failFastAssertionStillThrowsTerminalFailureExceptionFromUntilAssertedOrTimeout() {
        Throwable thrown = catchThrowable(() -> await().atMost(LONG)
                .failFast("terminal", () -> assertThat(true).isFalse())
                .untilAssertedOrTimeout(() -> assertThat(fakeRepository.getValue()).isEqualTo(1)));

        assertThat(thrown).isExactlyInstanceOf(TerminalFailureException.class).hasMessage("terminal");
    }

    @Test(timeout = 2000)
    public void exceptionFromTheSupplierThatIsNotIgnoredStillThrows() {
        Throwable thrown = catchThrowable(() -> await().atMost(LONG).untilOrTimeout(() -> {
            throw new IllegalStateException("boom");
        }, is(2)));

        assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class).hasMessage("boom");
    }

    @Test(timeout = 2000)
    public void exceptionFromTheBooleanConditionThatIsNotIgnoredStillThrows() {
        Throwable thrown = catchThrowable(() -> await().atMost(LONG).untilOrTimeout(() -> {
            throw new IllegalStateException("boom");
        }));

        assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class).hasMessage("boom");
    }

    @Test(timeout = 2000)
    public void ignoredExceptionsLeadToAFailedResultOnTimeout() {
        AwaitResult<Integer> result = await().atMost(SHORT).ignoreExceptions().untilOrTimeout(() -> {
            throw new IllegalStateException("boom");
        }, is(2));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getValue()).isNull();
        assertThat(result.getCause()).isExactlyInstanceOf(ConditionTimeoutException.class);
    }

    @Test(timeout = 2000)
    public void timeoutOfANestedAwaitInsideTheConditionIsThrownAndNotTurnedIntoAFailedResult() {
        Throwable thrown = catchThrowable(() -> await().atMost(LONG).untilOrTimeout(() -> {
            await("inner").atMost(Duration.ofMillis(50)).pollInterval(Duration.ofMillis(10)).until(() -> false);
            return true;
        }));

        assertThat(thrown).isExactlyInstanceOf(ConditionTimeoutException.class).hasMessageContaining("inner");
    }

    @Test(timeout = 2000)
    public void uncaughtExceptionFromOtherThreadStillThrows() {
        new ExceptionThrowingAsynch(new IllegalStateException("Illegal state!")).perform();

        Throwable thrown = catchThrowable(() -> await().atMost(LONG).untilOrTimeout(fakeRepository::getValue, equalTo(1)));

        assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class).hasMessage("Illegal state!");
    }

    @Test(timeout = 2000)
    public void illegalSettingsStillThrowIllegalArgumentException() {
        Throwable thrown = catchThrowable(() -> await().pollDelay(Duration.ofSeconds(1)).atMost(Duration.ofMillis(500)).untilOrTimeout(() -> true));

        assertThat(thrown).isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Timeout (500 milliseconds) must be greater than the poll delay");
    }

    // atLeast

    @Test(timeout = 2000)
    public void atLeastViolationIsAFailedResult() {
        AwaitResult<Boolean> result = await().atLeast(Duration.ofMillis(500)).atMost(LONG).untilOrTimeout(() -> true);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getValue()).isTrue();
        assertThat(result.getCause()).isExactlyInstanceOf(ConditionTimeoutException.class)
                .hasMessageStartingWith("Condition was evaluated in").hasMessageEndingWith("which is earlier than expected minimum timeout 500 milliseconds");
    }

    @Test(timeout = 2000)
    public void atLeastViolationIsAFailedResultWithTheTooEarlyValue() {
        AwaitResult<Integer> result = await().atLeast(Duration.ofMillis(500)).atMost(LONG).untilOrTimeout(() -> 2, is(2));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getValue()).isEqualTo(2);
        assertThat(result.getCause()).hasMessageStartingWith("Condition was evaluated in");
    }

    // Listener

    @Test(timeout = 2000)
    public void onTimeoutListenerIsCalledOnceOnTimeout() {
        AtomicInteger onTimeoutCalls = new AtomicInteger();
        ConditionEvaluationListener<Integer> listener = new ConditionEvaluationListener<Integer>() {
            @Override
            public void conditionEvaluated(EvaluatedCondition<Integer> condition) {
            }

            @Override
            public void onTimeout(TimeoutEvent timeoutEvent) {
                onTimeoutCalls.incrementAndGet();
            }
        };

        AwaitResult<Integer> result = await().atMost(SHORT).conditionEvaluationListener(listener).untilOrTimeout(() -> 1, is(2));

        assertThat(result.isSuccess()).isFalse();
        assertThat(onTimeoutCalls).hasValue(1);
    }

    @Test(timeout = 2000)
    public void onTimeoutListenerIsNotCalledOnSuccess() {
        AtomicInteger onTimeoutCalls = new AtomicInteger();
        ConditionEvaluationListener<Object> listener = new ConditionEvaluationListener<Object>() {
            @Override
            public void conditionEvaluated(EvaluatedCondition<Object> condition) {
            }

            @Override
            public void onTimeout(TimeoutEvent timeoutEvent) {
                onTimeoutCalls.incrementAndGet();
            }
        };

        AwaitResult<Boolean> result = await().atMost(LONG).conditionEvaluationListener(listener).untilOrTimeout(() -> true);

        assertThat(result.isSuccess()).isTrue();
        assertThat(onTimeoutCalls).hasValue(0);
    }
}
