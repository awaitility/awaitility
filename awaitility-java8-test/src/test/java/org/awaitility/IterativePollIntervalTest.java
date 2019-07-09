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

import org.awaitility.pollinterval.IterativePollInterval;
import org.junit.Test;

import java.time.Duration;

import static org.awaitility.Durations.FIVE_HUNDRED_MILLISECONDS;
import static org.awaitility.Durations.ONE_SECOND;
import static org.awaitility.pollinterval.IterativePollInterval.iterative;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class IterativePollIntervalTest {

    @Test public void
    iterative_poll_interval_allows_specifying_start_duration() {
        // Given
        IterativePollInterval pollInterval = new IterativePollInterval(prev -> prev.multipliedBy(2));

        // When
        Duration duration = pollInterval.next(1, Duration.ZERO);

        // Then
        assertThat(duration.toMillis(), is(2L));
    }

    @Test public void
    iterative_poll_interval_use_no_start_value_by_default() {
        // Given
        IterativePollInterval pollInterval = new IterativePollInterval(prev -> prev.multipliedBy(2));

        // When
        Duration duration = pollInterval.next(1, Duration.ofSeconds(1));

        // Then
        assertThat(duration.toMillis(), is(2000L));
    }

    @Test public void
    iterative_poll_interval_allows_specifying_start_duration_through_dsl() {
        // Given
        IterativePollInterval pollInterval = iterative(prev -> prev.multipliedBy(2)).with().startDuration(FIVE_HUNDRED_MILLISECONDS);

        // When
        Duration duration = pollInterval.next(1, ONE_SECOND);

        // Then
        assertThat(duration.toMillis(), is(1000L));
    }
}