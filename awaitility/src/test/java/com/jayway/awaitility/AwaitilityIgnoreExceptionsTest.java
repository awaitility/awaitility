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
import com.jayway.awaitility.classes.ThrowExceptionUnlessFakeRepositoryEqualsOne;
import com.jayway.awaitility.core.Predicate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.Callable;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

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
        await().atMost(1000, MILLISECONDS).and().ignoreExceptions().until(conditionsThatIsThrowingAnExceptionForATime());
    }

    @Test(timeout = 2000)
    public void exceptionsOnlySpecifiedExceptionsAreIgnored() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).and().ignoreException(IllegalArgumentException.class).until(conditionsThatIsThrowingAnExceptionForATime());
    }

    @Test(timeout = 2000)
    public void exceptionsOnlySpecifiedExceptionsAreIgnoredWhenUsingShortcut() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).and().ignoreExceptionsInstanceOf(RuntimeException.class).until(conditionsThatIsThrowingAnExceptionForATime());
    }

    @Test(timeout = 2000)
    public void exceptionsDuringEvaluationAreIgnoredWhenSetAsDefault() {
        new Asynch(fakeRepository).perform();
        Awaitility.ignoreExceptionsByDefault();
        await().atMost(1000, MILLISECONDS).until(conditionsThatIsThrowingAnExceptionForATime());
    }

    @Test(timeout = 2000)
    public void exceptionIgnoringWorksForHamcrestMatchers() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).with().ignoreExceptionsMatching(instanceOf(RuntimeException.class)).until(conditionsThatIsThrowingAnExceptionForATime());
    }

    @Test(timeout = 2000)
    public void exceptionIgnoringWorksForHamcrestMatchersStatically() {
        new Asynch(fakeRepository).perform();
        Awaitility.ignoreExceptionsByDefaultMatching(instanceOf(RuntimeException.class));
        await().atMost(1000, MILLISECONDS).until(conditionsThatIsThrowingAnExceptionForATime());
    }

    @Test(timeout = 2000)
    public void noIgnoredExceptionsHavePrecedenceOverStaticallyDefinedExceptionIgnorer() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Repository value is not 1");

        new Asynch(fakeRepository).perform();
        Awaitility.ignoreExceptionsByDefaultMatching(instanceOf(RuntimeException.class));
        await().atMost(1000, MILLISECONDS).with().ignoreNoExceptions().until(conditionsThatIsThrowingAnExceptionForATime());
    }

    @Test(timeout = 2000)
    public void exceptionIgnoringWorksForPredicates() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).with().ignoreExceptionsMatching(new Predicate<Exception>() {
            public boolean matches(Exception e) {
                return e instanceof RuntimeException;
            }
        }).until(conditionsThatIsThrowingAnExceptionForATime());
    }

    @Test(timeout = 2000)
    public void exceptionIgnoringWorksForPredicatesStatically() {
        new Asynch(fakeRepository).perform();
        Awaitility.ignoreExceptionsByDefaultMatching(new Predicate<Exception>() {
            public boolean matches(Exception e) {
                return e instanceof RuntimeException;
            }
        });
        await().atMost(1000, MILLISECONDS).until(conditionsThatIsThrowingAnExceptionForATime());
    }

    @Test(timeout = 2000)
    public void exceptionsDuringEvaluationAreReportedByDefault() {
        exception.expect(RuntimeException.class);
        exception.expectMessage(is("Repository value is not 1"));

        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).with().until(conditionsThatIsThrowingAnExceptionForATime());
    }

    private Callable<Boolean> conditionsThatIsThrowingAnExceptionForATime() {
        return new ThrowExceptionUnlessFakeRepositoryEqualsOne(fakeRepository);
    }
}
