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
import org.awaitility.classes.Asynch;
import org.awaitility.classes.FakeRepository;
import org.awaitility.classes.FakeRepositoryImpl;
import org.awaitility.classes.FakeRepositoryList;
import org.awaitility.core.ConditionEvaluationLogger;
import org.awaitility.core.ConditionTimeoutException;
import org.awaitility.core.ThrowingRunnable;
import org.awaitility.support.CountDown;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.opentest4j.AssertionFailedError;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.*;
import static org.awaitility.Durations.ONE_HUNDRED_MILLISECONDS;
import static org.awaitility.Durations.TWO_SECONDS;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;

/**
 * Tests for await().until(Runnable) using AssertionCondition.
 *
 * @author Marcin Zajączkowski, 2014-03-28
 * @author Johan Haleby
 */
class AwaitilityJava8Test {

    private FakeRepository fakeRepository;

    @BeforeEach
    void setup() {
        fakeRepository = new FakeRepositoryImpl();
        Awaitility.reset();
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void awaitAssertJAssertionAsLambda() {
        new Asynch(fakeRepository).perform();
        await().untilAsserted(() -> Assertions.assertThat(fakeRepository.getValue()).isEqualTo(1));
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void awaitUsingLambdaVersionOfCallableBoolean() {
        new Asynch(fakeRepository).perform();
        await().until(() -> fakeRepository.getValue() == 1);
    }

    @SuppressWarnings("Convert2Lambda")
    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void awaitAssertJAssertionAsAnonymousClass() {
        new Asynch(fakeRepository).perform();
        await().untilAsserted(new ThrowingRunnable() {
            @Override
            public void run() {
                Assertions.assertThat(fakeRepository.getValue()).isEqualTo(1);
            }
        });
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void awaitAssertJAssertionDisplaysOriginalErrorMessageAndTimeoutWhenConditionTimeoutExceptionOccurs() {
        new Asynch(fakeRepository).perform();

        ConditionTimeoutException ex = assertThrows(ConditionTimeoutException.class, 
                () -> with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).untilAsserted(
                () -> Assertions.assertThat(fakeRepository.getValue()).isEqualTo(1))
        );
        assertThat(ex.getMessage()).startsWith("Assertion condition defined as a lambda expression in " + AwaitilityJava8Test.class.getName())
                .endsWith("expected: 1\r\n but was: 0 within 120 milliseconds.");
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void awaitJUnitAssertionAsLambda() {
        new Asynch(fakeRepository).perform();
        await().untilAsserted(() -> assertEquals(1, fakeRepository.getValue()));
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void awaitJUnitAssertionDisplaysOriginalErrorMessageAndTimeoutWhenConditionTimeoutExceptionOccurs() {
        ConditionTimeoutException ex = assertThrows(ConditionTimeoutException.class,
                () -> with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).untilAsserted(
                () -> assertEquals(1, fakeRepository.getValue()))
        );
        assertThat(ex.getMessage()).startsWith("Assertion condition defined as a lambda expression in " + AwaitilityJava8Test.class.getName())
                .endsWith("expected: <1> but was: <0> within 120 milliseconds.");
    }

    /**
     * See <a href="https://github.com/awaitility/awaitility/issues/108">issue 108</a>
     */
    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void doesntRepeatAliasInLambdaConditionsForAssertConditions() {
        ConditionTimeoutException ex = assertThrows(ConditionTimeoutException.class,
                () -> with().pollInterval(10, MILLISECONDS).then().await("my alias").atMost(120, MILLISECONDS).untilAsserted(
                () -> assertEquals(1, fakeRepository.getValue()))
        );
        assertThat(countOfOccurrences(ex.getMessage(), "my alias")).isEqualTo(1);
    }

    /**
     * See <a href="https://github.com/awaitility/awaitility/issues/108">issue 108</a>
     */
    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void doesntRepeatAliasInLambdaConditionsForCallableConditions() {
         ConditionTimeoutException ex = assertThrows(ConditionTimeoutException.class,
                 () -> with().pollInterval(10, MILLISECONDS).then().await("my alias").atMost(120, MILLISECONDS).until(() -> fakeRepository.getValue() == 1));
         assertThat(countOfOccurrences(ex.getMessage(), "my alias")).isEqualTo(1);
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void lambdaErrorMessageLooksAlrightWhenUsingMethodReferences() {
        ConditionTimeoutException ex = assertThrows(ConditionTimeoutException.class,
                () -> with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).until(fakeRepository::getValue, equalTo(1))
        );
        assertThat(ex.getMessage()).isEqualTo("Lambda expression in org.awaitility.AwaitilityJava8Test that uses org.awaitility.classes.FakeRepository: expected <1> but was <0> within 120 milliseconds.");
    }

    @SuppressWarnings("Convert2MethodRef")
    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void lambdaErrorMessageLooksAlrightWhenUsingLambda() {
        ConditionTimeoutException ex = assertThrows(ConditionTimeoutException.class,
                () -> with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).until(() -> fakeRepository.getValue(), equalTo(1))
        );
        assertThat(ex.getMessage()).isEqualTo("Lambda expression in org.awaitility.AwaitilityJava8Test: expected <1> but was <0> within 120 milliseconds.");
    }

    @SuppressWarnings({"Convert2MethodRef", "CodeBlock2Expr"})
    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void lambdaErrorMessageLooksAlrightWhenUsingLambdaWithCurlyBraces() {
        ConditionTimeoutException ex = assertThrows(ConditionTimeoutException.class,
                () -> with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).until(() -> {
                    return fakeRepository.getValue();
                }, equalTo(1))
        );
        assertThat(ex.getMessage()).isEqualTo("Lambda expression in org.awaitility.AwaitilityJava8Test: expected <1> but was <0> within 120 milliseconds.");
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void lambdaErrorMessageLooksAlrightWhenAwaitUsingLambdaVersionOfCallableBoolean() {
        ConditionTimeoutException ex = assertThrows(ConditionTimeoutException.class,
                () -> await().atMost(200, MILLISECONDS).until(() -> fakeRepository.getValue() == 2)
        );
        assertThat(ex.getMessage()).isEqualTo("Condition with lambda expression in org.awaitility.AwaitilityJava8Test was not fulfilled within 200 milliseconds.");
    }

    @Timeout(value = 10000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void conditionResultsCanBeLoggedToSystemOut() {
        with()
                .conditionEvaluationListener(condition -> System.out.printf("%s (elapsed time %dms, remaining time %dms)\n", condition.getDescription(), condition.getElapsedTimeInMS(), condition.getRemainingTimeInMS()))
                .pollInterval(ONE_HUNDRED_MILLISECONDS)
                .atMost(TWO_SECONDS)
                .until(new CountDown(5), anyOf(is(0), lessThan(0)));
    }

    @Timeout(value = 10000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void loggingIntermediaryHandlerLogsToSystemOut() {
        with()
                .conditionEvaluationListener(new ConditionEvaluationLogger(SECONDS))
                .pollInterval(ONE_HUNDRED_MILLISECONDS)
                .atMost(TWO_SECONDS)
                .until(new CountDown(5), is(equalTo(0)));
    }

    @Test void
    canMakeUseOfThrowingMethodInAwaitilityToWrapRunnablesThatThrowsExceptions() {
        await().untilAsserted(() -> stringEquals("test", "test"));
    }

    @SuppressWarnings("ObviousNullCheck")
    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void includesCauseInStackTrace()  {
        ConditionTimeoutException ex = assertThrows(ConditionTimeoutException.class, () ->
            await().atMost(200, MILLISECONDS).untilAsserted(() -> {
                assertNotNull("34");
                assertNotNull(null);
            })
        );
        assertThat(ex.getCause().getClass().getName()).isEqualTo(AssertionFailedError.class.getName());
    }

    // This was previously a bug (https://github.com/awaitility/awaitility/issues/78)
    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void throwsExceptionImmediatelyWhenCallableConditionThrowsAssertionError() throws Exception {
        // Given
        long timeStart = System.nanoTime();
        new Asynch(fakeRepository).perform();

        // When
        final AtomicInteger counter = new AtomicInteger(0);
        assertThrows(AssertionError.class,
                () -> await().atMost(1500, MILLISECONDS).until(() -> {
                    counter.incrementAndGet();
                    assertTrue(counter.get() >= 2);
                    return true;
                })
        );

        // Then
        long timeEnd = System.nanoTime();
        assertThat(NANOSECONDS.toMillis(timeEnd - timeStart)).isLessThan(1500L);
    }

    // Asserts that https://github.com/awaitility/awaitility/issues/87 is resolved
    @Test
    void errorMessageLooksOkForHamcrestLambdaExpressionsWhoseMismatchDescriptionOriginallyIsEmptyStringByHamcrest() throws Exception {
        // Given
        FakeRepositoryList fakeRepositoryList = new FakeRepositoryList();

        new Thread(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            fakeRepositoryList.add("hello");
        });
            
        // When
        ConditionTimeoutException ex = assertThrows(ConditionTimeoutException.class, () ->
                given().pollDelay(10, MILLISECONDS).await().atMost(50, MILLISECONDS).until(fakeRepositoryList::state, hasItem(endsWith("hello")))
        );
        assertThat(ex.getMessage()).endsWith("expected a collection containing a string ending with \"hello\" but was empty within 50 milliseconds.");
    }

    // Asserts that https://github.com/awaitility/awaitility/issues/97 is resolved
    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void longConditionThrowsConditionTimeoutException() throws Exception {
        ConditionTimeoutException ex = assertThrows(ConditionTimeoutException.class, () ->
                given().pollDelay(10, MILLISECONDS).await().atMost(50, MILLISECONDS).until(() -> {
                    Thread.sleep(1000);
                    return false;
                })
        );
        assertThat(ex.getMessage()).isEqualTo("Condition with Lambda expression in org.awaitility.AwaitilityJava8Test was not fulfilled within 50 milliseconds.");
    }

    private void stringEquals(String first, String second) {
        Assertions.assertThat(first).isEqualTo(second);
    }

    private static int countOfOccurrences(String str, String subStr) {
        return (str.length() - str.replaceAll(Pattern.quote(subStr), "").length()) / subStr.length();
    }
}
