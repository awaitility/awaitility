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

    private final TimeUnit unit;

    /**
     * Uses {@link java.util.concurrent.TimeUnit#MILLISECONDS} as unit for elapsed and remaining time.
     */
    public ConditionEvaluationLogger() {
        this(MILLISECONDS);
    }

    /**
     * Specifies the {@link java.util.concurrent.TimeUnit} to use as unit for elapsed and remaining time.
     *
     * @param unit The time unit to use.
     */
    public ConditionEvaluationLogger(TimeUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("TimeUnit cannot be null");
        }
        this.unit = unit;
    }

    public void conditionEvaluated(EvaluatedCondition<Object> condition) {
        String description = condition.getDescription();
        long elapsedTime = unit.convert(condition.getElapsedTimeInMS(), MILLISECONDS);
        long remainingTime = unit.convert(condition.getRemainingTimeInMS(), MILLISECONDS);
        String unitAsString = unit.toString().toLowerCase();
        if (condition.isSatisfied()) {
            System.out.printf("%s after %d %s (remaining time %d %s, last poll interval was %s)%n", description, elapsedTime, unitAsString, remainingTime, unitAsString,
                    new TemporalDuration(condition.getPollInterval()).toString());
        } else {
            System.out.printf("%s (elapsed time %d %s, remaining time %d %s (last poll interval was %s))%n", description, elapsedTime,
                    unitAsString, remainingTime, unitAsString, new TemporalDuration(condition.getPollInterval()).toString());
        }
    }

    @Override
    public void beforeEvaluation(StartEvaluationEvent<Object> condition) {
        System.out.printf("%s", condition.getDescription());
    }

    @Override
    public void onTimeout(TimeoutEvent timeoutEvent) {
        System.out.printf("%s", timeoutEvent.getDescription());
    }

    @Override
    public void exceptionIgnored(IgnoredException ignoredException) {
        System.out.printf("Exception %s has been ignored", ignoredException.getThrowable().getMessage());
    }

    /**
     * Syntactic sugar to avoid writing the <code>new</code> keyword in Java.
     * Uses {@link java.util.concurrent.TimeUnit#MILLISECONDS} as unit for elapsed and remaining time.
     *
     * @return A new instance of {@link ConditionEvaluationLogger}
     */
    public static ConditionEvaluationLogger conditionEvaluationLogger() {
        return new ConditionEvaluationLogger();
    }

    /**
     * Syntactic sugar to avoid writing the <code>new</code> keyword in Java.
     * Specifies the {@link java.util.concurrent.TimeUnit} to use as unit for elapsed and remaining time.
     *
     * @param unit The time unit to use.
     */
    public static ConditionEvaluationLogger conditionEvaluationLogger(TimeUnit unit) {
        return new ConditionEvaluationLogger(unit);
    }
}