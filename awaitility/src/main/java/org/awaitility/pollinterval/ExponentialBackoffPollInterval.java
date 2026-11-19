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

import java.time.Duration;

import static org.awaitility.core.ForeverDuration.isForever;

/**
 * Doubles the poll interval from a starting duration up to a maximum duration.
 * For example, a 500 millisecond start and a 5 second maximum produce
 * 500, 1000, 2000, 4000, 5000, 5000, ... milliseconds.
 *
 * <pre>
 * await().pollInterval(exponential(Duration.ofMillis(500), Duration.ofSeconds(5))).until(...);
 * </pre>
 * The static factory is imported from this class. An explicitly configured poll delay
 * occurs before the first interval and does not change the starting duration.
 * Instances are immutable and can be shared between awaits.
 */
public final class ExponentialBackoffPollInterval implements PollInterval {

    private final Duration startDuration;
    private final Duration maxDuration;
    private final Duration halfMaximum;

    /**
     * Creates a bounded exponential poll interval.
     *
     * @param startDuration the first interval, positive and finite
     * @param maxDuration the upper bound, positive, finite and at least the start duration
     * @throws IllegalArgumentException if either duration is null, non-positive or forever,
     *                                  or the maximum is smaller than the start
     */
    public ExponentialBackoffPollInterval(Duration startDuration, Duration maxDuration) {
        validateDuration(startDuration, "Start duration");
        validateDuration(maxDuration, "Maximum duration");
        if (startDuration.compareTo(maxDuration) > 0) {
            throw new IllegalArgumentException("Maximum duration cannot be smaller than start duration");
        }
        this.startDuration = startDuration;
        this.maxDuration = maxDuration;
        this.halfMaximum = maxDuration.dividedBy(2);
    }

    private static void validateDuration(Duration duration, String name) {
        if (duration == null || duration.isZero() || duration.isNegative() || isForever(duration)) {
            throw new IllegalArgumentException(name + " must be positive and finite");
        }
    }

    /**
     * Returns the start duration on the first poll, then doubles the previous interval up to the maximum.
     * The cap is checked before multiplication to avoid arithmetic overflow.
     *
     * @param pollCount the number of times the condition has been polled, always positive
     * @param previousDuration the previous interval; ignored for the first poll, otherwise non-null and non-negative
     * @return the next interval
     * @throws IllegalArgumentException if a subsequent previous duration is null or negative
     */
    @Override
    public Duration next(int pollCount, Duration previousDuration) {
        if (pollCount == 1) {
            return startDuration;
        }
        if (previousDuration == null || previousDuration.isNegative()) {
            throw new IllegalArgumentException("Previous duration must be non-null and non-negative");
        }
        if (previousDuration.compareTo(halfMaximum) > 0) {
            return maxDuration;
        }
        return previousDuration.multipliedBy(2);
    }

    /**
     * Creates a bounded exponential poll interval.
     *
     * @param startDuration the first interval, positive and finite
     * @param maxDuration the positive, finite upper bound, at least the start duration
     * @return a new poll interval
     */
    public static ExponentialBackoffPollInterval exponential(Duration startDuration, Duration maxDuration) {
        return new ExponentialBackoffPollInterval(startDuration, maxDuration);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ExponentialBackoffPollInterval)) return false;
        ExponentialBackoffPollInterval that = (ExponentialBackoffPollInterval) other;
        return startDuration.equals(that.startDuration) && maxDuration.equals(that.maxDuration);
    }

    @Override
    public int hashCode() {
        return 31 * startDuration.hashCode() + maxDuration.hashCode();
    }

    @Override
    public String toString() {
        return "ExponentialBackoffPollInterval{" + "startDuration=" + startDuration + ", maxDuration=" + maxDuration + '}';
    }
}
