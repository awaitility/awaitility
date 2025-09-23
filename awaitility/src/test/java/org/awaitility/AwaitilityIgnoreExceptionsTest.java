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

package org.awaitility;

import org.awaitility.classes.Asynch;
import org.awaitility.classes.FakeRepository;
import org.awaitility.classes.FakeRepositoryImpl;
import org.awaitility.classes.ThrowExceptionUnlessFakeRepositoryEqualsOne;
import org.awaitility.core.ConditionEvaluationLogger;
import org.awaitility.core.IgnoredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;

class AwaitilityIgnoreExceptionsTest {
    private FakeRepository fakeRepository;

    @BeforeEach 
    void setup() {
        fakeRepository = new FakeRepositoryImpl();
        Awaitility.reset();
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void exceptionsDuringEvaluationAreIgnoredUponRequest() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).and().ignoreExceptions().until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void throwablesDuringEvaluationAreIgnoredUponRequest() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).and().ignoreExceptions().until(conditionsThatIsThrowingAnExceptionForATime(AssertionError.class));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void exceptionsOnlySpecifiedExceptionsAreIgnored() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).and().ignoreException(IllegalArgumentException.class).until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void ignoreExceptionWorksWithThrowable() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).and().ignoreException(AssertionError.class).until(conditionsThatIsThrowingAnExceptionForATime(AssertionError.class));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void exceptionsOnlySpecifiedExceptionsAreIgnoredWhenUsingShortcut() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).and().ignoreExceptionsInstanceOf(RuntimeException.class).until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void exceptionsDuringEvaluationAreIgnoredWhenSetAsDefault() {
        new Asynch(fakeRepository).perform();
        Awaitility.ignoreExceptionsByDefault();
        await().atMost(1000, MILLISECONDS).until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void throwablesDuringEvaluationAreIgnoredWhenSetAsDefault() {
        new Asynch(fakeRepository).perform();
        Awaitility.ignoreExceptionsByDefault();
        await().atMost(1000, MILLISECONDS).until(conditionsThatIsThrowingAnExceptionForATime(AssertionError.class));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void exceptionIgnoringWorksForHamcrestMatchers() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).with().ignoreExceptionsMatching(instanceOf(RuntimeException.class)).until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void assertionErrorIgnoringWorksForHamcrestMatchers() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).with().ignoreExceptionsMatching(instanceOf(AssertionError.class)).until(conditionsThatIsThrowingAnExceptionForATime(AssertionError.class));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void exceptionIgnoringWorksForHamcrestMatchersStatically() {
        new Asynch(fakeRepository).perform();
        Awaitility.ignoreExceptionsByDefaultMatching(instanceOf(RuntimeException.class));
        await().atMost(1000, MILLISECONDS).until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void noIgnoredExceptionsHavePrecedenceOverStaticallyDefinedExceptionIgnorer() {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {

            new Asynch(fakeRepository).perform();
            Awaitility.ignoreExceptionsByDefaultMatching(instanceOf(RuntimeException.class));
            await().atMost(1000, MILLISECONDS).with().ignoreNoExceptions().until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));
        });
        assertThat(exception.getMessage(), containsString("Repository value is not 1"));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void exceptionIgnoringWorksForPredicates() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).with().ignoreExceptionsMatching(RuntimeException.class::isInstance).until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void exceptionIgnoringWorksForPredicatesStatically() {
        new Asynch(fakeRepository).perform();
        Awaitility.ignoreExceptionsByDefaultMatching(RuntimeException.class::isInstance);
        await().atMost(1000, MILLISECONDS).until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void exceptionsDuringEvaluationAreReportedByDefault() {
        Throwable exception = assertThrows(RuntimeException.class, () -> {

            new Asynch(fakeRepository).perform();
            await().atMost(1000, MILLISECONDS).with().until(conditionsThatIsThrowingAnExceptionForATime(RuntimeException.class));
        });
        assertThat(exception.getMessage(), is("Repository value is not 1"));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void exceptionsDuringEvaluationAreIgnoredAndHandledUponRequest() {
        new Asynch(fakeRepository).perform();

        AtomicInteger exceptionCounter = new AtomicInteger(0);
        ConditionEvaluationLogger conditionEvaluationLogger = new ConditionEvaluationLogger() {
            @Override
            public void exceptionIgnored(IgnoredException ignoredException) {
                exceptionCounter.incrementAndGet();
            }
        };

        await().atMost(1000, MILLISECONDS).and()
                .pollInterval(Duration.ofMillis(250))
                .ignoreExceptions()
                .conditionEvaluationListener(conditionEvaluationLogger)
                .until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));

        assertThat(exceptionCounter.get(),is(2));

    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void exceptionsDuringEvaluationAreNotHandledByDefault() {
        Throwable exception = assertThrows(RuntimeException.class, () -> {

            new Asynch(fakeRepository).perform();

            ConditionEvaluationLogger conditionEvaluationLogger = new ConditionEvaluationLogger() {
                @Override public void exceptionIgnored(IgnoredException ignoredException) {
                    fail("should not handle exception by default");
                }
            };

            await().atMost(1000, MILLISECONDS).and()
                    .pollInterval(Duration.ofMillis(250))
                    .conditionEvaluationListener(conditionEvaluationLogger)
                    .until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));
        });
        assertThat(exception.getMessage(), is("Repository value is not 1"));
    }

    private Callable<Boolean> conditionsThatIsThrowingAnExceptionForATime(Class<? extends Throwable> throwable) {
        return new ThrowExceptionUnlessFakeRepositoryEqualsOne(fakeRepository, throwable);
    }
}
