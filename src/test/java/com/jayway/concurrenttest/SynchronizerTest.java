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
package com.jayway.concurrenttest;

import static com.jayway.concurrenttest.Synchronizer.await;
import static com.jayway.concurrenttest.Synchronizer.condition;
import static com.jayway.concurrenttest.synchronizer.ConditionOptions.atMost;
import static com.jayway.concurrenttest.synchronizer.ConditionOptions.callTo;
import static com.jayway.concurrenttest.synchronizer.ConditionOptions.duration;
import static com.jayway.concurrenttest.synchronizer.ConditionOptions.until;
import static com.jayway.concurrenttest.synchronizer.ConditionOptions.withPollInterval;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

import com.jayway.concurrenttest.classes.Asynch;
import com.jayway.concurrenttest.classes.ExceptionThrowingAsynch;
import com.jayway.concurrenttest.classes.ExceptionThrowingFakeRepository;
import com.jayway.concurrenttest.classes.FakeRepository;
import com.jayway.concurrenttest.classes.FakeRepositoryEqualsOne;
import com.jayway.concurrenttest.classes.FakeRepositoryImpl;
import com.jayway.concurrenttest.classes.FakeRepositoryValue;
import com.jayway.concurrenttest.synchronizer.AwaitSupportedCondition;
import com.jayway.concurrenttest.synchronizer.ConditionEvaluator;
import com.jayway.concurrenttest.synchronizer.Duration;
import com.jayway.concurrenttest.synchronizer.Supplier;

public class SynchronizerTest {

    private FakeRepository fakeRepository;

    @Before
    public void setup() {
        fakeRepository = new FakeRepositoryImpl();
        Synchronizer.reset();
    }

    @Test(timeout = 2000)
    public void foreverConditionSpecificationWithDirectBlock() throws Exception {
        new Asynch(fakeRepository).perform();
        condition(fakeRepositoryValueEqualsOne()).await();
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void awaitOperationBlocksAutomatically() throws Exception {
        new Asynch(fakeRepository).perform();
        await(until(fakeRepositoryValueEqualsOne()));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void awaitOperationSupportsSpecifyingPollSpecification() throws Exception {
        new Asynch(fakeRepository).perform();
        await(until(fakeRepositoryValueEqualsOne()), withPollInterval(20, TimeUnit.MILLISECONDS));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void awaitOperationSupportsSpecifyingPollIntervalDirectly() throws Exception {
        new Asynch(fakeRepository).perform();
        await(until(fakeRepositoryValueEqualsOne()), Duration.TWO_HUNDRED_MILLISECONDS);
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void conditionSupportsSpecifyingPollIntervalDirectly() throws Exception {
        new Asynch(fakeRepository).perform();
        condition(until(fakeRepositoryValueEqualsOne()), Duration.TWO_HUNDRED_MILLISECONDS).await();
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void conditionSupportsSpecifyingPollIntervalAndDurationDirectly() throws Exception {
        new Asynch(fakeRepository).perform();
        condition(Duration.ONE_SECOND, until(fakeRepositoryValueEqualsOne()), Duration.TWO_HUNDRED_MILLISECONDS)
                .await();
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(expected = TimeoutException.class)
    public void awaitOperationSupportsSpecifyingDurationDirectly() throws Exception {
        await(Duration.ONE_HUNDRED_MILLISECONDS, until(fakeRepositoryValueEqualsOne()));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(expected = TimeoutException.class)
    public void defineConditionSupportsSpecifyingDurationDirectly() throws Exception {
        condition(Duration.ONE_HUNDRED_MILLISECONDS, until(fakeRepositoryValueEqualsOne()),
                Duration.ONE_HUNDRED_MILLISECONDS).await();
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void awaitOperationSupportsSpecifyingPollInterval() throws Exception {
        new Asynch(fakeRepository).perform();
        await(until(fakeRepositoryValueEqualsOne()), withPollInterval(Duration.ONE_HUNDRED_MILLISECONDS));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void defineConditionSupportsSpecifyingPollSpecification() throws Exception {
        new Asynch(fakeRepository).perform();
        condition(until(value(), greaterThan(0)), withPollInterval(20, TimeUnit.MILLISECONDS)).await();
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void defineConditionSupportsSpecifyingPollInterval() throws Exception {
        new Asynch(fakeRepository).perform();
        condition(until(value(), greaterThan(0)), withPollInterval(Duration.ONE_HUNDRED_MILLISECONDS)).await();
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(expected = TimeoutException.class)
    public void defineConditionSupportsDefaultTimeout() throws Exception {
        Synchronizer.setDefaultTimeout(duration(20, TimeUnit.MILLISECONDS));
        condition(until(value(), greaterThan(0))).await();
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(expected = TimeoutException.class)
    public void awaitOperationSupportsDefaultTimeout() throws Exception {
        Synchronizer.setDefaultTimeout(duration(20, TimeUnit.MILLISECONDS));
        await(until(value(), greaterThan(0)));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout=2000)
    public void awaitUsingProxy() throws Exception {
        new Asynch(fakeRepository).perform();
        await(until(callTo(fakeRepository).getValue(), greaterThan(0)));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void foreverConditionSpecificationUsingUntilWithDirectBlock() throws Exception {
        new Asynch(fakeRepository).perform();
        condition(until(fakeRepositoryValueEqualsOne())).await();
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void awaitCanWaitOnCondition() throws Exception {
        new Asynch(fakeRepository).perform();
        await(condition(until(fakeRepositoryValueEqualsOne())));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void foreverConditionWithHamcrestMatchersWithDirectBlock() throws Exception {
        new Asynch(fakeRepository).perform();
        condition(until(value(), equalTo(1))).await();
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void specifyingDefaultPollIntervalImpactsAllSubsequentUndefinedPollIntervalStatements() throws Exception {
        Synchronizer.setDefaultPollInterval(20, TimeUnit.MILLISECONDS);
        new Asynch(fakeRepository).perform();
        condition(until(value(), equalTo(1))).await();
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000, expected = TimeoutException.class)
    public void conditionBreaksAfterDurationTimeout() throws Exception {
        new Asynch(fakeRepository).perform();
        condition(200, TimeUnit.MILLISECONDS, until(value(), equalTo(1))).await();
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000, expected = TimeoutException.class)
    public void conditionBreaksAfterDurationTimeoutWhenUsingAtMost() throws Exception {
        new Asynch(fakeRepository).perform();
        condition(atMost(200, TimeUnit.MILLISECONDS), until(value(), equalTo(1))).await();
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000, expected = IllegalStateException.class)
    public void uncaughtExceptionsArePropagatedToAwaitingThreadAndBreaksForeverBlockWhenSetToCatchAllUncaughtExceptions()
            throws Exception {
        AwaitSupportedCondition operation = condition(until(value(), equalTo(1))).andCatchAllUncaughtExceptions();
        new ExceptionThrowingAsynch().perform();
        operation.await();
    }

    @Test(timeout = 2000, expected = IllegalStateException.class)
    public void uncaughtExceptionsArePropagatedToAwaitingThreadAndBreaksForeverBlockAlsoForBlockWhenSetToCatchAllUncaughtExceptions()
            throws Exception {
        Synchronizer.catchUncaughtExceptions();
        new ExceptionThrowingAsynch().perform();
        await(until(value(), equalTo(1)));
    }

    @Test(timeout = 2000, expected = TimeoutException.class)
    public void catchUncaughtExceptionsIsReset() throws Exception {
        new ExceptionThrowingAsynch().perform();
        await(Duration.ONE_SECOND, until(value(), equalTo(1)));
    }

    @Test(timeout = 2000, expected = IllegalStateException.class)
    public void exceptionsInConditionsArePropagatedToAwaitingThreadAndBreaksForeverBlock() throws Exception {
        final ExceptionThrowingFakeRepository repository = new ExceptionThrowingFakeRepository();
        new Asynch(repository).perform();
        condition(until(new FakeRepositoryValue(repository), equalTo(1))).await();
    }

    private ConditionEvaluator fakeRepositoryValueEqualsOne() {
        return new FakeRepositoryEqualsOne(fakeRepository);
    }

    private Supplier<Integer> value() {
        return new FakeRepositoryValue(fakeRepository);
    }
}
