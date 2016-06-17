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

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FibonacciPollIntervalTest {

    @Test public void
    fibonacci_small_number() {
        // Given

        // When
        int fibonacci = new FibonacciPollInterval(TimeUnit.MILLISECONDS).fibonacci(10);

        // Then
        assertThat(fibonacci, is(55));
    }

    @Test public void
    fibonacci_large_number() {
        // Given

        // When
        int fibonacci = new FibonacciPollInterval(TimeUnit.MILLISECONDS).fibonacci(120);

        // Then
        assertThat(fibonacci, is(428607904));
    }
}