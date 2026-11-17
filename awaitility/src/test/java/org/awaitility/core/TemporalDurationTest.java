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
import java.time.temporal.ChronoUnit;

import static org.awaitility.core.TemporalDuration.formatAsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TemporalDurationTest {

    @Test
    public void dayScaleDurationsRenderAsDays() {
        assertThat(formatAsString(Duration.ofDays(365)), equalTo("365 days"));
        assertThat(formatAsString(Duration.ofDays(366)), equalTo("366 days"));
        assertThat(formatAsString(Duration.ofDays(31)), equalTo("31 days"));
        assertThat(formatAsString(Duration.ofDays(59)), equalTo("59 days"));
    }

    @Test
    public void chronoUnitYearsDurationRendersFromTheDurationItself() {
        assertThat(formatAsString(ChronoUnit.YEARS.getDuration()), equalTo("365 days 5 hours 49 minutes 12 seconds"));
    }

    @Test
    public void singularUnitsAreNotPluralized() {
        assertThat(formatAsString(Duration.ofDays(1)), equalTo("1 day"));
        assertThat(formatAsString(Duration.ofHours(1)), equalTo("1 hour"));
        assertThat(formatAsString(Duration.ofMinutes(1)), equalTo("1 minute"));
        assertThat(formatAsString(Duration.ofSeconds(1)), equalTo("1 second"));
        assertThat(formatAsString(Duration.ofMillis(1)), equalTo("1 millisecond"));
    }

    @Test
    public void fieldsAreCombinedWithSingleSpaces() {
        assertThat(formatAsString(Duration.ofSeconds(90)), equalTo("1 minute 30 seconds"));
        assertThat(formatAsString(Duration.ofDays(2).plusHours(3).plusMinutes(4).plusSeconds(5).plusMillis(6)),
                equalTo("2 days 3 hours 4 minutes 5 seconds 6 milliseconds"));
    }

    @Test
    public void subMillisecondDurationsRenderAsNanoseconds() {
        assertThat(formatAsString(Duration.ofNanos(500)), equalTo("500 nanoseconds"));
        assertThat(formatAsString(Duration.ofNanos(1)), equalTo("1 nanosecond"));
    }
}
