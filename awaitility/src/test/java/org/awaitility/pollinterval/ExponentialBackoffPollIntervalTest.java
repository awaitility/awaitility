/*
 * Copyright 2026 the original author or authors.
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

import org.awaitility.Awaitility;
import org.awaitility.core.ForeverDuration;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ExponentialBackoffPollIntervalTest {

    @Test
    public void requestedSequenceStartsThenDoublesAndStaysCapped() throws Exception {
        PollInterval interval = create(Duration.ofMillis(500), Duration.ofSeconds(5));
        Duration previous = null;
        long[] expectedMillis = {500, 1000, 2000, 4000, 5000, 5000};
        for (int index = 0; index < expectedMillis.length; index++) {
            previous = interval.next(index + 1, previous);
            assertEquals(Duration.ofMillis(expectedMillis[index]), previous);
        }
    }

    @Test
    public void firstIntervalIgnoresPreviousDurationAndPollDelay() throws Exception {
        PollInterval interval = create(Duration.ofMillis(3), Duration.ofMillis(9));
        assertEquals(Duration.ofMillis(3), interval.next(1, Duration.ofSeconds(30)));
        assertEquals(Duration.ofMillis(3), interval.next(1, null));
    }

    @Test
    public void preservesNanosecondPrecisionAtOddMaximum() throws Exception {
        PollInterval interval = create(Duration.ofNanos(1), Duration.ofNanos(3));
        assertEquals(Duration.ofNanos(1), interval.next(1, null));
        assertEquals(Duration.ofNanos(2), interval.next(2, Duration.ofNanos(1)));
        assertEquals(Duration.ofNanos(3), interval.next(3, Duration.ofNanos(2)));
        assertEquals(Duration.ofNanos(3), interval.next(4, Duration.ofNanos(3)));
    }

    @Test
    public void capsBeforeDurationMultiplicationCanOverflow() throws Exception {
        Duration maximum = Duration.ofSeconds(Long.MAX_VALUE - 1);
        PollInterval interval = create(Duration.ofSeconds(1), maximum);
        Duration half = maximum.dividedBy(2);
        assertEquals(half.multipliedBy(2), interval.next(2, half));
        assertEquals(maximum, interval.next(3, half.plusNanos(1)));
        assertEquals(maximum, interval.next(4, maximum));
        assertEquals(maximum, interval.next(5, ForeverDuration.FOREVER));
    }

    @Test
    public void equalStartAndMaximumRemainConstant() throws Exception {
        Duration value = Duration.ofMillis(5);
        PollInterval interval = create(value, value);
        assertEquals(value, interval.next(1, null));
        assertEquals(value, interval.next(2, value));
    }

    @Test
    public void rejectsInvalidConstructorDurations() throws Exception {
        Duration one = Duration.ofSeconds(1);
        for (Duration invalid : Arrays.asList(null, Duration.ZERO, Duration.ofNanos(-1), ForeverDuration.FOREVER)) {
            expectInvalid(invalid, one);
            expectInvalid(one, invalid);
        }
        expectInvalid(Duration.ofSeconds(2), one);
    }

    @Test
    public void rejectsInvalidPreviousDurationAfterFirstPoll() throws Exception {
        PollInterval interval = create(Duration.ofNanos(1), Duration.ofNanos(3));
        try {
            interval.next(2, null);
            fail("null previous duration");
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
        try {
            interval.next(2, Duration.ofNanos(-1));
            fail("negative previous duration");
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
    }

    @Test
    public void factoryAndConstructorHaveValueSemanticsWithoutMutableState() throws Exception {
        Duration start = Duration.ofMillis(1);
        Duration maximum = Duration.ofMillis(5);
        PollInterval constructor = create(start, maximum);
        Object factory = intervalType().getMethod("exponential", Duration.class, Duration.class).invoke(null, start, maximum);
        assertEquals(constructor, factory);
        assertEquals(constructor.hashCode(), factory.hashCode());
        assertNotEquals(constructor, create(start, Duration.ofMillis(6)));
        assertNotEquals(constructor, create(Duration.ofMillis(2), maximum));
        assertNotEquals(constructor, null);
        assertNotEquals(constructor, "other");
        assertTrue(constructor.toString().contains("startDuration=PT0.001S"));
        constructor.next(8, maximum);
        assertEquals(start, constructor.next(1, null));
    }

    @Test
    public void publicAwaitilityApiUsesTheStrategy() throws Exception {
        PollInterval interval = create(Duration.ofNanos(1), Duration.ofNanos(4));
        List<Duration> intervals = new ArrayList<>();
        AtomicInteger evaluations = new AtomicInteger();
        int result = Awaitility.await().pollInSameThread().atMost(Duration.ofSeconds(2))
                .pollInterval((count, previous) -> {
                    Duration next = interval.next(count, previous);
                    intervals.add(next);
                    return next;
                }).until(evaluations::incrementAndGet, value -> value == 3);
        assertEquals(3, result);
        assertEquals(Arrays.asList(Duration.ofNanos(1), Duration.ofNanos(2)), intervals);
    }

    @Test
    public void existingFixedIntervalIsUnchanged() {
        Duration fixed = Duration.ofMillis(123);
        assertEquals(fixed, new FixedPollInterval(fixed).next(1, Duration.ZERO));
        assertEquals(fixed, new FixedPollInterval(fixed).next(8, Duration.ofSeconds(10)));
    }

    @Test
    public void existingIterativeFirstCallStillAppliesFunction() {
        IterativePollInterval interval = new IterativePollInterval(value -> value.multipliedBy(2), Duration.ofMillis(500));
        assertEquals(Duration.ofSeconds(1), interval.next(1, Duration.ZERO));
        assertEquals(Duration.ofSeconds(2), interval.next(2, Duration.ofSeconds(1)));
    }

    // Reflective construction allows the exact same regression source to compile on base.
    // Missing class/factory on base is the intended feature failure, not a setup workaround.
    private static Class<?> intervalType() throws ClassNotFoundException {
        return Class.forName("org.awaitility.pollinterval.ExponentialBackoffPollInterval");
    }

    private static PollInterval create(Duration start, Duration maximum) throws Exception {
        return (PollInterval) intervalType().getConstructor(Duration.class, Duration.class).newInstance(start, maximum);
    }

    private static void expectInvalid(Duration start, Duration maximum) throws Exception {
        try {
            create(start, maximum);
            fail("invalid durations should be rejected");
        } catch (InvocationTargetException ex) {
            assertTrue(ex.getCause() instanceof IllegalArgumentException);
        }
    }
}
