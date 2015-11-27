/*
 * Copyright 2015 the original author or authors.
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

import com.jayway.awaitility.classes.Asynch;
import com.jayway.awaitility.classes.FakeRepository;
import com.jayway.awaitility.classes.FakeRepositoryImpl;
import com.jayway.awaitility.core.ConditionEvaluationLogger;
import com.jayway.awaitility.core.ConditionTimeoutException;
import com.jayway.awaitility.support.CountDown;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.jayway.awaitility.Awaitility.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

/**
 * Tests for await().until(Runnable) using AssertionCondition.
 *
 * @author Marcin Zajączkowski, 2014-03-28
 * @author Johan Haleby
 */
public class AwaitilityJava8Test {

    private FakeRepository fakeRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        fakeRepository = new FakeRepositoryImpl();
        Awaitility.reset();
    }

    @Test(timeout = 2000)
    public void awaitAssertJAssertionAsLambda() {
        new Asynch(fakeRepository).perform();
        await().until(() -> Assertions.assertThat(fakeRepository.getValue()).isEqualTo(1));
    }

    @Test(timeout = 2000)
    public void awaitUsingLambdaVersionOfCallableBoolean() {
        new Asynch(fakeRepository).perform();
        await().until(() -> fakeRepository.getValue() == 1);
    }

    @SuppressWarnings("Convert2Lambda")
    @Test(timeout = 2000)
    public void awaitAssertJAssertionAsAnonymousClass() {
        new Asynch(fakeRepository).perform();
        await().until(new Runnable() {
            @Override
            public void run() {
                Assertions.assertThat(fakeRepository.getValue()).isEqualTo(1);
            }
        });
    }

    @Test(timeout = 2000)
    public void awaitAssertJAssertionDisplaysOriginalErrorMessageAndTimeoutWhenConditionTimeoutExceptionOccurs() {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage(startsWith("Condition defined as a lambda expression in " + AwaitilityJava8Test.class.getName()));
        exception.expectMessage(endsWith("expected:<[1]> but was:<[0]> within 120 milliseconds."));

        new Asynch(fakeRepository).perform();
        with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).until(
                () -> Assertions.assertThat(fakeRepository.getValue()).isEqualTo(1));
    }

    @Test(timeout = 2000)
    public void awaitJUnitAssertionAsLambda() {
        new Asynch(fakeRepository).perform();
        await().until(() -> assertEquals(1, fakeRepository.getValue()));
    }

    @Test(timeout = 2000)
    public void awaitJUnitAssertionDisplaysOriginalErrorMessageAndTimeoutWhenConditionTimeoutExceptionOccurs() {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage(startsWith("Condition defined as a lambda expression in " + AwaitilityJava8Test.class.getName()));
        exception.expectMessage(endsWith("expected:<1> but was:<0> within 120 milliseconds."));

        with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).until(
                () -> assertEquals(1, fakeRepository.getValue()));
    }

    @Test(timeout = 2000)
    public void lambdaErrorMessageLooksAlrightWhenUsingMethodReferences() {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage("Lambda expression in com.jayway.awaitility.AwaitilityJava8Test that uses com.jayway.awaitility.classes.FakeRepository: expected <1> but was <0> within 120 milliseconds.");
        with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).until(fakeRepository::getValue, equalTo(1));
    }

    @SuppressWarnings("Convert2MethodRef")
    @Test(timeout = 2000)
    public void lambdaErrorMessageLooksAlrightWhenUsingLambda() {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage("Lambda expression in com.jayway.awaitility.AwaitilityJava8Test: expected <1> but was <0> within 120 milliseconds.");

        with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).until(() -> fakeRepository.getValue(), equalTo(1));
    }

    @SuppressWarnings({"Convert2MethodRef", "CodeBlock2Expr"})
    @Test(timeout = 2000)
    public void lambdaErrorMessageLooksAlrightWhenUsingLambdaWithCurlyBraces() {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage("Lambda expression in com.jayway.awaitility.AwaitilityJava8Test: expected <1> but was <0> within 120 milliseconds.");

        with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).until(() -> {
            return fakeRepository.getValue();
        }, equalTo(1));
    }

    @Test(timeout = 2000)
    public void lambdaErrorMessageLooksAlrightWhenAwaitUsingLambdaVersionOfCallableBoolean() {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage("Condition with lambda expression in com.jayway.awaitility.AwaitilityJava8Test was not fulfilled within 200 milliseconds.");

        await().atMost(200, MILLISECONDS).until(() -> fakeRepository.getValue() == 2);
    }

    @Test(timeout = 10000)
    public void conditionResultsCanBeLoggedToSystemOut() {
        with()
                .conditionEvaluationListener(condition -> System.out.printf("%s (elapsed time %dms, remaining time %dms)\n", condition.getDescription(), condition.getElapsedTimeInMS(), condition.getRemainingTimeInMS()))
                .pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
                .atMost(Duration.TWO_SECONDS)
                .until(new CountDown(5), anyOf(is(0), lessThan(0)));
    }

    @Test(timeout = 10000)
    public void loggingIntermediaryHandlerLogsToSystemOut() {
        with()
                .conditionEvaluationListener(new ConditionEvaluationLogger(SECONDS))
                .pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
                .atMost(Duration.TWO_SECONDS)
                .until(new CountDown(5), is(equalTo(0)));
    }

    @Test public void
    canMakeUseOfThrowingMethodInAwaitilityToWrapRunnablesThatThrowsExceptions() {
        await().until(matches(() -> stringEquals("test", "test")));
    }

    private void stringEquals(String first, String second) throws Exception {
        Assertions.assertThat(first).isEqualTo(second);
    }
}
