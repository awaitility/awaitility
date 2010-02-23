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
package com.jayway.concurrenttest.synchronizer;

import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;

public class SynchronizerOperationOptions {

    public static Duration withPollInterval(long time, TimeUnit unit) {
        return new Duration(time, unit);
    }

    public static Duration withPollInterval(Duration pollInterval) {
        if (pollInterval == null) {
            throw new IllegalArgumentException("pollInterval cannot be null");
        }
        return pollInterval;
    }

    public static Duration duration(long time, TimeUnit unit) {
        return new Duration(time, unit);
    }

    public static Duration atMost(Duration duration) {
        if (duration == null) {
            throw new IllegalArgumentException("duration cannot be null");
        }
        return duration;
    }

    public static Duration atMost(long time, TimeUnit unit) {
        return new Duration(time, unit);
    }

    public static Duration forever() {
        return Duration.FOREVER;
    }

    public static <T> Condition until(final Supplier<T> supplier, final Matcher<T> matcher) {
        if (supplier == null) {
            throw new IllegalArgumentException("You must specify a supplier (was null).");
        }
        if (matcher == null) {
            throw new IllegalArgumentException("You must specify a matcher (was null).");
        }
        return new Condition() {
            @Override
            public boolean evaluate() throws Exception {
                return matcher.matches(supplier.get());
            }
        };
    }

    public static Condition until(Condition condition) {
        return condition;
    }
}
