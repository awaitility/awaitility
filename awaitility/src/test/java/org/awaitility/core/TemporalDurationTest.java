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
import java.util.Random;

import static org.awaitility.core.TemporalDuration.formatAsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

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

    @Test
    public void renderedFieldsSumUpToTheDurationTruncatedToMillisForDurationsBetweenOneDayAndOneYear() {
        Duration oneYear = ChronoUnit.YEARS.getDuration();
        Duration[] durations = {
                Duration.ofDays(1),
                Duration.ofDays(1).plusNanos(1),
                Duration.ofDays(1).plusMillis(1),
                Duration.ofDays(31).plusHours(23).plusMinutes(59).plusSeconds(59).plusMillis(999).plusNanos(999_999),
                Duration.ofDays(59).plusHours(1),
                Duration.ofDays(364).plusSeconds(1),
                Duration.ofDays(365),
                oneYear.minusNanos(1)
        };
        for (Duration duration : durations) {
            assertRenderedFieldsSumUpToTheDuration(duration);
        }

        Random random = new Random(318);
        long range = oneYear.minusDays(1).toNanos();
        for (int i = 0; i < 10_000; i++) {
            long nanos = (long) (random.nextDouble() * range);
            assertRenderedFieldsSumUpToTheDuration(Duration.ofDays(1).plusNanos(nanos));
        }
    }

    @Test
    public void veryLongDurationsCanBeFormatted() {
        assertThat(formatAsString(ChronoUnit.FOREVER.getDuration()), equalTo("106751991167300 days 15 hours 30 minutes 7 seconds 999 milliseconds"));
        assertThat(formatAsString(Duration.ofSeconds(Long.MAX_VALUE)), equalTo("106751991167300 days 15 hours 30 minutes 7 seconds"));
        assertThat(formatAsString(Duration.ofMillis(Long.MAX_VALUE)), startsWith("106751991167 days"));
        assertThat(formatAsString(Duration.ofNanos(Long.MAX_VALUE)), equalTo("106751 days 23 hours 47 minutes 16 seconds 854 milliseconds"));
    }

    @Test
    public void durationsUnderADayKeepTheirPluralTextForValuesOtherThanOne() {
        assertThat(formatAsString(Duration.ofMillis(120)), equalTo("120 milliseconds"));
        assertThat(formatAsString(Duration.ofSeconds(2)), equalTo("2 seconds"));
        assertThat(formatAsString(Duration.ofMillis(59_999)), equalTo("59 seconds 999 milliseconds"));
        assertThat(formatAsString(Duration.ofMillis(1500)), equalTo("1 second 500 milliseconds"));
        assertThat(formatAsString(Duration.ofDays(1).minusMillis(1)), equalTo("23 hours 59 minutes 59 seconds 999 milliseconds"));
        assertThat(formatAsString(Duration.ofNanos(999_999)), equalTo("999999 nanoseconds"));
    }

    private static void assertRenderedFieldsSumUpToTheDuration(Duration duration) {
        String rendered = formatAsString(duration);
        String[] tokens = rendered.split(" ");
        assertThat(rendered, tokens.length % 2, equalTo(0));
        Duration sum = Duration.ZERO;
        for (int i = 0; i < tokens.length; i += 2) {
            long value = Long.parseLong(tokens[i]);
            sum = sum.plus(value, unitOf(tokens[i + 1], rendered));
        }
        assertThat(rendered, sum, equalTo(duration.minusNanos(duration.getNano() % 1_000_000)));
    }

    private static ChronoUnit unitOf(String unit, String rendered) {
        switch (unit) {
            case "day":
            case "days":
                return ChronoUnit.DAYS;
            case "hour":
            case "hours":
                return ChronoUnit.HOURS;
            case "minute":
            case "minutes":
                return ChronoUnit.MINUTES;
            case "second":
            case "seconds":
                return ChronoUnit.SECONDS;
            case "millisecond":
            case "milliseconds":
                return ChronoUnit.MILLIS;
            default:
                throw new AssertionError("Unexpected unit '" + unit + "' in '" + rendered + "'");
        }
    }
}
