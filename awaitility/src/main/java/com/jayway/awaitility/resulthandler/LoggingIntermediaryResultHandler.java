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
package com.jayway.awaitility.resulthandler;

import com.jayway.awaitility.core.IntermediaryResultHandler;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Simple implementation of {@link IntermediaryResultHandler} that prints the intermediary results to the console using <code>System.out.printf</code>.
 * It also prints the last value.
 */
public class LoggingIntermediaryResultHandler implements IntermediaryResultHandler<Object> {

    private final TimeUnit timeUnit;

    public LoggingIntermediaryResultHandler() {
        this(MILLISECONDS);
    }

    public LoggingIntermediaryResultHandler(TimeUnit timeUnit) {
        if (timeUnit == null) {
            throw new IllegalArgumentException("TimeUnit cannot be null");
        }
        this.timeUnit = timeUnit;
    }


    public void handleMismatch(String mismatchMessage, Object currentValue, long elapsedTimeInMS, long remainingTimeInMS) {
        System.out.printf("%s (elapsed time %d %s, remaining time %d %s)\n", mismatchMessage, timeUnit.convert(elapsedTimeInMS, MILLISECONDS),
                timeUnitAsString(), timeUnit.convert(remainingTimeInMS, MILLISECONDS), timeUnitAsString());
    }

    public void handleMatch(String matchMessage, Object conditionEndValue, long elapsedTimeInMS, long remainingTimeInMS) {
        System.out.printf("%s after %d %s (remaining time %d %s)\n",
                matchMessage, timeUnit.convert(elapsedTimeInMS, MILLISECONDS), timeUnitAsString(), timeUnit.convert(remainingTimeInMS, MILLISECONDS),
                timeUnitAsString());
    }

    private String timeUnitAsString() {
        return timeUnit.toString().toLowerCase();
    }
}
