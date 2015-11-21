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

public class FibonacciPollInterval implements PollInterval {

    private final TimeUnit timeUnit;

    public FibonacciPollInterval() {
        this(TimeUnit.MILLISECONDS);
    }

    public FibonacciPollInterval(TimeUnit timeUnit) {
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
}
