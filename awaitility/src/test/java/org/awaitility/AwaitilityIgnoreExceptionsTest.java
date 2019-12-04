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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class AwaitilityIgnoreExceptionsTest {
    private FakeRepository fakeRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        fakeRepository = new FakeRepositoryImpl();
        Awaitility.reset();
    }

    @Test(timeout = 2000)
    public void exceptionsDuringEvaluationAreIgnoredUponRequest() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).and().ignoreExceptions().until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));
    }

    @Test(timeout = 2000)
    public void throwablesDuringEvaluationAreIgnoredUponRequest() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).and().ignoreExceptions().until(conditionsThatIsThrowingAnExceptionForATime(AssertionError.class));
    }

    @Test(timeout = 2000)
    public void exceptionsOnlySpecifiedExceptionsAreIgnored() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).and().ignoreException(IllegalArgumentException.class).until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));
    }
    
    @Test(timeout = 2000)
    public void ignoreExceptionWorksWithThrowable() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).and().ignoreException(AssertionError.class).until(conditionsThatIsThrowingAnExceptionForATime(AssertionError.class));
    }

    @Test(timeout = 2000)
    public void exceptionsOnlySpecifiedExceptionsAreIgnoredWhenUsingShortcut() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).and().ignoreExceptionsInstanceOf(RuntimeException.class).until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));
    }

    @Test(timeout = 2000)
    public void exceptionsDuringEvaluationAreIgnoredWhenSetAsDefault() {
        new Asynch(fakeRepository).perform();
        Awaitility.ignoreExceptionsByDefault();
        await().atMost(1000, MILLISECONDS).until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));
    }
    
    @Test(timeout = 2000)
    public void throwablesDuringEvaluationAreIgnoredWhenSetAsDefault() {
        new Asynch(fakeRepository).perform();
        Awaitility.ignoreExceptionsByDefault();
        await().atMost(1000, MILLISECONDS).until(conditionsThatIsThrowingAnExceptionForATime(AssertionError.class));
    }

    @Test(timeout = 2000)
    public void exceptionIgnoringWorksForHamcrestMatchers() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).with().ignoreExceptionsMatching(instanceOf(RuntimeException.class)).until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));
    }

    @Test(timeout = 2000)
    public void assertionErrorIgnoringWorksForHamcrestMatchers() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).with().ignoreExceptionsMatching(instanceOf(AssertionError.class)).until(conditionsThatIsThrowingAnExceptionForATime(AssertionError.class));
    }

    @Test(timeout = 2000)
    public void exceptionIgnoringWorksForHamcrestMatchersStatically() {
        new Asynch(fakeRepository).perform();
        Awaitility.ignoreExceptionsByDefaultMatching(instanceOf(RuntimeException.class));
        await().atMost(1000, MILLISECONDS).until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));
    }

    @Test(timeout = 2000)
    public void noIgnoredExceptionsHavePrecedenceOverStaticallyDefinedExceptionIgnorer() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Repository value is not 1");

        new Asynch(fakeRepository).perform();
        Awaitility.ignoreExceptionsByDefaultMatching(instanceOf(RuntimeException.class));
        await().atMost(1000, MILLISECONDS).with().ignoreNoExceptions().until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));
    }

    @Test(timeout = 2000)
    public void exceptionIgnoringWorksForPredicates() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).with().ignoreExceptionsMatching(RuntimeException.class::isInstance).until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));
    }

    @Test(timeout = 2000)
    public void exceptionIgnoringWorksForPredicatesStatically() {
        new Asynch(fakeRepository).perform();
        Awaitility.ignoreExceptionsByDefaultMatching(RuntimeException.class::isInstance);
        await().atMost(1000, MILLISECONDS).until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));
    }

    @Test(timeout = 2000)
    public void exceptionsDuringEvaluationAreReportedByDefault() {
        exception.expect(RuntimeException.class);
        exception.expectMessage(is("Repository value is not 1"));

        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).with().until(conditionsThatIsThrowingAnExceptionForATime(RuntimeException.class));
    }

    @Test(timeout = 2000)
    public void exceptionsDuringEvaluationAreIgnoredAndHandledUponRequest() {
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

    @Test(timeout = 2000)
    public void exceptionsDuringEvaluationAreNotHandledByDefault() {
        exception.expect(RuntimeException.class);
        exception.expectMessage(is("Repository value is not 1"));

        new Asynch(fakeRepository).perform();

        ConditionEvaluationLogger conditionEvaluationLogger = new ConditionEvaluationLogger() {
            @Override
            public void exceptionIgnored(IgnoredException ignoredException) {
                fail("should not handle exception by default");
            }
        };

        await().atMost(1000, MILLISECONDS).and()
                .pollInterval(Duration.ofMillis(250))
                .conditionEvaluationListener(conditionEvaluationLogger)
                .until(conditionsThatIsThrowingAnExceptionForATime(IllegalArgumentException.class));
    }

    private Callable<Boolean> conditionsThatIsThrowingAnExceptionForATime(Class<? extends Throwable> throwable) {
        return new ThrowExceptionUnlessFakeRepositoryEqualsOne(fakeRepository, throwable);
    }
}
