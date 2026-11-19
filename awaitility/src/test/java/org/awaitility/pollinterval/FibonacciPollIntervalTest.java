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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
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

    @Test public void
    next_default_offset() {
        // Given
        Duration unused = Duration.ofMillis(ThreadLocalRandom.current().nextLong());
        FibonacciPollInterval pollInterval = new FibonacciPollInterval(TimeUnit.SECONDS);

        // When
        Duration next = pollInterval.next(1, unused);

        // Then
        assertThat(next, is(Duration.ofSeconds(1)));
    }

    @Test public void
    next_does_not_overflow_at_poll_count_47() {
        // Given
        FibonacciPollInterval pollInterval = new FibonacciPollInterval(TimeUnit.NANOSECONDS);

        // When
        Duration next = pollInterval.next(47, Duration.ZERO);

        // Then
        assertThat(next, is(Duration.ofNanos(2971215073L)));
    }

    @Test public void
    next_returns_correct_value_at_poll_count_46() {
        // Given
        FibonacciPollInterval pollInterval = new FibonacciPollInterval(TimeUnit.NANOSECONDS);

        // When
        Duration next = pollInterval.next(46, Duration.ZERO);

        // Then
        assertThat(next, is(Duration.ofNanos(1836311903L)));
    }

    @Test public void
    next_saturates_for_very_large_poll_counts() {
        // Given
        FibonacciPollInterval pollInterval = new FibonacciPollInterval(TimeUnit.NANOSECONDS);

        // When
        Duration next = pollInterval.next(200, Duration.ZERO);

        // Then
        assertThat(next, is(Duration.ofNanos(Long.MAX_VALUE)));
    }

    @Test public void
    next_uses_offset_plus_poll_count_around_the_int_overflow_boundary() {
        // Given
        FibonacciPollInterval pollInterval = new FibonacciPollInterval(5, TimeUnit.NANOSECONDS);

        // When
        Duration atValue46 = pollInterval.next(41, Duration.ZERO);
        Duration atValue47 = pollInterval.next(42, Duration.ZERO);

        // Then
        assertThat(atValue46, is(Duration.ofNanos(1836311903L)));
        assertThat(atValue47, is(Duration.ofNanos(2971215073L)));
    }

    @Test public void
    next_returns_exact_long_fibonacci_numbers_until_long_overflow() {
        // Given
        FibonacciPollInterval pollInterval = new FibonacciPollInterval(TimeUnit.NANOSECONDS);

        // When
        Duration atValue92 = pollInterval.next(92, Duration.ZERO);
        Duration atValue93 = pollInterval.next(93, Duration.ZERO);

        // Then
        assertThat(atValue92, is(Duration.ofNanos(7540113804746346429L)));
        assertThat(atValue93, is(Duration.ofNanos(Long.MAX_VALUE)));
    }

    @Test public void
    next_for_very_large_poll_counts_is_capped_to_the_largest_duration_expressible_in_nanoseconds() {
        // Given
        FibonacciPollInterval pollInterval = new FibonacciPollInterval(TimeUnit.MILLISECONDS);

        // When
        Duration next = pollInterval.next(Integer.MAX_VALUE, Duration.ZERO);

        // Then
        assertThat(next, is(Duration.ofMillis(Long.MAX_VALUE / 1_000_000L)));
    }

    @Test public void
    next_above_46_is_non_decreasing_non_negative_and_never_throws_for_any_time_unit() {
        for (TimeUnit unit : TimeUnit.values()) {
            // Given
            FibonacciPollInterval pollInterval = new FibonacciPollInterval(unit);
            Duration previous = pollInterval.next(46, Duration.ZERO);

            for (int pollCount = 47; pollCount <= 500; pollCount++) {
                // When
                Duration next = pollInterval.next(pollCount, previous);

                // Then
                assertThat(unit + " at " + pollCount, next.isNegative(), is(false));
                assertThat(unit + " at " + pollCount, next, greaterThanOrEqualTo(previous));
                assertThat(unit + " at " + pollCount, next.toNanos(), greaterThan(0L));
                previous = next;
            }
        }
    }

    @Test public void
    next_honors_overridden_fibonacci_method_up_to_46() {
        // Given
        final List<Integer> invokedWith = new ArrayList<>();
        FibonacciPollInterval pollInterval = new FibonacciPollInterval(2, TimeUnit.MILLISECONDS) {
            @Override
            protected int fibonacci(int value) {
                invokedWith.add(value);
                return value * 10;
            }
        };

        // When
        Duration atValue3 = pollInterval.next(1, Duration.ZERO);
        Duration atValue46 = pollInterval.next(44, Duration.ZERO);
        Duration atValue47 = pollInterval.next(45, Duration.ZERO);

        // Then
        assertThat(atValue3, is(Duration.ofMillis(30)));
        assertThat(atValue46, is(Duration.ofMillis(460)));
        assertThat(atValue47, is(Duration.ofMillis(2971215073L)));
        assertThat(invokedWith, is(Arrays.asList(3, 46)));
    }

    @Test public void
    next_negative_offset() {
        // Given
        Duration unused = Duration.ofMillis(ThreadLocalRandom.current().nextLong());
        FibonacciPollInterval pollInterval = new FibonacciPollInterval(-1, TimeUnit.SECONDS);

        // When
        Duration next = pollInterval.next(1, unused);

        // Then
        assertThat(next, is(Duration.ofSeconds(0)));
    }

}