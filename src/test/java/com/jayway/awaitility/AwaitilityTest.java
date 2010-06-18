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
import static com.jayway.awaitility.Awaitility.catchUncaughtExceptions;
import static com.jayway.awaitility.Awaitility.catchingUncaughtExceptions;
import static com.jayway.awaitility.Awaitility.waitAtMost;
import static com.jayway.awaitility.Awaitility.withPollInterval;
import static com.jayway.awaitility.Awaitility.withTimeout;
import static com.jayway.awaitility.synchronizer.ConditionFactory.callTo;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.classes.Asynch;
import com.jayway.awaitility.classes.ExceptionThrowingAsynch;
import com.jayway.awaitility.classes.ExceptionThrowingFakeRepository;
import com.jayway.awaitility.classes.FakeRepository;
import com.jayway.awaitility.classes.FakeRepositoryEqualsOne;
import com.jayway.awaitility.classes.FakeRepositoryImpl;
import com.jayway.awaitility.classes.FakeRepositoryValue;
import com.jayway.awaitility.classes.FinalClass;
import com.jayway.awaitility.classes.FinalFakeRepositoryImpl;
import com.jayway.awaitility.synchronizer.CannotCreateProxyException;
import com.jayway.awaitility.synchronizer.ConditionEvaluator;
import com.jayway.awaitility.synchronizer.Duration;

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
		await().until(callTo(fakeRepository).getValue(), greaterThan(0));
		assertEquals(1, fakeRepository.getValue());
	}

	@Test(timeout = 2000)
	public void givenInstancePassedToCallToIsAFinalClassThenInterfaceProxyingIsUsed() throws Exception {
		fakeRepository = new FinalFakeRepositoryImpl();
		new Asynch(fakeRepository).perform();
		await().until(callTo(fakeRepository).getValue(), greaterThan(0));
		assertEquals(1, fakeRepository.getValue());
	}

	@Test(expected = CannotCreateProxyException.class)
	public void givenInstancePassedToCallToIsAFinalClassWithNoInterfacesThenExceptionIsThrown() throws Exception {
		callTo(new FinalClass());
	}

	@Test(timeout = 2000)
	public void awaitOperationBlocksAutomatically() throws Exception {
		new Asynch(fakeRepository).perform();
		await().until(fakeRepositoryValueEqualsOne());
		assertEquals(1, fakeRepository.getValue());
	}

	@Test(timeout = 2000)
	public void awaitOperationSupportsSpecifyingPollSpecification() throws Exception {
		new Asynch(fakeRepository).perform();
		withPollInterval(20, TimeUnit.MILLISECONDS).await().until(fakeRepositoryValueEqualsOne());
		withPollInterval(20, TimeUnit.MILLISECONDS).await().until(fakeRepositoryValueEqualsOne());
		assertEquals(1, fakeRepository.getValue());
	}

	@Test(timeout = 2000)
	public void awaitOperationSupportsSpecifyingPollInterval() throws Exception {
		new Asynch(fakeRepository).perform();
		withPollInterval(Duration.ONE_HUNDRED_MILLISECONDS).await().until(fakeRepositoryValueEqualsOne());
		assertEquals(1, fakeRepository.getValue());
	}

	@Test(expected = TimeoutException.class)
	public void awaitOperationSupportsDefaultTimeout() throws Exception {
		Awaitility.setDefaultTimeout(20, TimeUnit.MILLISECONDS);
		await().until(value(), greaterThan(0));
		assertEquals(1, fakeRepository.getValue());
	}

	@Test(timeout = 2000)
	public void foreverConditionSpecificationUsingUntilWithDirectBlock() throws Exception {
		new Asynch(fakeRepository).perform();
		await().until(fakeRepositoryValueEqualsOne());
		assertEquals(1, fakeRepository.getValue());
	}

	@Test(timeout = 2000)
	public void foreverConditionWithHamcrestMatchersWithDirectBlock() throws Exception {
		new Asynch(fakeRepository).perform();
		await().until(value(), equalTo(1));
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
		catchUncaughtExceptions();
		new ExceptionThrowingAsynch().perform();
		await().until(value(), equalTo(1));
	}

	@Test(timeout = 2000, expected = IllegalStateException.class)
	public void uncaughtExceptionsArePropagatedToAwaitingThreadAndBreaksForeverBlockWhenCatchingAllUncaughtExceptions()
			throws Exception {
		new ExceptionThrowingAsynch().perform();
		catchingUncaughtExceptions().and().await().forever().until(value(), equalTo(1));
	}

	@Test(timeout = 2000, expected = TimeoutException.class)
	public void catchUncaughtExceptionsIsReset() throws Exception {
		new ExceptionThrowingAsynch().perform();
		await().atMost(Duration.ONE_SECOND).until(value(), equalTo(1));
	}

	@Test(timeout = 2000, expected = TimeoutException.class)
	public void waitAtMostWorks() throws Exception {
		new ExceptionThrowingAsynch().perform();
		withPollInterval(Duration.ONE_HUNDRED_MILLISECONDS).atMost(Duration.ONE_SECOND).until(
				callTo(fakeRepository).getValue(), equalTo(1));
		waitAtMost(Duration.ONE_SECOND).until(callTo(fakeRepository).getValue(), equalTo(1));
		await().atMost(Duration.ONE_SECOND).until(callTo(fakeRepository).getValue(), equalTo(1));
		await().until(callTo(fakeRepository).getValue(), equalTo(1));
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
		withTimeout(1, SECONDS).await().until(callTo(fakeRepository).getValue(), greaterThan(0));
	}

	@Test(timeout = 2000)
	public void awaitWithAliasDisplaysAliasWhenTimeoutExceptionOccurs() throws Exception {
		String alias = "test";
		exception.expect(TimeoutException.class);
		exception.expectMessage(alias);

		await(alias).atMost(20, MILLISECONDS).until(value(), greaterThan(0));
	}

	private ConditionEvaluator fakeRepositoryValueEqualsOne() {
		return new FakeRepositoryEqualsOne(fakeRepository);
	}

	private Callable<Integer> value() {
		return new FakeRepositoryValue(fakeRepository);
	}
}
