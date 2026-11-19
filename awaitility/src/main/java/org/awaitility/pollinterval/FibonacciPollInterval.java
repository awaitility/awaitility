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

package org.awaitility.pollinterval;

import org.awaitility.core.DurationFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Generates a non-linear poll interval based on the fibonacci sequence: [0, 1, 1, 2, 3, 5, 8, 13, ..]
 */
public class FibonacciPollInterval implements PollInterval {

    private static final int DEFAULT_OFFSET = 0;

    /**
     * The largest value for which the fibonacci number fits in an {@code int} (fib(46) = 1836311903).
     */
    private static final int MAX_INT_FIBONACCI_VALUE = 46;

    private final TimeUnit unit;
    private final int offset;

    /**
     * Create an instance of the {@link FibonacciPollInterval} with the time unit of {@link java.util.concurrent.TimeUnit#MILLISECONDS}.
     */
    public FibonacciPollInterval() {
        this(TimeUnit.MILLISECONDS);
    }

    /**
     * Create an instance of the {@link FibonacciPollInterval} with the supplied time unit starting with offset equal to {@value #DEFAULT_OFFSET}.
     *
     * @param unit The time unit
     * @see FibonacciPollInterval#FibonacciPollInterval(int, TimeUnit)
     */
    public FibonacciPollInterval(TimeUnit unit) {
        this(DEFAULT_OFFSET, unit);
    }

    /**
     * Create an instance of the {@link FibonacciPollInterval} with a supplied time unit.
     *
     * @param offset   The fibonacci offset. For example if offset is 5 and poll count is 1 then the returned duration will be 8 (since  <code>fib(6)</code> is equal to 8).
     *                 Default offset is {@value #DEFAULT_OFFSET}. You can set the offset to <tt>-1</tt> if you want the first value to be <code>fib(0)</code> (i.e. pollCount - offset).
     *                 By default the first value will be <code>fib(1)</code>.
     * @param unit The time unit
     */
    public FibonacciPollInterval(int offset, TimeUnit unit) {
        if (offset < -1) {
            throw new IllegalArgumentException("Offset must be greater than or equal to -1");
        }
        if (unit == null) {
            throw new IllegalArgumentException("Chrono unit cannot be null");
        }
        this.offset = offset;
        this.unit = unit;
    }

    /**
     * Generate the next duration.
     * <p>
     * For values up to 46 (offset + poll count) the duration is {@link #fibonacci(int)}, so subclasses that override
     * that method keep working. Above 46 the fibonacci number no longer fits in an {@code int}, so it is computed
     * with 64-bit arithmetic instead. The resulting duration is capped at the largest duration that can be expressed
     * in nanoseconds, so it never overflows.
     * </p>
     *
     * @param pollCount        The number of times the condition has been polled (evaluated)
     * @param previousDuration The duration of the previously returned poll interval
     * @return The next duration in the fibonacci sequence.
     */
    @Override
    public Duration next(int pollCount, Duration previousDuration) {
        final int value = offset + pollCount;
        final long fibonacci = value <= MAX_INT_FIBONACCI_VALUE ? fibonacci(value) : saturatedFibonacci(value);
        return toDuration(fibonacci);
    }

    /**
     * Syntactic sugar for <code>new FibonacciPollInterval()</code>
     *
     * @return A new instance of {@link FibonacciPollInterval}.
     */
    public static FibonacciPollInterval fibonacci() {
        return new FibonacciPollInterval();
    }

    /**
     * Syntactic sugar for <code>new FibonacciPollInterval(unit)</code>
     *
     * @param unit The time unit
     * @return A new instance of {@link FibonacciPollInterval}.
     */
    public static FibonacciPollInterval fibonacci(TimeUnit unit) {
        return new FibonacciPollInterval(unit);
    }


    /**
     * Syntactic sugar for <code>new FibonacciPollInterval(offset, unit)</code>
     *
     * @param offset   The fibonacci offset. For example if offset is 5 and poll count is 1 then the returned duration will be 8 (since  <code>fib(6)</code> is equal to 8).
     *                 Default offset is {@value #DEFAULT_OFFSET}.
     * @param unit The time unit
     * @return A new instance of {@link FibonacciPollInterval}.
     */
    public static FibonacciPollInterval fibonacci(int offset, TimeUnit unit) {
        return new FibonacciPollInterval(offset, unit);
    }

    /**
     * Syntactic sugar
     *
     * @return The same of instance of {@link FibonacciPollInterval}
     */
    public FibonacciPollInterval with() {
        return this;
    }

    /**
     * Syntactic sugar
     *
     * @return The same of instance of {@link FibonacciPollInterval}
     */
    public FibonacciPollInterval and() {
        return this;
    }

    /**
     * Create a new {@link FibonacciPollInterval} with the same offset but with a different time unit
     *
     * @return The same of instance of {@link FibonacciPollInterval}
     */
    public FibonacciPollInterval unit(TimeUnit unit) {
        return new FibonacciPollInterval(offset, unit);
    }

    /**
     * Create a new {@link FibonacciPollInterval} with the same time unit but with a different offset
     *
     * @return The same of instance of {@link FibonacciPollInterval}
     */
    public FibonacciPollInterval offset(int offset) {
        return new FibonacciPollInterval(offset, unit);
    }

    /**
     * Generate the value of the fibonacci sequence for <code>number</code>.
     *
     * @param value The value
     * @return the fibonacci number
     */
    protected int fibonacci(int value) {
        return fib(value, 1, 0);
    }

    // Tail recursive implementation of fibonacci
    private int fib(int value, int current, int previous) {
        if (value == 0) {
            return previous;
        } else if (value == 1) {
            return current;
        }
        return fib(value - 1, current + previous, current);
    }

    /**
     * Generate the value of the fibonacci sequence for <code>value</code> using 64-bit arithmetic.
     * The result saturates at {@link Long#MAX_VALUE} instead of overflowing. {@link #fibonacci(int)} is
     * left unchanged: it only supports values whose fibonacci number fits in an {@code int} (up to 46).
     *
     * @param value The value
     * @return the fibonacci number, saturated at {@link Long#MAX_VALUE}
     */
    private static long saturatedFibonacci(int value) {
        if (value <= 0) {
            return 0;
        }
        long previous = 0;
        long current = 1;
        for (int i = 1; i < value; i++) {
            if (current > Long.MAX_VALUE - previous) {
                return Long.MAX_VALUE;
            }
            long next = current + previous;
            previous = current;
            current = next;
        }
        return current;
    }

    /**
     * Convert the amount to a {@link Duration} in the configured unit, capped at the largest amount of this unit that
     * can be converted to nanoseconds (which is what {@link DurationFactory#of(long, TimeUnit)} requires).
     */
    private Duration toDuration(long amount) {
        final long maxAmount = unit.convert(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        return DurationFactory.of(Math.min(amount, maxAmount), unit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FibonacciPollInterval)) return false;

        FibonacciPollInterval that = (FibonacciPollInterval) o;

        return offset == that.offset && unit == that.unit;
    }

    @Override
    public int hashCode() {
        int result = unit.hashCode();
        result = 31 * result + offset;
        return result;
    }

    @Override
    public String toString() {
        return "FibonacciPollInterval{" +
                "offset=" + offset +
                ", unit=" + unit +
                '}';
    }
}