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

package org.awaitility.pollinterval;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FibonacciPollIntervalTest {

    @Test void fibonacci_small_number() {
        // Given

        // When
        int fibonacci = new FibonacciPollInterval(TimeUnit.MILLISECONDS).fibonacci(10);

        // Then
        assertThat(fibonacci, is(55));
    }

    @Test void fibonacci_large_number() {
        // Given

        // When
        int fibonacci = new FibonacciPollInterval(TimeUnit.MILLISECONDS).fibonacci(120);

        // Then
        assertThat(fibonacci, is(428607904));
    }

    @Test void next_default_offset() {
        // Given
        Duration unused = Duration.ofMillis(ThreadLocalRandom.current().nextLong());
        FibonacciPollInterval pollInterval = new FibonacciPollInterval(TimeUnit.SECONDS);

        // When
        Duration next = pollInterval.next(1, unused);

        // Then
        assertThat(next, is(Duration.ofSeconds(1)));
    }

    @Test void next_negative_offset() {
        // Given
        Duration unused = Duration.ofMillis(ThreadLocalRandom.current().nextLong());
        FibonacciPollInterval pollInterval = new FibonacciPollInterval(-1, TimeUnit.SECONDS);

        // When
        Duration next = pollInterval.next(1, unused);

        // Then
        assertThat(next, is(Duration.ofSeconds(0)));
    }

}