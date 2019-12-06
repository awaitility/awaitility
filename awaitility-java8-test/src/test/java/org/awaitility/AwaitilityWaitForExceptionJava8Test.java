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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class AwaitilityWaitForExceptionJava8Test {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        Awaitility.reset();
    }

    @Test(timeout = 2000)
    public void waits_until_an_exception_of_a_specific_type_is_throw() {
        AtomicInteger counter = new AtomicInteger();
        Callable<Integer> countingCallable = () -> {
            int count = counter.incrementAndGet();
            if (count == 5) {
                throw new IOException("expected");
            }
            return count;
        };

        await().atMost(1000, MILLISECONDS).untilThrown(IOException.class, countingCallable);
    }

    @Test(timeout = 2000)
    public void until_thrown_returns_the_caught_exception() {
        AtomicInteger counter = new AtomicInteger();
        IOException expected = new IOException("expected");
        Callable<Integer> countingCallable = () -> {
            int count = counter.incrementAndGet();
            if (count == 5) {
                throw expected;
            }
            return count;
        };

        IOException actual = await().atMost(1000, MILLISECONDS).untilThrown(IOException.class, countingCallable);
        assertThat(actual).isSameAs(expected);
    }
}