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
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Simple implementation of {@link ConditionEvaluationListener} that prints the condition evaluation results to the console using <code>System.out.println</code> by default.
 * You can customize the how the results are written by providing a custom consumer that prints the results.
 */
public class ConditionEvaluationLogger implements ConditionEvaluationListener<Object> {

    private final Consumer<String> logPrinter;
    private final TimeUnit unit;

    /**
     * Uses {@link java.util.concurrent.TimeUnit#MILLISECONDS} as unit for elapsed and remaining time.
     */
    public ConditionEvaluationLogger() {
        this(MILLISECONDS);
    }

    /**
     * Specifies a consumer that is responsible for actually printing the logs
     *
     * @param logPrinter The logger to use
     */
    public ConditionEvaluationLogger(Consumer<String> logPrinter) {
        this(logPrinter, MILLISECONDS);
    }

    /**
     * Specifies the {@link java.util.concurrent.TimeUnit} to use as unit for elapsed and remaining time.
     *
     * @param unit The time unit to use.
     */
    public ConditionEvaluationLogger(TimeUnit unit) {
        this(System.out::println, unit);
    }

    public ConditionEvaluationLogger(Consumer<String> logPrinter, TimeUnit unit) {
        if (logPrinter == null) {
            throw new IllegalArgumentException("LogPrinter cannot be null");
        }
        if (unit == null) {
            throw new IllegalArgumentException("TimeUnit cannot be null");
        }
        this.logPrinter = logPrinter;
        this.unit = unit;
    }

    public void conditionEvaluated(EvaluatedCondition<Object> condition) {
        String description = condition.getDescription();
        long elapsedTime = unit.convert(condition.getElapsedTimeInMS(), MILLISECONDS);
        long remainingTime = unit.convert(condition.getRemainingTimeInMS(), MILLISECONDS);
        String unitAsString = unit.toString().toLowerCase();
        final String message;
        if (condition.isSatisfied()) {
            message = String.format("%s after %d %s (remaining time %d %s, last poll interval was %s)", description, elapsedTime, unitAsString, remainingTime, unitAsString, new TemporalDuration(condition.getPollInterval()).toString());
        } else {
            message = String.format("%s (elapsed time %d %s, remaining time %d %s (last poll interval was %s))", description, elapsedTime, unitAsString, remainingTime, unitAsString, new TemporalDuration(condition.getPollInterval()).toString());
        }
        logPrinter.accept(message);
    }

    @Override
    public void beforeEvaluation(StartEvaluationEvent<Object> condition) {
        logPrinter.accept(condition.getDescription());
    }

    @Override
    public void onTimeout(TimeoutEvent timeoutEvent) {
        logPrinter.accept(String.format("%s", timeoutEvent.getDescription()));
    }

    @Override
    public void exceptionIgnored(IgnoredException ignoredException) {
        logPrinter.accept(String.format("Exception %s has been ignored", ignoredException.getThrowable().getMessage()));
    }

    /**
     * Syntactic sugar to avoid writing the <code>new</code> keyword in Java.
     * Uses {@link java.util.concurrent.TimeUnit#MILLISECONDS} as unit for elapsed and remaining time.
     *
     * @return A new instance of {@link ConditionEvaluationLogger}
     */
    public static ConditionEvaluationLogger conditionEvaluationLogger(Consumer<String> logger) {
        return new ConditionEvaluationLogger(logger);
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