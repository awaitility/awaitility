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
package org.awaitility.core;

import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.awaitility.constraint.AtMostWaitConstraint;
import org.awaitility.pollinterval.FixedPollInterval;
import org.junit.After;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * How {@link ConditionFactory#during(Duration)} interacts with the at most timeout and the poll delay
 * (issues 91, 165, 186 and 264).
 * <p>
 * The hold period starts at the first successful evaluation, never earlier than the poll delay, while the timeout counts
 * from the start of the await. So for poll delay p and during(d), a condition that is true from the first evaluation on:
 * <ul>
 * <li>succeeds when atMost is not set by the user, since the default timeout is extended by d,</li>
 * <li>fails at once when the user sets p &lt; atMost &lt;= p + d,</li>
 * <li>behaves as before when the user sets an atMost comfortably larger than p + d.</li>
 * </ul>
 */
public class DuringTimeoutTest {

    private static final Callable<Boolean> ALWAYS_TRUE = () -> true;

    @After
    public void resetAwaitility() {
        Awaitility.reset();
    }

    // Effective timeout

    @Test
    public void builtInDefaultTimeoutIsExtendedByTheDuringPeriod() {
        ConditionSettings settings = await().during(Duration.ofSeconds(10)).generateConditionSettings();

        assertEquals(Duration.ofSeconds(20), settings.getMaxWaitTime());
        assertEquals(Duration.ofSeconds(10), settings.getHoldPredicateTime());
        assertNull(settings.getUnsatisfiableHoldMessage());
    }

    @Test
    public void defaultTimeoutSetWithSetDefaultTimeoutIsExtendedByTheDuringPeriod() {
        Awaitility.setDefaultTimeout(Duration.ofSeconds(5));

        assertEquals(Duration.ofSeconds(7), await().during(Duration.ofSeconds(2)).generateConditionSettings().getMaxWaitTime());
        assertEquals(Duration.ofSeconds(7), await().during(2000, MILLISECONDS).generateConditionSettings().getMaxWaitTime());
    }

    @Test
    public void defaultTimeoutFromThePublicConstructorIsExtendedByTheDuringPeriod() {
        ConditionFactory conditionFactory = new ConditionFactory(null, AtMostWaitConstraint.TEN_SECONDS, new FixedPollInterval(Duration.ofMillis(100)),
                null, false, e -> false, null, null, null);

        assertEquals(Duration.ofSeconds(11), conditionFactory.during(Duration.ofSeconds(1)).generateConditionSettings().getMaxWaitTime());
    }

    @Test
    public void foreverDefaultTimeoutStaysForever() {
        Awaitility.setDefaultTimeout(Durations.FOREVER);

        assertEquals(Durations.FOREVER, await().during(Duration.ofSeconds(1)).generateConditionSettings().getMaxWaitTime());
    }

    @Test
    public void userSetAtMostIsNotExtended() {
        ConditionSettings settings = await().atMost(Duration.ofSeconds(5)).during(Duration.ofSeconds(1)).generateConditionSettings();

        assertEquals(Duration.ofSeconds(5), settings.getMaxWaitTime());
        assertNull(settings.getUnsatisfiableHoldMessage());
    }

    @Test
    public void defaultTimeoutWithoutDuringIsNotExtended() {
        assertEquals(Duration.ofSeconds(10), await().generateConditionSettings().getMaxWaitTime());
        assertEquals(Duration.ofSeconds(10), await().atLeast(Duration.ofMillis(10)).generateConditionSettings().getMaxWaitTime());
    }

    @Test
    public void pollDelayIsCheckedAgainstTheUnextendedDefaultTimeout() {
        Awaitility.setDefaultTimeout(Duration.ofMillis(100));

        try {
            await().pollDelay(Duration.ofMillis(200)).during(Duration.ofSeconds(1)).until(ALWAYS_TRUE);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Timeout (100 milliseconds) must be greater than the poll delay (200 milliseconds).", e.getMessage());
        }
    }

    // (a) atMost not set by the user: the await succeeds

    @Test(timeout = 2000L)
    public void duringEqualToTheDefaultTimeoutSucceeds() {
        Awaitility.setDefaultTimeout(Duration.ofMillis(500));

        long elapsed = measure(() -> await().during(Duration.ofMillis(500)).until(ALWAYS_TRUE));

        assertThat(elapsed, greaterThanOrEqualTo(500L));
    }

    @Test(timeout = 2000L)
    public void duringLongerThanTheDefaultTimeoutMinusThePollDelaySucceeds() {
        Awaitility.setDefaultTimeout(Duration.ofMillis(300));

        await().pollDelay(Duration.ofMillis(100)).during(Duration.ofMillis(500)).until(ALWAYS_TRUE);
    }

    @Test(timeout = 2000L)
    public void duringWithHamcrestMatcherAndDefaultTimeoutSucceeds() {
        Awaitility.setDefaultTimeout(Duration.ofMillis(300));

        assertEquals(Integer.valueOf(1), await().during(Duration.ofMillis(400)).until(() -> 1, equalTo(1)));
    }

    @Test(timeout = 2000L)
    public void awaiterUsesTheExtendedBuiltInDefaultTimeout() {
        // during(10s) with the built-in 10 second default could never succeed before. We check the timeout that the
        // awaiter reports, then stop the await by throwing from the condition instead of waiting 10 seconds.
        AtomicLong remainingTimeAtStart = new AtomicLong();
        AtomicInteger evaluations = new AtomicInteger();
        try {
            await()
                    .conditionEvaluationListener(new ConditionEvaluationListener<Boolean>() {
                        @Override
                        public void conditionEvaluated(EvaluatedCondition<Boolean> condition) {
                        }

                        @Override
                        public void beforeEvaluation(StartEvaluationEvent<Boolean> startEvaluationEvent) {
                            remainingTimeAtStart.set(startEvaluationEvent.getRemainingTimeInMS());
                        }
                    })
                    .during(Duration.ofSeconds(10))
                    .until(() -> {
                        if (evaluations.incrementAndGet() > 1) {
                            throw new IllegalStateException("stop");
                        }
                        return true;
                    });
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("stop", e.getMessage());
        }

        assertEquals(20_000L, remainingTimeAtStart.get());
    }

    // (b) atMost set by the user and pollDelay < atMost <= pollDelay + during: fails at once

    @Test(timeout = 2000L)
    public void userSetAtMostThatCannotFitTheDuringPeriodFailsImmediately() {
        AtomicInteger evaluations = new AtomicInteger();
        AtomicInteger timeouts = new AtomicInteger();
        long start = System.nanoTime();

        try {
            await()
                    .atMost(Duration.ofSeconds(1))
                    .pollDelay(Duration.ofMillis(100))
                    .during(Duration.ofSeconds(5))
                    .conditionEvaluationListener(new ConditionEvaluationListener<Boolean>() {
                        @Override
                        public void conditionEvaluated(EvaluatedCondition<Boolean> condition) {
                        }

                        @Override
                        public void onTimeout(TimeoutEvent timeoutEvent) {
                            timeouts.incrementAndGet();
                        }
                    })
                    .until(() -> {
                        evaluations.incrementAndGet();
                        return true;
                    });
            fail("Expected ConditionTimeoutException");
        } catch (ConditionTimeoutException e) {
            long elapsedMillis = (System.nanoTime() - start) / 1_000_000L;
            assertThat(elapsedMillis, lessThan(500L));
            assertEquals("Condition can never be fulfilled: during(5 seconds) needs the condition to hold for longer than " +
                    "atMost(1 second) minus pollDelay(100 milliseconds) allows. Use an atMost larger than 5 seconds 100 milliseconds, " +
                    "or leave atMost unset so that the default timeout is extended by the during period.", e.getMessage());
        }

        assertEquals(1, timeouts.get());
        assertEquals(0, evaluations.get());
    }

    @Test(timeout = 2000L)
    public void userSetAtMostEqualToPollDelayPlusDuringFailsImmediately() {
        long elapsed = measure(() -> {
            try {
                await("my alias").atMost(Duration.ofMillis(600)).pollDelay(Duration.ofMillis(100)).during(Duration.ofMillis(500)).until(ALWAYS_TRUE);
                fail("Expected ConditionTimeoutException");
            } catch (ConditionTimeoutException e) {
                assertThat(e.getMessage(), containsString("Condition with alias 'my alias' can never be fulfilled: during(500 milliseconds) " +
                        "needs the condition to hold for longer than atMost(600 milliseconds) minus pollDelay(100 milliseconds) allows."));
            }
        });

        assertThat(elapsed, lessThan(300L));
    }

    @Test(timeout = 2000L)
    public void duringEqualToUserSetAtMostFailsImmediately() {
        // The case from issue 186: during(100ms).timeout(150ms) with the default 100 ms poll delay
        long elapsed = measure(() -> {
            try {
                await().during(100, MILLISECONDS).timeout(Duration.ofMillis(150)).until(ALWAYS_TRUE);
                fail("Expected ConditionTimeoutException");
            } catch (ConditionTimeoutException e) {
                assertThat(e.getMessage(), containsString("can never be fulfilled"));
            }
        });

        assertThat(elapsed, lessThan(100L));
    }

    @Test(timeout = 2000L)
    public void waitAtMostThatCannotFitTheDuringPeriodFailsImmediately() {
        try {
            Awaitility.waitAtMost(Duration.ofMillis(300)).during(Duration.ofMillis(300)).until(ALWAYS_TRUE);
            fail("Expected ConditionTimeoutException");
        } catch (ConditionTimeoutException e) {
            assertThat(e.getMessage(), containsString("can never be fulfilled"));
        }
    }

    @Test
    public void userSetAtMostNotLargerThanPollDelayStillThrowsIllegalArgumentException() {
        try {
            await().atMost(Duration.ofMillis(100)).pollDelay(Duration.ofMillis(200)).during(Duration.ofSeconds(1)).until(ALWAYS_TRUE);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Timeout (100 milliseconds) must be greater than the poll delay (200 milliseconds).", e.getMessage());
        }
    }

    // (c) Forever

    @Test(timeout = 2000L)
    public void foreverDefaultTimeoutWithDuringSucceeds() {
        Awaitility.setDefaultTimeout(Durations.FOREVER);

        long elapsed = measure(() -> await().during(Duration.ofSeconds(1)).until(ALWAYS_TRUE));

        assertThat(elapsed, greaterThanOrEqualTo(1000L));
    }

    @Test(timeout = 2000L)
    public void foreverThenDuringSucceeds() {
        long elapsed = measure(() -> await().forever().during(Duration.ofMillis(300)).until(ALWAYS_TRUE));

        assertThat(elapsed, greaterThanOrEqualTo(300L));
    }

    // atLeast and during

    @Test(timeout = 2000L)
    public void atLeastAndDuringWithExtendedDefaultTimeoutSucceeds() {
        Awaitility.setDefaultTimeout(Duration.ofMillis(400));

        long elapsed = measure(() -> await().atLeast(Duration.ofMillis(100)).during(Duration.ofMillis(400)).until(ALWAYS_TRUE));

        assertThat(elapsed, greaterThanOrEqualTo(400L));
    }

    @Test(timeout = 2000L)
    public void betweenThatCannotFitTheDuringPeriodFailsImmediately() {
        try {
            await().between(Duration.ofMillis(50), Duration.ofMillis(300)).during(Duration.ofMillis(500)).until(ALWAYS_TRUE);
            fail("Expected ConditionTimeoutException");
        } catch (ConditionTimeoutException e) {
            assertThat(e.getMessage(), containsString("can never be fulfilled"));
        }
    }

    // A user atMost comfortably larger than pollDelay + during behaves as before

    @Test(timeout = 2000L)
    public void userSetAtMostLargerThanPollDelayPlusDuringSucceeds() {
        long elapsed = measure(() -> await().atMost(Duration.ofMillis(1500)).during(Duration.ofMillis(300)).until(ALWAYS_TRUE));

        assertThat(elapsed, greaterThanOrEqualTo(300L));
    }

    @Test(timeout = 2000L)
    public void userSetAtMostLargerThanPollDelayPlusDuringTimesOutAfterAtMostWhenNeverFulfilled() {
        long elapsed = measure(() -> {
            try {
                await().atMost(Duration.ofMillis(600)).during(Duration.ofMillis(200)).until(() -> false);
                fail("Expected ConditionTimeoutException");
            } catch (ConditionTimeoutException e) {
                assertThat(e.getMessage(), endsWith("was not fulfilled within 600 milliseconds."));
                assertThat(e.getMessage(), not(containsString("did not remain fulfilled")));
            }
        });

        assertThat(elapsed, greaterThanOrEqualTo(600L));
    }

    // Matched but not held long enough

    @Test(timeout = 2000L)
    public void timeoutMessageSaysTheConditionWasFulfilledButDidNotHoldLongEnough() {
        long start = System.currentTimeMillis();
        try {
            await().atMost(Duration.ofMillis(800)).during(Duration.ofMillis(500))
                    .until(() -> System.currentTimeMillis() - start > 400L);
            fail("Expected ConditionTimeoutException");
        } catch (ConditionTimeoutException e) {
            assertEquals("Condition was fulfilled but did not remain fulfilled for 500 milliseconds within 800 milliseconds.", e.getMessage());
        }
    }

    @Test(timeout = 2000L)
    public void timeoutMessageWithAliasAndHamcrestMatcherSaysTheConditionWasFulfilledButDidNotHoldLongEnough() {
        long start = System.currentTimeMillis();
        AtomicInteger value = new AtomicInteger();
        try {
            await("counter").atMost(Duration.ofMillis(800)).during(Duration.ofMillis(500))
                    .until(() -> System.currentTimeMillis() - start > 400L ? value.get() : -1, equalTo(0));
            fail("Expected ConditionTimeoutException");
        } catch (ConditionTimeoutException e) {
            assertEquals("Condition with alias 'counter' was fulfilled but did not remain fulfilled for 500 milliseconds within 800 milliseconds.",
                    e.getMessage());
        }
    }

    private static long measure(Runnable runnable) {
        long start = System.nanoTime();
        runnable.run();
        return (System.nanoTime() - start) / 1_000_000L;
    }
}
