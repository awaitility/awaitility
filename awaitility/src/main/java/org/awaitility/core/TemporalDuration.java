/*
 * Copyright 2019 the original author or authors.
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

import java.time.Duration;

class TemporalDuration {

    private final Duration duration;

    TemporalDuration(Duration duration) {
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        if (duration.compareTo(Duration.ofMillis(1)) < 0) {
            final long nanos = duration.toNanos();
            return nanos + (nanos == 1 ? " nanosecond" : " nanoseconds");
        }

        final StringBuilder result = new StringBuilder();
        appendField(result, duration.toDays(), "day", "days");
        appendField(result, duration.toHours() % 24, "hour", "hours");
        appendField(result, duration.toMinutes() % 60, "minute", "minutes");
        appendField(result, duration.getSeconds() % 60, "second", "seconds");
        appendField(result, duration.toMillis() % 1000, "millisecond", "milliseconds");

        return result.length() == 0 ? "0 milliseconds" : result.toString();
    }

    private static void appendField(StringBuilder result, long value, String singular, String plural) {
        if (value == 0) {
            return;
        }
        if (result.length() > 0) {
            result.append(' ');
        }
        result.append(value).append(' ').append(value == 1 ? singular : plural);
    }

    static String formatAsString(Duration duration) {
        return new TemporalDuration(duration).toString();
    }
}
