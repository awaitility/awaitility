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
package org.awaitility.core;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Simple implementation of {@link ConditionEvaluationListener} that prints the condition evaluation results to the console using <code>System.out.printf</code>.
 * It also prints the final value if applicable.
 */
public class ConditionEvaluationLogger implements ConditionEvaluationListener<Object> {

    private final TimeUnit timeUnit;

    /**
     * Uses {@link java.util.concurrent.TimeUnit#MILLISECONDS} as unit for elapsed and remaining time.
     */
    public ConditionEvaluationLogger() {
        this(MILLISECONDS);
    }

    /**
     * Specifies the {@link java.util.concurrent.TimeUnit} to use as unit for elapsed and remaining time.
     *
     * @param timeUnit The time unit to use.
     */
    public ConditionEvaluationLogger(TimeUnit timeUnit) {
        if (timeUnit == null) {
            throw new IllegalArgumentException("TimeUnit cannot be null");
        }
        this.timeUnit = timeUnit;
    }

    public void conditionEvaluated(EvaluatedCondition<Object> condition) {
        String description = condition.getDescription();
        long elapsedTime = timeUnit.convert(condition.getElapsedTimeInMS(), MILLISECONDS);
        long remainingTime = timeUnit.convert(condition.getRemainingTimeInMS(), MILLISECONDS);
        String timeUnitAsString = timeUnit.toString().toLowerCase();
        if (condition.isSatisfied()) {
            System.out.printf("%s after %d %s (remaining time %d %s, last poll interval was %d %s)%n", description, elapsedTime, timeUnitAsString, remainingTime, timeUnitAsString,
                    condition.getPollInterval().getValue(), condition.getPollInterval().getTimeUnitAsString());
        } else {
            System.out.printf("%s (elapsed time %d %s, remaining time %d %s (last poll interval was %d %s))%n", description, elapsedTime,
                    timeUnitAsString, remainingTime, timeUnitAsString, condition.getPollInterval().getValue(),
                    condition.getPollInterval().getTimeUnitAsString());
        }
    }
}
