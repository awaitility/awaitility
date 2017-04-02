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

import org.awaitility.classes.*;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.Before;
import org.junit.ComparisonFailure;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.*;
import static org.awaitility.Awaitility.*;
import static org.awaitility.Duration.ONE_SECOND;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AwaitilityTest {

    private FakeRepository fakeRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        fakeRepository = new FakeRepositoryImpl();
        Awaitility.reset();
    }

    @Test(timeout = 2000)
    public void awaitOperationBlocksAutomatically() throws Exception {
        new Asynch(fakeRepository).perform();
        await().until(fakeRepositoryValueEqualsOne());
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void awaitOperationSupportsSpecifyingPollIntervalUsingTimeunit() throws Exception {
        new Asynch(fakeRepository).perform();
        with().pollInterval(20, TimeUnit.MILLISECONDS).await().until(fakeRepositoryValueEqualsOne());
        given().pollInterval(20, TimeUnit.MILLISECONDS).await().until(fakeRepositoryValueEqualsOne());
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void awaitOperationSupportsSpecifyingPollInterval() throws Exception {
        new Asynch(fakeRepository).perform();
        with().pollInterval(Duration.ONE_HUNDRED_MILLISECONDS).then().await().until(fakeRepositoryValueEqualsOne());
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void awaitOperationSupportsSpecifyingZeroAsPollDelay() throws Exception {
        new Asynch(fakeRepository).perform();
        with().pollDelay(Duration.ZERO).pollInterval(Duration.ONE_HUNDRED_MILLISECONDS).then().await().until(fakeRepositoryValueEqualsOne());
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void awaitOperationSupportsSpecifyingZeroAsPollInterval() throws Exception {
        new Asynch(fakeRepository).perform();
        with().pollDelay(Duration.TWO_HUNDRED_MILLISECONDS).pollInterval(Duration.ZERO).then().await().until(fakeRepositoryValueEqualsOne());
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void awaitOperationDoesntSupportSpecifyingForeverAsPollDelay() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Cannot delay polling forever");

        new Asynch(fakeRepository).perform();
        with().pollDelay(Duration.FOREVER).pollInterval(Duration.ONE_HUNDRED_MILLISECONDS).then().await().until(fakeRepositoryValueEqualsOne());
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void awaitOperationDoesntSupportSpecifyingForeverAsPollInterval() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Cannot use a fixed poll interval of length 'forever'");

        new Asynch(fakeRepository).perform();
        with().pollDelay(Duration.ONE_HUNDRED_MILLISECONDS).pollInterval(Duration.FOREVER).then().await().until(fakeRepositoryValueEqualsOne());
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void awaitOperationSupportsSpecifyingPollDelay() throws Exception {
        new Asynch(fakeRepository).perform();
        with().pollDelay(Duration.ONE_HUNDRED_MILLISECONDS).await().until(fakeRepositoryValueEqualsOne());
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000L, expected = ConditionTimeoutException.class)
    public void awaitOperationSupportsDefaultTimeout() throws Exception {
        Awaitility.setDefaultTimeout(120, TimeUnit.MILLISECONDS);
        await().until(value(), greaterThan(0));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void foreverConditionSpecificationUsingUntilWithDirectBlock() throws Exception {
        new Asynch(fakeRepository).perform();
        await().forever().until(fakeRepositoryValueEqualsOne());
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void foreverConditionWithHamcrestMatchersWithDirectBlock() throws Exception {
        new Asynch(fakeRepository).perform();
        await().forever().until(value(), equalTo(1));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void foreverConditionWithHamcrestCollectionMatchersWithDirectBlock() throws Exception {
        new Asynch(fakeRepository).perform();
        await().forever().until(valueAsList(), hasItem(1));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 3000, expected = ConditionTimeoutException.class)
    public void throwsTimeoutExceptionWhenDoneEarlierThanAtLeastConstraint() throws Exception {
        new Asynch(fakeRepository).perform();
        await().atLeast(1, SECONDS).and().atMost(2, SECONDS).until(value(), equalTo(1));
    }

    @Test(timeout = 3000)
    public void doesNotThrowTimeoutExceptionWhenDoneLaterThanAtLeastConstraint() throws Exception {
        new Asynch(fakeRepository).perform();
        await().atLeast(100, NANOSECONDS).until(value(), equalTo(1));
    }

    @Test(timeout = 2000)
    public void specifyingDefaultPollIntervalImpactsAllSubsequentUndefinedPollIntervalStatements() throws Exception {
        Awaitility.setDefaultPollInterval(20, TimeUnit.MILLISECONDS);
        new Asynch(fakeRepository).perform();
        await().until(value(), equalTo(1));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000, expected = ConditionTimeoutException.class)
    public void conditionBreaksAfterDurationTimeout() throws Exception {
        new Asynch(fakeRepository).perform();
        await().atMost(200, TimeUnit.MILLISECONDS).until(value(), equalTo(1));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000, expected = IllegalStateException.class)
    public void uncaughtExceptionsArePropagatedToAwaitingThreadAndBreaksForeverBlockWhenSetToCatchAllUncaughtExceptions()
            throws Exception {
        catchUncaughtExceptionsByDefault();
        new ExceptionThrowingAsynch(new IllegalStateException("Illegal state!")).perform();
        await().forever().until(value(), equalTo(1));
    }

    @Test(timeout = 2000, expected = ComparisonFailure.class)
    public void uncaughtThrowablesArePropagatedToAwaitingThreadAndBreaksForeverBlockWhenSetToCatchAllUncaughtExceptions() {
        new ExceptionThrowingAsynch(new ComparisonFailure("Message", "Something", "Something else")).perform();
        await().forever().until(value(), equalTo(1));
    }

    @Test(timeout = 2000)
    public void uncaughtThrowablesFromOtherThreadsCanBeIgnored() throws Exception {
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
                public Boolean call() throws Exception {
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

    @Test(timeout = 2000, expected = IllegalStateException.class)
    public void uncaughtExceptionsArePropagatedToAwaitingThreadAndBreaksForeverBlockWhenCatchingAllUncaughtExceptions()
            throws Exception {
        new ExceptionThrowingAsynch(new IllegalStateException("Illegal state!")).perform();
        catchUncaughtExceptions().and().await().forever().until(value(), equalTo(1));
    }

    @Test(timeout = 2000, expected = ConditionTimeoutException.class)
    public void whenDontCatchUncaughtExceptionsIsSpecifiedThenExceptionsFromOtherThreadsAreNotCaught() throws Exception {
        new AssertExceptionThrownInAnotherThreadButNeverCaughtByAnyThreadTest() {
            @Override
            public void testLogic() {
                new ExceptionThrowingAsynch(new IllegalStateException("Illegal state!")).perform();
                dontCatchUncaughtExceptions().and().await().atMost(ONE_SECOND).until(value(), equalTo(1));
            }
        };
    }

    @Test(timeout = 2000, expected = ConditionTimeoutException.class)
    public void whenDontCatchUncaughtExceptionsIsSpecifiedAndTheBuildOfTheAwaitStatementHasStartedThenExceptionsFromOtherThreadsAreNotCaught()
            throws Exception {
        new AssertExceptionThrownInAnotherThreadButNeverCaughtByAnyThreadTest() {
            @Override
            public void testLogic() {
                new ExceptionThrowingAsynch(new IllegalStateException("Illegal state!")).perform();
                await().and().dontCatchUncaughtExceptions().given().timeout(ONE_SECOND).until(value(), equalTo(1));
            }
        };
    }

    @Test(timeout = 2000, expected = ConditionTimeoutException.class)
    public void catchUncaughtExceptionsIsReset() throws Exception {
        new AssertExceptionThrownInAnotherThreadButNeverCaughtByAnyThreadTest() {
            @Override
            public void testLogic() {
                new ExceptionThrowingAsynch(new IllegalStateException("Illegal state!")).perform();
                dontCatchUncaughtExceptions().and().await().atMost(Duration.ONE_SECOND).until(value(), equalTo(1));
            }
        };
    }

    @Test(timeout = 2000, expected = IllegalStateException.class)
    public void exceptionsInConditionsArePropagatedToAwaitingThreadAndBreaksForeverBlock() throws Exception {
        final ExceptionThrowingFakeRepository repository = new ExceptionThrowingFakeRepository();
        new Asynch(repository).perform();
        await().until(new FakeRepositoryValue(repository), equalTo(1));
    }

    @Test(timeout = 2000)
    public void awaitWithAliasDisplaysAliasWhenConditionTimeoutExceptionOccurs() throws Exception {
        String alias = "test";
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage(
                "Condition with alias 'test' didn't complete within 120 milliseconds because org.awaitility.classes.FakeRepositoryValue expected a value greater than <0> but <0> was equal to <0>.");

        await(alias).atMost(120, MILLISECONDS).until(value(), greaterThan(0));
    }

    @Test(timeout = 2000)
    public void awaitWithAliasDisplaysAliasWhenConditionTimeoutExceptionAndConditionIsACallableOccurs() throws Exception {
        String alias = "test";
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage("Condition with alias 'test' didn't complete within 120 milliseconds because condition returned by method \"awaitWithAliasDisplaysAliasWhenConditionTimeoutExceptionAndConditionIsACallableOccurs\" in class org.awaitility.AwaitilityTest was not fulfilled.");

        await(alias).atMost(120, MILLISECONDS).until(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return fakeRepository.getValue() > 0;
            }
        });
    }

    @Test(timeout = 2000)
    public void awaitDisplaysSupplierAndMatcherMismatchMessageWhenConditionTimeoutExceptionOccurs() throws Exception {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage(FakeRepositoryValue.class.getName()
                + " expected a value greater than <0> but <0> was equal to <0> within 120 milliseconds.");

        with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).until(value(), greaterThan(0));
    }

    @Test(timeout = 2000)
    public void awaitDisplaysCallableNameWhenConditionTimeoutExceptionOccurs() throws Exception {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage(String.format("Condition %s was not fulfilled within 120 milliseconds.",
                FakeRepositoryEqualsOne.class.getName()));

        await().atMost(120, MILLISECONDS).until(fakeRepositoryValueEqualsOne());
    }

    @Test(timeout = 2000)
    public void awaitDisplaysMethodDeclaringTheCallableWhenCallableIsAnonymousClassAndConditionTimeoutExceptionOccurs()
            throws Exception {
        exception.expect(ConditionTimeoutException.class);
        exception
                .expectMessage(String
                        .format("Condition returned by method \"fakeRepositoryValueEqualsOneAsAnonymous\" in class %s was not fulfilled within 120 milliseconds.",
                                AwaitilityTest.class.getName()));

        await().atMost(120, MILLISECONDS).until(fakeRepositoryValueEqualsOneAsAnonymous());
    }

    @Test(timeout = 2000)
    public void awaitDisplaysMethodDeclaringTheSupplierWhenSupplierIsAnonymousClassAndConditionTimeoutExceptionOccurs()
            throws Exception {
        exception.expect(ConditionTimeoutException.class);
        exception
                .expectMessage(String
                        .format("%s.valueAsAnonymous Callable expected %s but was <0> within 120 milliseconds.",
                                AwaitilityTest.class.getName(), equalTo(2).toString()));

        with().pollInterval(10, MILLISECONDS).await().atMost(120, MILLISECONDS).until(valueAsAnonymous(), equalTo(2));
    }

    @Test
    public void awaitilityThrowsIllegalStateExceptionWhenTimeoutIsLessThanPollDelay() throws Exception {
        exception.expect(IllegalStateException.class);
        exception.expectMessage(is("Timeout (10 seconds) must be greater than the poll delay (10 minutes)."));

        with().pollDelay(10, MINUTES).await().atMost(10, SECONDS).until(fakeRepositoryValueEqualsOne());
    }

    @Test
    public void awaitilityThrowsIllegalStateExceptionWhenTimeoutIsEqualToPollDelay() throws Exception {
        exception.expect(IllegalStateException.class);
        exception.expectMessage(is("Timeout (200 milliseconds) must be greater than the poll delay (200 milliseconds)."));

        with().with().pollDelay(20, MILLISECONDS).pollDelay(200, MILLISECONDS).await().atMost(200, MILLISECONDS).until(fakeRepositoryValueEqualsOne());
    }

    @Test(timeout = 2000L, expected = IllegalStateException.class)
    public void rethrowsExceptionsInCallable() {
        await().atMost(1, TimeUnit.SECONDS)
                .until(new Callable<Boolean>() {
                           public Boolean call() throws Exception {
                               throw new IllegalStateException("Hello");
                           }
                       }
                );
    }

    private Callable<Boolean> fakeRepositoryValueEqualsOne() {
        return new FakeRepositoryEqualsOne(fakeRepository);
    }

    private Callable<Boolean> fakeRepositoryValueEqualsOneAsAnonymous() {
        return new Callable<Boolean>() {

            public Boolean call() throws Exception {
                return fakeRepository.getValue() == 1;
            }
        };
    }

    private Callable<Integer> value() {
        return new FakeRepositoryValue(fakeRepository);
    }

    private Callable<Integer> valueAsAnonymous() {
        return new Callable<Integer>() {
            public Integer call() throws Exception {
                return fakeRepository.getValue();
            }
        };
    }

    private Callable<List<Integer>> valueAsList() {
        return new Callable<List<Integer>>() {
            public List<Integer> call() throws Exception {
                return Collections.singletonList(fakeRepository.getValue());
            }
        };
    }
}