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

import org.junit.Test;

import java.time.Duration;
import java.time.Instant;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class InstantLowerBoundTest {
    @Test
    public void rejectsSuccessBeforeTheInstant() {
        ConditionTimeoutException failure = assertThrows(ConditionTimeoutException.class, () -> await()
                .pollDelay(Duration.ZERO).notBefore(Instant.now().plusSeconds(5))
                .atMost(Duration.ofSeconds(10)).until(() -> true));
        assertTrue(failure.getMessage().contains("earlier than expected minimum"));
    }

    @Test
    public void resolvesTheInstantWhenTheWaitStartsRatherThanWhenConfigured() throws InterruptedException {
        ConditionFactory factory = await().pollDelay(Duration.ZERO)
                .notBefore(Instant.now().plusMillis(100)).atMost(Duration.ofSeconds(1));
        Thread.sleep(150);
        factory.until(() -> true);
    }

    @Test
    public void chainingDuringAndAtMostPreservesTheInstant() {
        ConditionTimeoutException failure = assertThrows(ConditionTimeoutException.class, () -> await()
                .pollDelay(Duration.ZERO).notBefore(Instant.now().plusSeconds(5))
                .during(Duration.ofMillis(20)).atMost(Duration.ofSeconds(1)).until(() -> true));
        assertTrue(failure.getMessage().contains("earlier than expected minimum"));
    }

    @Test
    public void durationMinimumReplacesInstantMinimum() {
        await().pollDelay(Duration.ZERO).notBefore(Instant.now().plusSeconds(5))
                .atLeast(Duration.ZERO).until(() -> true);
    }

    @Test
    public void instantMinimumPreservesExistingMaximumAndHoldDuration() {
        org.awaitility.constraint.WaitConstraint constraint = await().atMost(Duration.ofSeconds(2))
                .during(Duration.ofMillis(50)).notBefore(Instant.now().minusSeconds(1))
                .getSettings().getTimeoutConstraint();
        assertEquals(Duration.ofSeconds(2), constraint.getMaxWaitTime());
        assertEquals(Duration.ofMillis(50), constraint.getHoldPredicateTime());
        assertEquals(Duration.ZERO, constraint.getMinWaitTime());
    }

    @Test
    public void rejectsNullInstant() {
        assertThrows(NullPointerException.class, () -> await().notBefore(null));
    }
}
