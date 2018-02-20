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
package org.awaitility;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

/**
 * <p>Duration class.</p>
 */
@SuppressWarnings("Duplicates")
public class Duration implements Comparable<Duration> {

    /**
     * Constant <code>FOREVER</code>
     */
    public static final Duration FOREVER = new Duration(Long.MAX_VALUE, DAYS);

    /**
     * Constant <code>ZERO</code>
     */
    public static final Duration ZERO = new Duration(0, MILLISECONDS);

    /**
     * Constant <code>ONE_MILLISECOND</code>
     *
     * @since 1.7.0
     */
    public static final Duration ONE_MILLISECOND = new Duration(1, MILLISECONDS);

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
        return this == FOREVER;
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
     * Get the value in milliseconds.
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
     * Get the value in nanoseconds.
     *
     * @return a long.
     */
    public long getValueInNS() {
        if (value == NONE) {
            return value;
        }
        return NANOSECONDS.convert(value, unit);
    }

    /**
     * Add a amount (with the same time unit as the current duration)
     *
     * @param amount The amount to add
     * @return A new amount
     */
    public Duration plus(long amount) {
        return new Plus().apply(this, unit == null ? FOREVER : new Duration(amount, unit));
    }

    /**
     * Add a amount with the given time unit. For example:
     * <pre>
     * new Duration(2, SECONDS).plus(4, MILLISECONDS)
     * </pre>
     * will return a Duration of 2004 {@link TimeUnit#MILLISECONDS}.
     * <p>
     * <pre>
     * new Duration(2, SECONDS).plus(1, MINUTES)
     * </pre>
     * will return a Duration of 62 {@link TimeUnit#SECONDS}.
     *
     * @param amount   The amount to add
     * @param timeUnit The time unit to add
     * @return A new amount
     */
    public Duration plus(long amount, TimeUnit timeUnit) {
        if (timeUnit == null) {
            throw new IllegalArgumentException("Time unit cannot be null");
        }

        return new Plus().apply(this, new Duration(amount, timeUnit));
    }

    /**
     * Add a duration with the given time unit. For example:
     * <pre>
     * new Duration(2, SECONDS).plus(Duration.FIVE_HUNDRED_MILLISECONDS)
     * </pre>
     * will return a Duration of 2500 {@link TimeUnit#MILLISECONDS}.
     * <p>
     *
     * @param duration The duration to add
     * @return A new duration
     */
    public Duration plus(Duration duration) {
        return new Plus().apply(this, duration);
    }

    /**
     * Multiply this amount with the given amount
     *
     * @param amount The amount
     * @return A new amount
     */
    public Duration multiply(long amount) {
        return new Multiply().apply(this, unit == null ? FOREVER : new Duration(amount, unit));
    }


    /**
     * Divide this duration amount with the given amount
     *
     * @param amount The amount
     * @return A new amount
     */
    public Duration divide(long amount) {
        return new Divide().apply(this, unit == null ? FOREVER : new Duration(amount, unit));
    }

    /**
     * Subtract an amount (with the same time unit as the current amount)
     *
     * @param amount The amount to add
     * @return A new amount
     */
    public Duration minus(long amount) {
        return new Minus().apply(this, unit == null ? FOREVER : new Duration(amount, unit));
    }

    /**
     * Subtract an amount with the given time unit. For example:
     * <pre>
     * new Duration(2, SECONDS).minus(4, MILLISECONDS)
     * </pre>
     * will return a Duration of 1996 {@link TimeUnit#MILLISECONDS}.
     * <p>
     * <pre>
     * new Duration(2, MINUTES).minus(1, SECONDS)
     * </pre>
     * will return a Duration of 119 {@link TimeUnit#SECONDS}.
     *
     * @param amount   The amount to add
     * @param timeUnit The time unit to add
     * @return A new amount
     */
    public Duration minus(long amount, TimeUnit timeUnit) {
        if (timeUnit == null) {
            throw new IllegalArgumentException("Time unit cannot be null");
        }

        return new Minus().apply(this, new Duration(amount, timeUnit));
    }

    /**
     * Add a duration with the given time unit. For example:
     * <pre>
     * new Duration(2, SECONDS).plus(Duration.FIVE_HUNDRED_MILLISECONDS)
     * </pre>
     * will return a Duration of 2500 {@link TimeUnit#MILLISECONDS}.
     * <p>
     *
     * @param duration The duration to add
     * @return A new duration
     */
    public Duration minus(Duration duration) {
        return new Minus().apply(this, duration);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Duration{" +
                "unit=" + unit +
                ", value=" + value +
                '}';
    }

    public int compareTo(Duration other) {
        if (other == null) return 1;
        long x = getValueInMS();
        long y = other.getValueInMS();
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    private static abstract class BiFunction {
        public final Duration apply(Duration lhs, Duration rhs) {
            if (lhs == null || rhs == null) {
                throw new IllegalArgumentException("Duration cannot be null");
            }

            Duration specialDuration = handleSpecialCases(lhs, rhs);
            if (specialDuration != null) {
                return specialDuration;
            }

            final Duration newDuration;
            if (lhs.getTimeUnit().ordinal() > rhs.getTimeUnit().ordinal()) {
                long lhsConverted = rhs.getTimeUnit().convert(lhs.getValue(), lhs.getTimeUnit());
                newDuration = new Duration(apply(lhsConverted, rhs.getValue()), rhs.getTimeUnit());
            } else if (lhs.getTimeUnit().ordinal() < rhs.getTimeUnit().ordinal()) {
                long rhsConverted = lhs.getTimeUnit().convert(rhs.getValue(), rhs.getTimeUnit());
                newDuration = new Duration(apply(lhs.getValue(), rhsConverted), lhs.getTimeUnit());
            } else {
                // Same time unit
                newDuration = new Duration(apply(lhs.getValue(), rhs.getValue()), lhs.getTimeUnit());
            }

            return newDuration;
        }

        protected abstract Duration handleSpecialCases(Duration lhs, Duration rhs);

        abstract long apply(long operand1, long operand2);
    }

    private static class Plus extends BiFunction {
        protected Duration handleSpecialCases(Duration lhs, Duration rhs) {
            if (ZERO.equals(rhs)) {
                return lhs;
            } else if (ZERO.equals(lhs)) {
                return rhs;
            } else if (lhs == FOREVER || rhs == FOREVER) {
                return FOREVER;
            }
            return null;
        }

        long apply(long operand1, long operand2) {
            return operand1 + operand2;
        }
    }

    private static class Minus extends BiFunction {
        protected Duration handleSpecialCases(Duration lhs, Duration rhs) {
            if (!lhs.isZero() && rhs.isZero()) {
                return lhs;
            } else if (lhs == FOREVER) {
                return FOREVER;
            } else if (rhs == FOREVER) {
                return ZERO;
            } else if (FOREVER.equals(rhs)) {
                return ZERO;
            }
            return null;
        }

        long apply(long operand1, long operand2) {
            return operand1 - operand2;
        }
    }

    private static class Multiply extends BiFunction {
        @Override
        protected Duration handleSpecialCases(Duration lhs, Duration rhs) {
            if (lhs.isZero() || rhs.isZero()) {
                return ZERO;
            } else if (lhs == FOREVER || rhs == FOREVER) {
                return FOREVER;
            }
            return null;
        }

        long apply(long operand1, long operand2) {
            return operand1 * operand2;
        }
    }

    private static class Divide extends BiFunction {
        protected Duration handleSpecialCases(Duration lhs, Duration rhs) {
            if (lhs == FOREVER) {
                return FOREVER;
            } else if (rhs == FOREVER) {
                throw new IllegalArgumentException("Cannot divide by infinity");
            } else if (ZERO.equals(lhs)) {
                return ZERO;
            }
            return null;
        }

        long apply(long operand1, long operand2) {
            return operand1 / operand2;
        }
    }
}
