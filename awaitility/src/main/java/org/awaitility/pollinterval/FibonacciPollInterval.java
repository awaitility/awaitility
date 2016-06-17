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

import org.awaitility.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Generates a non-linear poll interval based on the fibonacci sequence: [0, 1, 1, 2, 3, 5, 8, 13, ..]
 */
public class FibonacciPollInterval implements PollInterval {

    private static final int DEFAULT_OFFSET = 0;

    private final TimeUnit timeUnit;
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
     * @param timeUnit The time unit
     * @see FibonacciPollInterval#FibonacciPollInterval(int, TimeUnit)
     */
    public FibonacciPollInterval(TimeUnit timeUnit) {
        this(DEFAULT_OFFSET, timeUnit);
    }

    /**
     * Create an instance of the {@link FibonacciPollInterval} with a supplied time unit.
     *
     * @param offset   The fibonacci offset. For example if offset is 5 and poll count is 1 then the returned duration will be 8 (since  <code>fib(6)</code> is equal to 8).
     *                 Default offset is {@value #DEFAULT_OFFSET}. You can set the offset to <tt>-1</tt> if you want the first value to be <code>fib(0)</code> (i.e. pollCount - offset).
     *                 By default the first value will be <code>fib(1)</code>.
     * @param timeUnit The time unit
     */
    public FibonacciPollInterval(int offset, TimeUnit timeUnit) {
        if (offset <= -1) {
            throw new IllegalArgumentException("Offset must be greater than or equal to -1");
        }
        if (timeUnit == null) {
            throw new IllegalArgumentException("Time unit cannot be null");
        }
        this.offset = offset;
        this.timeUnit = timeUnit;
    }

    /**
     * Generate the next duration
     *
     * @param pollCount        The number of times the condition has been polled (evaluated)
     * @param previousDuration The duration of the previously returned poll interval
     * @return The next duration in the fibonacci sequence.
     */
    public Duration next(int pollCount, Duration previousDuration) {
        return new Duration(fibonacci(offset + pollCount), timeUnit);
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
     * Syntactic sugar for <code>new FibonacciPollInterval(timeUnit)</code>
     *
     * @param timeUnit The time unit
     * @return A new instance of {@link FibonacciPollInterval}.
     */
    public static FibonacciPollInterval fibonacci(TimeUnit timeUnit) {
        return new FibonacciPollInterval(timeUnit);
    }


    /**
     * Syntactic sugar for <code>new FibonacciPollInterval(offset, timeUnit)</code>
     *
     * @param offset   The fibonacci offset. For example if offset is 5 and poll count is 1 then the returned duration will be 8 (since  <code>fib(6)</code> is equal to 8).
     *                 Default offset is {@value #DEFAULT_OFFSET}.
     * @param timeUnit The time unit
     * @return A new instance of {@link FibonacciPollInterval}.
     */
    public static FibonacciPollInterval fibonacci(int offset, TimeUnit timeUnit) {
        return new FibonacciPollInterval(offset, timeUnit);
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
    public FibonacciPollInterval timeUnit(TimeUnit unit) {
        return new FibonacciPollInterval(offset, unit);
    }

    /**
     * Create a new {@link FibonacciPollInterval} with the same time unit but with a different offset
     *
     * @return The same of instance of {@link FibonacciPollInterval}
     */
    public FibonacciPollInterval offset(int offset) {
        return new FibonacciPollInterval(offset, timeUnit);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FibonacciPollInterval)) return false;

        FibonacciPollInterval that = (FibonacciPollInterval) o;

        return offset == that.offset && timeUnit == that.timeUnit;
    }

    @Override
    public int hashCode() {
        int result = timeUnit.hashCode();
        result = 31 * result + offset;
        return result;
    }

    @Override
    public String toString() {
        return "FibonacciPollInterval{" +
                "offset=" + offset +
                ", timeUnit=" + timeUnit +
                '}';
    }
}
