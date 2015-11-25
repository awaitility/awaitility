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

package com.jayway.awaitility.pollinterval;

import com.jayway.awaitility.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Generates a non-linear poll interval based on the fibonacci sequence: [0, 1, 1, 2, 3, 5, 8, 13, ..]
 */
public class FibonacciPollInterval implements PollInterval {

    private final TimeUnit timeUnit;

    /**
     * Create an instance of the {@link FibonacciPollInterval} with a time unit of {@link java.util.concurrent.TimeUnit#MILLISECONDS}.
     */
    public FibonacciPollInterval() {
        this(TimeUnit.MILLISECONDS);
    }

    /**
     * Create an instance of the {@link FibonacciPollInterval} with a supplied time unit.
     */
    public FibonacciPollInterval(TimeUnit timeUnit) {
        if (timeUnit == null) {
            throw new IllegalArgumentException("Time unit cannot be null");
        }
        this.timeUnit = timeUnit;
    }

    private int fibonacci(int n) {
        if (n == 0)
            return 0;
        else if (n == 1)
            return 1;
        else
            return fibonacci(n - 1) + fibonacci(n - 2);
    }

    public Duration next(int pollCount, Duration previousDuration) {
        return new Duration(fibonacci(pollCount), timeUnit);
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
}
