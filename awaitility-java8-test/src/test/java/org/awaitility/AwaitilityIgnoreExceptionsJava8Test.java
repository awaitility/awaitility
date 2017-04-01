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
import org.awaitility.core.CheckedExceptionRethrower;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.given;

public class AwaitilityIgnoreExceptionsJava8Test {
    private FakeRepository fakeRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        fakeRepository = new FakeRepositoryImpl();
        Awaitility.reset();
    }

    @Test(timeout = 2000)
    public void exceptionIgnoringWorksWithPredicates() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).and().ignoreExceptionsMatching(e -> e.getMessage().endsWith("is not 1")).until(() -> {
            if (fakeRepository.getValue() != 1) {
                throw new IllegalArgumentException("Repository value is not 1");
            }
            return true;
        });
    }

    @Test(timeout = 2000)
    public void exceptionIgnoringWorksWithPredicatesStatically() {
        new Asynch(fakeRepository).perform();
        Awaitility.ignoreExceptionsByDefaultMatching(e -> e instanceof RuntimeException);
        await().atMost(1000, MILLISECONDS).until(() -> {
            if (fakeRepository.getValue() != 1) {
                throw new IllegalArgumentException("Repository value is not 1");
            }
            return true;
        });
    }

    @Test(timeout = 2000L)
    public void untilAssertedCanIgnoreThrowable() throws Exception {
        new Asynch(fakeRepository).perform();
        AtomicInteger counter = new AtomicInteger(0);

        given().ignoreExceptionsMatching(Objects::nonNull).await().atMost(1000, MILLISECONDS).untilAsserted(() -> {
            if (counter.incrementAndGet() < 3) {
                CheckedExceptionRethrower.safeRethrow(new Throwable("Test"));
            }
            assertThat(fakeRepository.getValue()).isEqualTo(1);
        });
    }

    /*
     * Note that this might actually be a bug/limitation. The reason is that when the exception is
     * caught and the condition reevaluated and thus end up in an infinite loop that will be
     * broken by the timeout. What ought to happen is that Awaitility somehow remembers that
     * the condition was fulfilled even though an exception is thrown later in the condition?
     * Or perhaps this behavior is correct?
     */
    @Test(timeout = 2000L, expected = ConditionTimeoutException.class)
    public void cannotHandleExceptionsThrownAfterAStatementIsFulfilled() throws Exception {
        new Asynch(fakeRepository).perform();

        given().ignoreExceptionsMatching(Objects::nonNull).await().atMost(800, MILLISECONDS).untilAsserted(() -> {
            assertThat(fakeRepository.getValue()).isEqualTo(1);
            CheckedExceptionRethrower.safeRethrow(new Throwable("Test"));
        });
    }
}
