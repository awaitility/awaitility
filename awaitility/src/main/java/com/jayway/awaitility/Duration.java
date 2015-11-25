/*
 * Copyright 2010 the original author or authors.
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
package com.jayway.awaitility;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * <p>Duration class.</p>
 */
public class Duration {

    /**
     * Constant <code>FOREVER</code>
     */
    public static final Duration FOREVER = new Duration();

    /**
     * Constant <code>ZERO</code>
     */
    public static final Duration ZERO = new Duration(0, MILLISECONDS);

    /**
     * Constant <code>ONE_HUNDRED_MILLISECONDS</code>
     */
    public static final Duration ONE_HUNDRED_MILLISECONDS = new Duration(100, MILLISECONDS);
    /**
     * Constant <code>TWO_HUNDRED_MILLISECONDS</code>
     */
    public static final Duration TWO_HUNDRED_MILLISECONDS = new Duration(200, MILLISECONDS);
    /**
     * Constant <code>FIVE_HUNDRED_MILLISECONDS</code>
     */
    public static final Duration FIVE_HUNDRED_MILLISECONDS = new Duration(500, MILLISECONDS);
    /**
     * Constant <code>ONE_SECOND</code>
     */
    public static final Duration ONE_SECOND = new Duration(1, SECONDS);
    /**
     * Constant <code>TWO_SECONDS</code>
     */
    public static final Duration TWO_SECONDS = new Duration(2, SECONDS);
    /**
     * Constant <code>FIVE_SECONDS</code>
     */
    public static final Duration FIVE_SECONDS = new Duration(5, SECONDS);
    /**
     * Constant <code>TEN_SECONDS</code>
     */
    public static final Duration TEN_SECONDS = new Duration(10, SECONDS);
    /**
     * Constant <code>ONE_MINUTE</code>
     */
    public static final Duration ONE_MINUTE = new Duration(60, SECONDS);
    /**
     * Constant <code>TWO_MINUTES</code>
     */
    public static final Duration TWO_MINUTES = new Duration(120, SECONDS);
    /**
     * Constant <code>FIVE_MINUTES</code>
     */
    public static final Duration FIVE_MINUTES = new Duration(300, SECONDS);
    /**
     * Constant <code>TEN_MINUTES</code>
     */
    public static final Duration TEN_MINUTES = new Duration(600, SECONDS);
    /**
     * Constant <code>SAME_AS_POLL_INTERVAL</code>
     */
    public static final Duration SAME_AS_POLL_INTERVAL = new Duration();
    private static final int NONE = -1;

    private final long value;
    private final TimeUnit unit;

    private Duration() {
        this.value = NONE;
        this.unit = null;
    }

    /**
     * <p>Constructor for Duration.</p>
     *
     * @param value a long.
     * @param unit  a {@link java.util.concurrent.TimeUnit} object.
     */
    public Duration(long value, TimeUnit unit) {
        if (value < 0) {
            throw new IllegalArgumentException("value must be >= 0, was " + value);
        }
        if (unit == null) {
            throw new IllegalArgumentException("TimeUnit cannot be null");
        }
        this.value = value;
        this.unit = unit;
    }

    /**
     * <p>getTimeUnit.</p>
     *
     * @return a {@link java.util.concurrent.TimeUnit} object.
     */
    public TimeUnit getTimeUnit() {
        return unit;
    }

    /**
     * <p>getTimeUnitAsString.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTimeUnitAsString() {
        return unit == null ? "<not defined>" : unit.toString().toLowerCase();
    }

    /**
     * <p>isForever.</p>
     *
     * @return a boolean.
     */
    public boolean isForever() {
        return unit == null && value == NONE;
    }

    /**
     * <p>isZero</p>
     *
     * @return a boolean.
     */
    public boolean isZero() {
        return this.equals(ZERO);
    }

    /**
     * <p>Getter for the field <code>value</code>.</p>
     *
     * @return a long.
     */
    public long getValue() {
        return value;
    }

    /**
     * <p>getValueInMS.</p>
     *
     * @return a long.
     */
    public long getValueInMS() {
        if (value == NONE) {
            return value;
        }
        return MILLISECONDS.convert(value, unit);
    }

    /**
     * Add a duration (with the same time unit as the current duration)
     *
     * @param duration The duration to add
     * @return A new duration
     */
    public Duration plus(long duration) {
        if (SAME_AS_POLL_INTERVAL == this) {
            // TODO Fix this
            return new Duration(duration, MILLISECONDS);
        }
        return new Duration(value + duration, unit);
    }

    /**
     * Multiply this duration with the given duration
     *
     * @param duration The duration
     * @return A new duration
     */
    public Duration multiply(long duration) {
        if (SAME_AS_POLL_INTERVAL == this) {
            // TODO Fix this
            return new Duration(duration, MILLISECONDS);
        }
        return new Duration((value == 0 ? 1 : value) * duration, unit);
    }

    /**
     * Add a duration with the given time unit. For example:
     * <pre>
     * new Duration(2, SECONDS).plus(4, MILLISECONDS)
     * </pre>
     * this will return a Duration with 2004 {@link TimeUnit#MILLISECONDS}.
     *
     * @param duration The duration to add
     * @param timeUnit The time unit to add
     * @return A new duration
     */
    public Duration plus(long duration, TimeUnit timeUnit) {
        // TODO Fix NPE
        int min = Math.min(timeUnit.ordinal(), unit.ordinal());
        int max = Math.max(timeUnit.ordinal(), unit.ordinal());
        TimeUnit[] timeUnits = TimeUnit.values();
        TimeUnit sourceUnit = timeUnits[min];
        TimeUnit targetUnit = timeUnits[max];
        return new Duration(duration + targetUnit.convert(value, sourceUnit), targetUnit);
    }

    /**
     * Add a duration (with the same time unit as the current duration)
     *
     * @param duration The duration to add
     * @return A new duration
     */
    public Duration minus(long duration) {
        return new Duration(value - duration, unit);
    }

    /**
     * Add a duration with the given time unit. For example:
     * <pre>
     * new Duration(2, SECONDS).add(4, MILLISECONDS)
     * </pre>
     * this will return a Duration with 2004 {@link TimeUnit#MILLISECONDS}.
     *
     * @param duration The duration to add
     * @param timeUnit The time unit to add
     * @return A new duration
     */
    public Duration minus(long duration, TimeUnit timeUnit) {
        // TODO Fixme
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Duration duration = (Duration) o;

        return getValueInMS() == duration.getValueInMS();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = (int) (value ^ (value >>> 32));
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        return result;
    }
}
