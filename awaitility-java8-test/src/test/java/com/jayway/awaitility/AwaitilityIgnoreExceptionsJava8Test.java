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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

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
}
