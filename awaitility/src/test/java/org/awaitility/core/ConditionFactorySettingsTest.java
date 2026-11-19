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
import org.awaitility.constraint.AtMostWaitConstraint;
import org.awaitility.constraint.IntervalWaitConstraint;
import org.awaitility.constraint.WaitConstraint;
import org.awaitility.pollinterval.FixedPollInterval;
import org.junit.After;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.waitAtMost;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Verifies which entry points record that atMost was set by the user, and that routing the DSL through
 * {@link ConditionFactorySettings} keeps the resulting settings unchanged.
 */
public class ConditionFactorySettingsTest {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    @After
    public void resetAwaitility() {
        Awaitility.reset();
    }

    // Not set by the user

    @Test
    public void defaultAwaitDoesNotSetAtMost() {
        assertAtMostNotSetByUser(await(), DEFAULT_TIMEOUT);
        assertAtMostNotSetByUser(await("alias"), DEFAULT_TIMEOUT);
        assertAtMostNotSetByUser(Awaitility.with(), DEFAULT_TIMEOUT);
        assertAtMostNotSetByUser(Awaitility.given(), DEFAULT_TIMEOUT);
        assertAtMostNotSetByUser(Awaitility.catchUncaughtExceptions(), DEFAULT_TIMEOUT);
        assertAtMostNotSetByUser(Awaitility.dontCatchUncaughtExceptions(), DEFAULT_TIMEOUT);
    }

    @Test
    public void setDefaultTimeoutWithDurationDoesNotSetAtMost() {
        Awaitility.setDefaultTimeout(Duration.ofMillis(1234));

        assertAtMostNotSetByUser(await(), Duration.ofMillis(1234));
    }

    @Test
    public void setDefaultTimeoutWithTimeUnitDoesNotSetAtMost() {
        Awaitility.setDefaultTimeout(1234, MILLISECONDS);

        assertAtMostNotSetByUser(await(), Duration.ofMillis(1234));
    }

    @Test
    public void defaultTimeoutSystemPropertyDoesNotSetAtMost() throws Exception {
        List<String> command = new ArrayList<>();
        command.add(Paths.get(System.getProperty("java.home"), "bin", "java").toString());
        command.add("-Dawaitility.defaultTimeout=PT0.123S");
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add(SystemPropertyScenario.class.getName());

        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        assertEquals("System property scenario failed:\n" + output, 0, process.waitFor());
    }

    @Test
    public void publicConstructorDoesNotSetAtMost() {
        ConditionFactory conditionFactory = new ConditionFactory(null, AtMostWaitConstraint.TEN_SECONDS.withMaxWaitTime(Duration.ofSeconds(5)),
                new FixedPollInterval(Duration.ofMillis(100)), null, true, new PredicateExceptionIgnorer(e -> false),
                null, null, null);

        assertAtMostNotSetByUser(conditionFactory, Duration.ofSeconds(5));
    }

    @Test
    public void publicConstructorDoesNotSetAtMostEvenWithForeverConstraint() {
        ConditionFactory conditionFactory = new ConditionFactory("alias", AtMostWaitConstraint.FOREVER,
                new FixedPollInterval(Duration.ofMillis(100)), null, true, new PredicateExceptionIgnorer(e -> false),
                null, null, null);

        assertAtMostNotSetByUser(conditionFactory, AtMostWaitConstraint.FOREVER.getMaxWaitTime());
    }

    @Test
    public void atLeastAndDuringDoNotSetAtMost() {
        assertAtMostNotSetByUser(await().atLeast(Duration.ofMillis(50)), DEFAULT_TIMEOUT);
        assertAtMostNotSetByUser(await().atLeast(50, MILLISECONDS), DEFAULT_TIMEOUT);
        assertAtMostNotSetByUser(await().during(Duration.ofMillis(50)), DEFAULT_TIMEOUT);
        assertAtMostNotSetByUser(await().during(50, MILLISECONDS), DEFAULT_TIMEOUT);
    }

    // Set by the user

    @Test
    public void atMostWithDurationSetsAtMost() {
        assertAtMostSetByUser(await().atMost(Duration.ofMillis(500)), Duration.ofMillis(500));
    }

    @Test
    public void atMostWithTimeUnitSetsAtMost() {
        assertAtMostSetByUser(await().atMost(500, MILLISECONDS), Duration.ofMillis(500));
    }

    @Test
    public void timeoutWithDurationSetsAtMost() {
        assertAtMostSetByUser(await().timeout(Duration.ofMillis(500)), Duration.ofMillis(500));
    }

    @Test
    public void timeoutWithTimeUnitSetsAtMost() {
        assertAtMostSetByUser(await().timeout(500, MILLISECONDS), Duration.ofMillis(500));
    }

    @Test
    public void betweenWithDurationsSetsAtMost() {
        ConditionFactory conditionFactory = await().between(Duration.ofMillis(100), Duration.ofMillis(500));

        assertAtMostSetByUser(conditionFactory, Duration.ofMillis(500));
        assertEquals(Duration.ofMillis(100), conditionFactory.getSettings().getTimeoutConstraint().getMinWaitTime());
    }

    @Test
    public void betweenWithTimeUnitsSetsAtMost() {
        ConditionFactory conditionFactory = await().between(100, MILLISECONDS, 500, MILLISECONDS);

        assertAtMostSetByUser(conditionFactory, Duration.ofMillis(500));
        assertEquals(Duration.ofMillis(100), conditionFactory.getSettings().getTimeoutConstraint().getMinWaitTime());
    }

    @Test
    public void foreverSetsAtMost() {
        ConditionFactory conditionFactory = await().forever();

        assertAtMostSetByUser(conditionFactory, AtMostWaitConstraint.FOREVER.getMaxWaitTime());
        WaitConstraint timeoutConstraint = conditionFactory.getSettings().getTimeoutConstraint();
        assertTrue(timeoutConstraint instanceof AtMostWaitConstraint);
        assertEquals(Duration.ZERO, timeoutConstraint.getMinWaitTime());
        assertEquals(Duration.ZERO, timeoutConstraint.getHoldPredicateTime());
    }

    @Test
    public void foreverKeepsAtLeastAndDuringSetEarlier() {
        ConditionFactory conditionFactory = await().atLeast(Duration.ofMillis(100)).during(Duration.ofMillis(200)).forever();

        assertAtMostSetByUser(conditionFactory, AtMostWaitConstraint.FOREVER.getMaxWaitTime());
        WaitConstraint timeoutConstraint = conditionFactory.getSettings().getTimeoutConstraint();
        assertEquals(Duration.ofMillis(100), timeoutConstraint.getMinWaitTime());
        assertEquals(Duration.ofMillis(200), timeoutConstraint.getHoldPredicateTime());
    }

    @Test
    public void waitAtMostWithDurationSetsAtMost() {
        assertAtMostSetByUser(waitAtMost(Duration.ofMillis(500)), Duration.ofMillis(500));
    }

    @Test
    public void waitAtMostWithTimeUnitSetsAtMost() {
        assertAtMostSetByUser(waitAtMost(500, MILLISECONDS), Duration.ofMillis(500));
    }

    @Test
    public void userSetAtMostSurvivesOtherSettings() {
        ConditionFactory conditionFactory = await().atMost(2, SECONDS)
                .atLeast(Duration.ofMillis(10))
                .during(Duration.ofMillis(20))
                .alias("alias")
                .pollInterval(Duration.ofMillis(30))
                .pollDelay(Duration.ofMillis(40))
                .ignoreExceptions()
                .logging()
                .failFast(() -> false)
                .pollInSameThread();

        assertTrue(conditionFactory.getSettings().isAtMostSetByUser());
        assertEquals(Duration.ofSeconds(2), conditionFactory.getSettings().getTimeoutConstraint().getMaxWaitTime());
    }

    // Unchanged settings

    @Test
    public void waitAtMostKeepsTheDefaultSettings() {
        Awaitility.setDefaultPollInterval(Duration.ofMillis(77));
        Awaitility.setDefaultPollDelay(Duration.ofMillis(66));
        Awaitility.doNotCatchUncaughtExceptionsByDefault();
        Awaitility.ignoreExceptionsByDefault();
        Awaitility.setDefaultConditionEvaluationListener(new ConditionEvaluationLogger());
        Awaitility.pollInSameThread();
        Awaitility.setDefaultFailFastCondition(() -> false);

        ConditionFactorySettings defaults = await().getSettings();
        ConditionFactorySettings settings = waitAtMost(Duration.ofMillis(500)).getSettings();

        assertNull(settings.getAlias());
        assertEquals(Duration.ofMillis(500), settings.getTimeoutConstraint().getMaxWaitTime());
        assertEquals(Duration.ZERO, settings.getTimeoutConstraint().getMinWaitTime());
        assertEquals(Duration.ZERO, settings.getTimeoutConstraint().getHoldPredicateTime());
        assertSame(defaults.getPollInterval(), settings.getPollInterval());
        assertEquals(Duration.ofMillis(66), settings.getPollDelay());
        assertFalse(settings.isCatchUncaughtExceptions());
        assertSame(defaults.getExceptionsIgnorer(), settings.getExceptionsIgnorer());
        assertSame(defaults.getConditionEvaluationListener(), settings.getConditionEvaluationListener());
        assertSame(defaults.getExecutorLifecycle(), settings.getExecutorLifecycle());
        assertSame(defaults.getFailFastCondition(), settings.getFailFastCondition());
    }

    @Test
    public void atLeastOnlyKeepsTheDefaultAtMost() {
        WaitConstraint waitConstraint = await().atLeast(Duration.ofMillis(100)).getSettings().getTimeoutConstraint();

        assertTrue(waitConstraint instanceof IntervalWaitConstraint);
        assertEquals(Duration.ofMillis(100), waitConstraint.getMinWaitTime());
        assertEquals(DEFAULT_TIMEOUT, waitConstraint.getMaxWaitTime());
    }

    @Test
    public void atMostAndAtLeastGiveTheSameIntervalInEitherOrder() {
        WaitConstraint atMostFirst = await().atMost(Duration.ofMillis(500)).atLeast(Duration.ofMillis(100)).getSettings().getTimeoutConstraint();
        WaitConstraint atLeastFirst = await().atLeast(Duration.ofMillis(100)).atMost(Duration.ofMillis(500)).getSettings().getTimeoutConstraint();

        for (WaitConstraint waitConstraint : new WaitConstraint[]{atMostFirst, atLeastFirst}) {
            assertTrue(waitConstraint instanceof IntervalWaitConstraint);
            assertEquals(Duration.ofMillis(100), waitConstraint.getMinWaitTime());
            assertEquals(Duration.ofMillis(500), waitConstraint.getMaxWaitTime());
        }
    }

    private static void assertAtMostSetByUser(ConditionFactory conditionFactory, Duration expectedMaxWaitTime) {
        assertTrue("atMost should be set by the user", conditionFactory.getSettings().isAtMostSetByUser());
        assertEquals(expectedMaxWaitTime, conditionFactory.getSettings().getTimeoutConstraint().getMaxWaitTime());
    }

    private static void assertAtMostNotSetByUser(ConditionFactory conditionFactory, Duration expectedMaxWaitTime) {
        assertFalse("atMost should not be set by the user", conditionFactory.getSettings().isAtMostSetByUser());
        assertEquals(expectedMaxWaitTime, conditionFactory.getSettings().getTimeoutConstraint().getMaxWaitTime());
    }

    /**
     * Run in a forked JVM with <code>-Dawaitility.defaultTimeout=PT0.123S</code> so that {@link Awaitility} reads the
     * system property when it is first loaded.
     */
    public static class SystemPropertyScenario {
        public static void main(String[] args) {
            ConditionFactorySettings settings = await().getSettings();
            if (!Duration.ofMillis(123).equals(settings.getTimeoutConstraint().getMaxWaitTime())) {
                throw new AssertionError("Expected default timeout 123 ms but was " + settings.getTimeoutConstraint().getMaxWaitTime());
            }
            if (settings.isAtMostSetByUser()) {
                throw new AssertionError("Expected atMost not to be set by the user for the system property default timeout");
            }
        }
    }
}
