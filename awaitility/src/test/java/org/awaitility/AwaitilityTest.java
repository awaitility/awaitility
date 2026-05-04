/*
 * Copyright 2016 the original author or authors.
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

import org.assertj.core.api.Assertions;
import org.awaitility.classes.*;
import org.awaitility.core.*;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.platform.commons.JUnitException;
import org.opentest4j.AssertionFailedError;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.*;
import static org.awaitility.Awaitility.*;
import static org.awaitility.Durations.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;

class AwaitilityTest {

    private FakeRepository fakeRepository;

    @BeforeEach
    void setup() {
        fakeRepository = new FakeRepositoryImpl();
        Awaitility.reset();
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitOperationBlocksAutomatically() {
        new Asynch(fakeRepository).perform();
        await().until(fakeRepositoryValueEqualsOne());
        assertEquals(1, fakeRepository.getValue());
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitOperationWithConsumerMatcher() {
        new Asynch(fakeRepository).perform();
        await().untilAsserted(fakeRepository::getValue, value -> Assertions.assertThat(value).isEqualTo(1));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitOperationSupportsSpecifyingPollIntervalUsingTimeunit() {
        new Asynch(fakeRepository).perform();
        with().pollInterval(20, TimeUnit.MILLISECONDS).await().until(fakeRepositoryValueEqualsOne());
        given().pollInterval(20, TimeUnit.MILLISECONDS).await().until(fakeRepositoryValueEqualsOne());
        assertEquals(1, fakeRepository.getValue());
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitOperationSupportsSpecifyingPollInterval() {
        new Asynch(fakeRepository).perform();
        with().pollInterval(ONE_HUNDRED_MILLISECONDS).then().await().until(fakeRepositoryValueEqualsOne());
        assertEquals(1, fakeRepository.getValue());
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitOperationSupportsSpecifyingZeroAsPollDelay() {
        new Asynch(fakeRepository).perform();
        with().pollDelay(Duration.ZERO).pollInterval(ONE_HUNDRED_MILLISECONDS).then().await().until(fakeRepositoryValueEqualsOne());
        assertEquals(1, fakeRepository.getValue());
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitOperationSupportsSpecifyingZeroAsPollInterval() {
        new Asynch(fakeRepository).perform();
        with().pollDelay(TWO_HUNDRED_MILLISECONDS).pollInterval(Duration.ZERO).then().await().until(fakeRepositoryValueEqualsOne());
        assertEquals(1, fakeRepository.getValue());
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitOperationDoesntSupportSpecifyingForeverAsPollDelay() {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            new Asynch(fakeRepository).perform();
            with().pollDelay(ForeverDuration.FOREVER).pollInterval(ONE_HUNDRED_MILLISECONDS).then().await().until(fakeRepositoryValueEqualsOne());
            assertEquals(1, fakeRepository.getValue());
        });
        assertThat(exception.getMessage(), containsString("Cannot delay polling forever"));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitOperationDoesntSupportSpecifyingForeverAsPollInterval() {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            new Asynch(fakeRepository).perform();
            with().pollDelay(ONE_HUNDRED_MILLISECONDS).pollInterval(ForeverDuration.FOREVER).then().await().until(fakeRepositoryValueEqualsOne());
            assertEquals(1, fakeRepository.getValue());
        });
        assertThat(exception.getMessage(), containsString("Cannot use a fixed poll interval of length 'forever'"));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitOperationSupportsSpecifyingPollDelay() {
        new Asynch(fakeRepository).perform();
        with().pollDelay(ONE_HUNDRED_MILLISECONDS).await().until(fakeRepositoryValueEqualsOne());
        assertEquals(1, fakeRepository.getValue());
    }

    @Test
    @Timeout(value = 2000L, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitOperationSupportsDefaultTimeout() {
        Awaitility.setDefaultTimeout(120, TimeUnit.MILLISECONDS);
        assertThrows(ConditionTimeoutException.class, () -> await().until(value(), greaterThan(0)));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void foreverConditionSpecificationUsingUntilWithDirectBlock() {
        new Asynch(fakeRepository).perform();
        await().forever().until(fakeRepositoryValueEqualsOne());
        assertEquals(1, fakeRepository.getValue());
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void foreverConditionWithHamcrestMatchersWithDirectBlock() {
        new Asynch(fakeRepository).perform();
        await().forever().until(value(), equalTo(1));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void foreverConditionWithHamcrestCollectionMatchersWithDirectBlock() {
        new Asynch(fakeRepository).perform();
        await().forever().until(valueAsList(), hasItem(1));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test
    @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void throwsTimeoutExceptionWhenDoneEarlierThanAtLeastConstraint() {
        new Asynch(fakeRepository).perform();
        assertThrows(ConditionTimeoutException.class, () -> await().atLeast(1, SECONDS).and().atMost(2, SECONDS).until(value(), equalTo(1)));
    }

    @Test
    @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void doesNotThrowTimeoutExceptionWhenDoneLaterThanAtLeastConstraint() {
        new Asynch(fakeRepository).perform();
        await().atLeast(100, NANOSECONDS).until(value(), equalTo(1));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void specifyingDefaultPollIntervalImpactsAllSubsequentUndefinedPollIntervalStatements() {
        Awaitility.setDefaultPollInterval(20, TimeUnit.MILLISECONDS);
        new Asynch(fakeRepository).perform();
        await().until(value(), equalTo(1));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void conditionBreaksAfterDurationTimeout() {
        new Asynch(fakeRepository).perform();
        assertThrows(ConditionTimeoutException.class, () -> await().atMost(200, TimeUnit.MILLISECONDS).until(value(), equalTo(1)));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void remainingTimeIsNegativeOrZeroAfterDurationTimeouts() {
        new Asynch(fakeRepository).perform();
        ConditionEvaluationListener<Integer> conditionEvaluationListener = new ConditionEvaluationListener<Integer>() {
            @Override
            public void conditionEvaluated(EvaluatedCondition<Integer> condition) {
            }

            @Override
            public void onTimeout(TimeoutEvent timeoutEvent) {
                assertThat(timeoutEvent.getRemainingTimeInMS(), lessThanOrEqualTo(0L));
            }
        };
        assertThrows(ConditionTimeoutException.class, () -> await().pollDelay(50, MILLISECONDS).conditionEvaluationListener(conditionEvaluationListener).atMost(100, TimeUnit.MILLISECONDS).until(value(), equalTo(1)));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void uncaughtExceptionsArePropagatedToAwaitingThreadAndBreaksForeverBlockWhenSetToCatchAllUncaughtExceptions() {
        catchUncaughtExceptionsByDefault();
        new ExceptionThrowingAsynch(new IllegalStateException("Illegal state!")).perform();
        assertThrows(IllegalStateException.class, () -> await().forever().until(value(), equalTo(1)));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void uncaughtThrowablesArePropagatedToAwaitingThreadAndBreaksForeverBlockWhenSetToCatchAllUncaughtExceptions() {
        new ExceptionThrowingAsynch(new AssertionFailedError("Message", "Something", "Something else")).perform();
        assertThrows(AssertionFailedError.class, () -> await().forever().until(value(), equalTo(1)));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void uncaughtThrowablesFromOtherThreadsCanBeIgnored() throws Exception {
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        final AtomicBoolean exceptionThrown = new AtomicBoolean(false);
        final ExecutorService es = Executors.newFixedThreadPool(5);
        final FakeRepository fakeRepository = new FakeRepository() {

            @Override
            public int getValue() {
                int value = atomicInteger.get();
                if (value < 3) {
                    exceptionThrown.set(true);
                    throw new IllegalArgumentException("Error!");
                }
                return value;
            }

            @Override
            public void setValue(int value) {

                atomicInteger.set(value);
            }
        };
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (atomicInteger.get() < 3) {
                    try {
                        es.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(50);
                                    fakeRepository.setValue(atomicInteger.incrementAndGet());

                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }).get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        thread.start();

        try {
            given().pollDelay(0, MILLISECONDS).and().ignoreExceptions().await().until(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    return fakeRepository.getValue() == 3;
                }
            });
            assertEquals(3, atomicInteger.get());
            assertTrue(exceptionThrown.get());
        } finally {
            thread.join();
            es.shutdownNow();
        }
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void ignoredExceptionsAreAddedToExceptionHierarchy() {
        try {
            await().ignoreExceptions().atMost(200, TimeUnit.MILLISECONDS).until(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    throw new Exception("Nested");
                }
            });
            org.junit.Assert.fail();
        } catch (ConditionTimeoutException e) {
            assertNotNull(e.getCause());
            assertEquals("Nested", e.getCause().getMessage());
        }
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void uncaughtExceptionsArePropagatedToAwaitingThreadAndBreaksForeverBlockWhenCatchingAllUncaughtExceptions() {
        new ExceptionThrowingAsynch(new IllegalStateException("Illegal state!")).perform();
        assertThrows(IllegalStateException.class, () -> catchUncaughtExceptions().and().await().forever().until(value(), equalTo(1)));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void whenDontCatchUncaughtExceptionsIsSpecifiedThenExceptionsFromOtherThreadsAreNotCaught() throws Exception {
        assertThrows(ConditionTimeoutException.class, () -> {
            new AssertExceptionThrownInAnotherThreadButNeverCaughtByAnyThreadTest() {
                @Override public void testLogic() {
                    new ExceptionThrowingAsynch(new IllegalStateException("Illegal state!")).perform();
                    dontCatchUncaughtExceptions().and().await().atMost(ONE_SECOND).until(value(), equalTo(1));
                }
            };
        });
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void whenDontCatchUncaughtExceptionsIsSpecifiedAndTheBuildOfTheAwaitStatementHasStartedThenExceptionsFromOtherThreadsAreNotCaught()
            throws Exception {
        assertThrows(ConditionTimeoutException.class, () -> {
            new AssertExceptionThrownInAnotherThreadButNeverCaughtByAnyThreadTest() {
                @Override public void testLogic() {
                    new ExceptionThrowingAsynch(new IllegalStateException("Illegal state!")).perform();
                    await().and().dontCatchUncaughtExceptions().given().timeout(ONE_SECOND).until(value(), equalTo(1));
                }
            };
        });
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void catchUncaughtExceptionsIsReset() throws Exception {
        assertThrows(ConditionTimeoutException.class, () -> {
            new AssertExceptionThrownInAnotherThreadButNeverCaughtByAnyThreadTest() {
                @Override public void testLogic() {
                    new ExceptionThrowingAsynch(new IllegalStateException("Illegal state!")).perform();
                    dontCatchUncaughtExceptions().and().await().atMost(ONE_SECOND).until(value(), equalTo(1));
                }
            };
        });
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void exceptionsInConditionsArePropagatedToAwaitingThreadAndBreaksForeverBlock() {
        final ExceptionThrowingFakeRepository repository = new ExceptionThrowingFakeRepository();
        new Asynch(repository).perform();
        assertThrows(IllegalStateException.class, () -> await().until(new FakeRepositoryValue(repository), equalTo(1)));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitWithAliasDisplaysAliasWhenConditionTimeoutExceptionOccurs() {
        String alias = "test";
        Throwable exception = assertThrows(ConditionTimeoutException.class, () ->
            await(alias).atMost(120, MILLISECONDS).until(value(), greaterThan(0)));
        assertThat(exception.getMessage(), containsString("Condition with alias 'test' didn't complete within 120 milliseconds because org.awaitility.classes.FakeRepositoryValue expected a value greater than <0> but <0> was equal to <0>."));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitWithAliasDisplaysAliasWhenConditionTimeoutExceptionAndConditionIsACallableOccurs() {
        String alias = "test";
        Throwable exception = assertThrows(ConditionTimeoutException.class, () ->
            //noinspection Convert2Lambda - This is because we want to try a real Callable at least once in the test suite
            await(alias).atMost(120, MILLISECONDS).until(new Callable<Boolean>() {
                public Boolean call() {
                    return fakeRepository.getValue() > 0;
                }
            }));
        assertThat(exception.getMessage(), matchesPattern("Condition with alias 'test' didn't complete within 120 milliseconds because condition returned by method \\\"lambda\\$awaitWithAliasDisplaysAliasWhenConditionTimeoutExceptionAndConditionIsACallableOccurs\\$\\d+\\\" in class org.awaitility.AwaitilityTest was not fulfilled."));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitDisplaysSupplierAndMatcherMismatchMessageWhenConditionTimeoutExceptionOccurs() {
        Throwable exception = assertThrows(ConditionTimeoutException.class, () ->
            with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).until(value(), greaterThan(0)));
        assertThat(exception.getMessage(), containsString(FakeRepositoryValue.class.getName()
                + " expected a value greater than <0> but <0> was equal to <0> within 120 milliseconds."));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitDisplaysCallableNameWhenConditionTimeoutExceptionOccurs() {
        Throwable exception = assertThrows(ConditionTimeoutException.class, () ->
            await().atMost(120, MILLISECONDS).until(fakeRepositoryValueEqualsOne()));
        assertThat(exception.getMessage(), containsString(String.format("Condition %s was not fulfilled within 120 milliseconds.",
                FakeRepositoryEqualsOne.class.getName())));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitDisplaysMethodDeclaringTheCallableWhenCallableIsAnonymousClassAndConditionTimeoutExceptionOccurs() {
        Throwable exception = assertThrows(ConditionTimeoutException.class, () ->
            await().atMost(120, MILLISECONDS).until(fakeRepositoryValueEqualsOneAsAnonymous()));
        assertThat(exception.getMessage(), containsString(String
                .format("Condition returned by method \"fakeRepositoryValueEqualsOneAsAnonymous\" in class %s was not fulfilled within 120 milliseconds.",
                        AwaitilityTest.class.getName())));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitDisplaysMethodDeclaringTheSupplierWhenSupplierIsAnonymousClassAndConditionTimeoutExceptionOccurs() {
        Throwable exception = assertThrows(ConditionTimeoutException.class, () ->
            with().pollInterval(10, MILLISECONDS).await().atMost(120, MILLISECONDS).until(valueAsAnonymous(), equalTo(2)));
        assertThat(exception.getMessage(), containsString(String
                .format("%s.valueAsAnonymous Callable expected %s but was <0> within 120 milliseconds.",
                        AwaitilityTest.class.getName(), equalTo(2).toString())));
    }

    @SuppressWarnings("unchecked")
    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitDisplaysMethodDeclaringTheSupplierWhenSupplierIsAnonymousClassAndConditionTimeoutExceptionOccursWhenUsingNanos() {
        Throwable exception = assertThrows(ConditionTimeoutException.class, () ->
            with().pollInterval(10, NANOSECONDS).await().atMost(120, NANOSECONDS).until(valueAsAnonymous(), equalTo(2)));
        assertThat(exception.getMessage(), Matchers.anyOf(Stream.of(equalTo(0).toString(), "null")
                .map(s -> String.format("%s.valueAsAnonymous Callable expected %s but was %s within 120 nanoseconds.", AwaitilityTest.class.getName(), equalTo(2).toString(), s))
                .map(Matchers::containsString)
                .toArray(Matcher[]::new)));
    }

    @Test
    void noArithmeticExceptionIsThrownWhenConvertingASaneAmountOfDaysToNanos() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> // We don't actually want to wait for 10 days, just that we don't get an ArithmeticException when converting days to nanos
            assertTimeoutPreemptively(Duration.ofMillis(500),
                    () -> with().pollInterval(10, DAYS).await().atMost(120, DAYS).until(valueAsAnonymous(), equalTo(2))
            )
        );
        assertInstanceOf(JUnitException.class, error.getCause());
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void throwsNiceExceptionWhenPollDurationIsSpecifiedAsNanosAndIsLessThanPollInterval() {
        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
            with().pollInterval(10, MILLISECONDS).await().atMost(122, NANOSECONDS).until(valueAsAnonymous(), equalTo(2)));
        assertThat(exception.getMessage(), containsString("Timeout (122 nanoseconds) must be greater than the poll delay (10 milliseconds)."));
    }

    @Test
    void awaitilityThrowsIllegalArgumentExceptionWhenTimeoutIsLessThanPollDelay() {
        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
            with().pollDelay(10, MINUTES).await().atMost(10, SECONDS).until(fakeRepositoryValueEqualsOne()));
        assertThat(exception.getMessage(), is("Timeout (10 seconds) must be greater than the poll delay (10 minutes)."));
    }

    @Test
    void awaitilityThrowsIllegalArgumentExceptionWhenTimeoutIsEqualToPollDelay() {
        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
            with().with().pollDelay(20, MILLISECONDS).pollDelay(200, MILLISECONDS).await().atMost(200, MILLISECONDS).until(fakeRepositoryValueEqualsOne()));
        assertThat(exception.getMessage(), is("Timeout (200 milliseconds) must be greater than the poll delay (200 milliseconds)."));
    }

    @Test
    @Timeout(value = 2000L, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void rethrowsExceptionsInCallable() {
        assertThrows(IllegalStateException.class, () ->
            await().atMost(1, TimeUnit.SECONDS)
                    .until(() -> {
                        throw new IllegalStateException("Hello");
                    }));
    }

    @Test
    @Timeout(value = 2000L, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitDuringTimeOnCondition() throws Exception {
        Duration duration = measureDuration(() ->
            await()
                .during(1, SECONDS)
                .until(() -> true)
        );

        assertThat(duration.toMillis(), greaterThan(1000L));
    }

    @Test
    @Timeout(value = 2500L, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitDuringTimeWillWaitTheTimeStartingWhenTheConditionHolds() throws Exception {
        long startTime = System.currentTimeMillis();

        Duration duration = measureDuration(() ->
            await()
                .during(1000, MILLISECONDS)
                .until(() ->
                    (System.currentTimeMillis() - startTime) > 500L
                )
        );

        assertThat(duration.toMillis(), greaterThan(1500L));
    }

    @Test
    @Timeout(value = 2000L, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitDuringTimeWillThrowExceptionWhenTimeOut() {
        assertThrows(ConditionTimeoutException.class, () ->
            await()
                    .atMost(1000, MILLISECONDS)
                    .during(200, MILLISECONDS)
                    .until(() -> false));
    }

    @Test
    @Timeout(value = 2000L, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitDuringTimeWillThrowExceptionWhenTimeOutEvenIfConditionIsOkAtTheEndButNotDuringPeriod() {
        long startTime = System.currentTimeMillis();

        assertThrows(ConditionTimeoutException.class, () ->
            await()
                    .atMost(1500, MILLISECONDS)
                    .during(1000, MILLISECONDS)
                    .until(() -> (System.currentTimeMillis() - startTime) > 1000L));
    }

    @Test
    @Timeout(value = 2000L, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void awaitDuringTimeOnConditionMessingAtLeastAtMost() throws Exception {
        Duration duration = measureDuration(() ->
            await()
                .during(1, SECONDS)
                .during(1, SECONDS)
                .atLeast(1, SECONDS)
                .atMost(2, SECONDS)
                .until(() -> true)
        );

        assertThat(duration.toMillis(), greaterThan(1000L));
    }

    @Test
    void throwsIAEWhenTimeoutIsTooLargeForTheUnit() {
        int timeoutMinutes = Integer.MAX_VALUE;
        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
            Awaitility.await().atMost(timeoutMinutes, TimeUnit.MINUTES).until(() -> true));
        assertThat(exception.getMessage(), containsString("Cannot convert " + timeoutMinutes + " MINUTES to nanoseconds, as required by Awaitility, because this value is too large"));
    }

    private Callable<Boolean> fakeRepositoryValueEqualsOne() {
        return new FakeRepositoryEqualsOne(fakeRepository);
    }

    private Callable<Boolean> fakeRepositoryValueEqualsOneAsAnonymous() {
        //noinspection Convert2Lambda
        return new Callable<Boolean>() {

            public Boolean call() {
                return fakeRepository.getValue() == 1;
            }
        };
    }

    private Callable<Integer> value() {
        return new FakeRepositoryValue(fakeRepository);
    }

    private Callable<Integer> valueAsAnonymous() {
        //noinspection Convert2Lambda
        return new Callable<Integer>() {
            public Integer call() {
                return fakeRepository.getValue();
            }
        };
    }

    private Callable<List<Integer>> valueAsList() {
        return () -> Collections.singletonList(fakeRepository.getValue());
    }

    @FunctionalInterface
    interface ActionWithException {
        void action() throws Exception;
    }
    private Duration measureDuration(ActionWithException a) throws Exception {
        Instant init = Instant.now();

        a.action();

        return Duration.between(init, Instant.now());
    }
}