/*
 * Copyright 2010 the original author or authors.
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
package com.jayway.awaitility;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.awaitility.Awaitility.to;
import static com.jayway.awaitility.Awaitility.dontCatchUncaughtExceptions;
import static com.jayway.awaitility.Awaitility.catchUncaughtExceptions;
import static com.jayway.awaitility.Awaitility.catchUncaughtExceptionsByDefault;
import static com.jayway.awaitility.Awaitility.dontCatchUncaughtExceptions;
import static com.jayway.awaitility.Awaitility.given;
import static com.jayway.awaitility.Awaitility.waitAtMost;
import static com.jayway.awaitility.Awaitility.with;
import static com.jayway.awaitility.Duration.ONE_SECOND;
import static com.jayway.awaitility.Duration.SAME_AS_POLL_INTERVAL;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.jayway.awaitility.classes.Asynch;
import com.jayway.awaitility.classes.ExceptionThrowingAsynch;
import com.jayway.awaitility.classes.ExceptionThrowingFakeRepository;
import com.jayway.awaitility.classes.FakeObjectRepository;
import com.jayway.awaitility.classes.FakeRepository;
import com.jayway.awaitility.classes.FakeRepositoryEqualsOne;
import com.jayway.awaitility.classes.FakeRepositoryImpl;
import com.jayway.awaitility.classes.FakeRepositoryValue;
import com.jayway.awaitility.classes.FinalClass;
import com.jayway.awaitility.classes.FinalFakeRepositoryImpl;
import com.jayway.awaitility.proxy.CannotCreateProxyException;

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
    public void awaitUsingCallTo() throws Exception {
        new Asynch(fakeRepository).perform();
        await().untilCall(to(fakeRepository).getValue(), greaterThan(0));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void givenInstancePassedToCallToIsAFinalClassThenInterfaceProxyingIsUsed() throws Exception {
        fakeRepository = new FinalFakeRepositoryImpl();
        new Asynch(fakeRepository).perform();
        await().untilCall(to(fakeRepository).getValue(), greaterThan(0));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(expected = CannotCreateProxyException.class)
    public void givenInstancePassedToCallToIsAFinalClassWithNoInterfacesThenExceptionIsThrown() throws Exception {
        to(new FinalClass());
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
    public void awaitOperationSupportsSpecifyingPollDelay() throws Exception {
        new Asynch(fakeRepository).perform();
        with().pollDelay(Duration.ONE_HUNDRED_MILLISECONDS).await().until(fakeRepositoryValueEqualsOne());
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(expected = TimeoutException.class)
    public void awaitOperationSupportsDefaultTimeout() throws Exception {
        Awaitility.setDefaultTimeout(20, TimeUnit.MILLISECONDS);
        await().until(value(), greaterThan(0));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(expected = TimeoutException.class)
    public void awaitOperationSupportsDefaultPollDelay() throws Exception {
        Awaitility.setDefaultPollDelay(3000, TimeUnit.MILLISECONDS);
        await().atMost(ONE_SECOND).until(value(), greaterThan(0));
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
    public void specifyingDefaultPollIntervalImpactsAllSubsequentUndefinedPollIntervalStatements() throws Exception {
        Awaitility.setDefaultPollInterval(20, TimeUnit.MILLISECONDS);
        new Asynch(fakeRepository).perform();
        await().until(value(), equalTo(1));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000, expected = TimeoutException.class)
    public void conditionBreaksAfterDurationTimeout() throws Exception {
        new Asynch(fakeRepository).perform();
        await().atMost(200, TimeUnit.MILLISECONDS).until(value(), equalTo(1));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000, expected = IllegalStateException.class)
    public void uncaughtExceptionsArePropagatedToAwaitingThreadAndBreaksForeverBlockWhenSetToCatchAllUncaughtExceptions()
            throws Exception {
        catchUncaughtExceptionsByDefault();
        new ExceptionThrowingAsynch().perform();
        await().forever().until(value(), equalTo(1));
    }

    @Test(timeout = 2000, expected = IllegalStateException.class)
    public void uncaughtExceptionsArePropagatedToAwaitingThreadAndBreaksForeverBlockWhenCatchingAllUncaughtExceptions()
            throws Exception {
        new ExceptionThrowingAsynch().perform();
        catchUncaughtExceptions().and().await().forever().until(value(), equalTo(1));
    }

    @Test(timeout = 2000, expected = TimeoutException.class)
    public void whenDontCatchUncaughtExceptionsIsSpecifiedThenExceptionsFromOtherThreadsAreNotCaught() throws Exception {
        new AssertExceptionThrownInAnotherThreadButNeverCaughtByAnyThreadTest() {
            @Override
            public void testLogic() throws Exception {
                new ExceptionThrowingAsynch().perform();
                dontCatchUncaughtExceptions().and().await().atMost(ONE_SECOND).until(value(), equalTo(1));
            }
        };
    }

    @Test(timeout = 2000, expected = TimeoutException.class)
    public void whenDontCatchUncaughtExceptionsIsSpecifiedAndTheBuildOfTheAwaitStatementHasStartedThenExceptionsFromOtherThreadsAreNotCaught()
            throws Exception {
        new AssertExceptionThrownInAnotherThreadButNeverCaughtByAnyThreadTest() {
            @Override
            public void testLogic() throws Exception {
                new ExceptionThrowingAsynch().perform();
                await().and().dontCatchUncaughtExceptions().given().timeout(ONE_SECOND).until(value(), equalTo(1));
            }
        };
    }

    @Test(timeout = 2000, expected = TimeoutException.class)
    public void catchUncaughtExceptionsIsReset() throws Exception {
        new AssertExceptionThrownInAnotherThreadButNeverCaughtByAnyThreadTest() {
            @Override
            public void testLogic() throws Exception {
                new ExceptionThrowingAsynch().perform();
                dontCatchUncaughtExceptions().and().await().atMost(Duration.ONE_SECOND).until(value(), equalTo(1));
            }
        };
    }

    @Test(timeout = 2000, expected = TimeoutException.class)
    public void waitAtMostWorks() throws Exception {
        new AssertExceptionThrownInAnotherThreadButNeverCaughtByAnyThreadTest() {

            @Override
            public void testLogic() throws Exception {
                new ExceptionThrowingAsynch().perform();
                dontCatchUncaughtExceptions().and().given().pollInterval(Duration.ONE_HUNDRED_MILLISECONDS).then().await().atMost(ONE_SECOND)
                        .untilCall(to(fakeRepository).getValue(), equalTo(1));
                waitAtMost(ONE_SECOND).and().dontCatchUncaughtExceptions().untilCall(to(fakeRepository).getValue(), equalTo(1));
                dontCatchUncaughtExceptions().and().await().atMost(ONE_SECOND).untilCall(to(fakeRepository).getValue(), equalTo(1));
                dontCatchUncaughtExceptions().and().await().untilCall(to(fakeRepository).getValue(), equalTo(1));
            }
        };
    }

    @Test(timeout = 2000, expected = IllegalStateException.class)
    public void exceptionsInConditionsArePropagatedToAwaitingThreadAndBreaksForeverBlock() throws Exception {
        final ExceptionThrowingFakeRepository repository = new ExceptionThrowingFakeRepository();
        new Asynch(repository).perform();
        await().until(new FakeRepositoryValue(repository), equalTo(1));
    }

    @Test(timeout = 4000)
    public void awaitWithTimeout() throws Exception {
        new Asynch(fakeRepository).perform();
        with().timeout(1, SECONDS).await().untilCall(to(fakeRepository).getValue(), greaterThan(0));
    }

    @Test(timeout = 2000)
    public void awaitWithAliasDisplaysAliasWhenTimeoutExceptionOccurs() throws Exception {
        String alias = "test";
        exception.expect(TimeoutException.class);
        exception.expectMessage(alias);

        await(alias).atMost(20, MILLISECONDS).until(value(), greaterThan(0));
    }

    @Test(timeout = 2000, expected = IllegalStateException.class)
    public void awaitWithSameAsPollIntervalThrowsIllegalStateException() throws Exception {
        await().atMost(SAME_AS_POLL_INTERVAL).until(value(), greaterThan(0));
    }

    @Test(timeout = 2000)
    public void awaitDisplaysSupplierAndMatcherNameWhenTimeoutExceptionOccurs() throws Exception {
        exception.expect(TimeoutException.class);
        exception.expectMessage(FakeRepositoryValue.class.getName()
                + " expected a value greater than <0> but was <0> within 20 milliseconds.");

        with().pollInterval(10, MILLISECONDS).then().await().atMost(20, MILLISECONDS).until(value(), greaterThan(0));
    }

    @Test(timeout = 2000)
    public void awaitDisplaysCallableNameWhenTimeoutExceptionOccurs() throws Exception {
        exception.expect(TimeoutException.class);
        exception.expectMessage(String.format("Condition %s was not fulfilled within 20 milliseconds.",
                FakeRepositoryEqualsOne.class.getName()));

        await().atMost(20, MILLISECONDS).until(fakeRepositoryValueEqualsOne());
    }

    @Test(timeout = 2000)
    public void awaitDisplaysMethodDeclaringTheCallableWhenCallableIsAnonymousClassAndTimeoutExceptionOccurs()
            throws Exception {
        exception.expect(TimeoutException.class);
        exception
                .expectMessage(String
                        .format("Condition returned by method \"fakeRepositoryValueEqualsOneAsAnonymous\" in class %s was not fulfilled within 20 milliseconds.",
                        AwaitilityTest.class.getName()));

        await().atMost(20, MILLISECONDS).until(fakeRepositoryValueEqualsOneAsAnonymous());
    }

    @Test(timeout = 2000)
    public void awaitDisplaysMethodInvocationNameAndMatcherNameWhenUsingCallToAndTimeoutExceptionOccurs()
            throws Exception {
        exception.expect(TimeoutException.class);
        exception.expectMessage(FakeRepositoryImpl.class.getName()
                + ".getValue() expected a value greater than <0> but was <0> within 50 milliseconds.");

        new Asynch(fakeRepository).perform();
        with().pollInterval(10, MILLISECONDS).and().timeout(50, MILLISECONDS).await()
                .untilCall(to(fakeRepository).getValue(), greaterThan(0));
    }

    @Test(timeout = 2000)
    public void awaitDisplaysMethodDeclaringTheSupplierWhenSupplierIsAnonymousClassAndTimeoutExceptionOccurs()
            throws Exception {
        exception.expect(TimeoutException.class);
        exception
                .expectMessage(String
                        .format("%s.valueAsAnonymous Callable expected %s but was <0> within 20 milliseconds.",
                        AwaitilityTest.class.getName(), equalTo(2).toString()));

        with().pollInterval(10, MILLISECONDS).await().atMost(20, MILLISECONDS).until(valueAsAnonymous(), equalTo(2));
    }

    @Test(timeout = 2000)
    public void awaitDisplaysLastPollResultOnTimeout() throws Exception {
        FakeObjectRepository fakeObjectRepository = new FakeObjectRepository();
        Object actualObject = fakeObjectRepository.getObject();
        Object expectedObject = new Object();

        exception.expect(TimeoutException.class);
        exception.expectMessage(String.format("%s.getObject() expected <%s> but was <%s> within 50 milliseconds.",
                FakeObjectRepository.class.getName(), expectedObject.toString(), actualObject.toString()));

        with().pollInterval(10, MILLISECONDS).and().timeout(50, MILLISECONDS).await()
                .untilCall(to(fakeObjectRepository).getObject(), is(expectedObject));
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

    private abstract class AssertExceptionThrownInAnotherThreadButNeverCaughtByAnyThreadTest {
        public AssertExceptionThrownInAnotherThreadButNeverCaughtByAnyThreadTest() throws Exception {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            System.setErr(new PrintStream(byteArrayOutputStream, true));
            try {
                testLogic();
            } finally {
                String errorMessage = byteArrayOutputStream.toString();
                try {
                    assertTrue(errorMessage.contains("Illegal state!"));
                } finally {
                    System.setErr(System.err);
                }
            }
        }

        public abstract void testLogic() throws Exception;
    }
}
