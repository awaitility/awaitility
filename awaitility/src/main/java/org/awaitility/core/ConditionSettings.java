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

import org.awaitility.constraint.WaitConstraint;
import org.awaitility.pollinterval.PollInterval;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConditionSettings {
    private final String alias;
    private final WaitConstraint waitConstraint;
    private final PollInterval pollInterval;
    private final Duration pollDelay;
    private final boolean catchUncaughtExceptions;
    private final ExceptionIgnorer ignoreExceptions;
    private final ConditionEvaluationListener conditionEvaluationListener;
    private final ExecutorLifecycle executorLifecycle;
    private final FailFastCondition failFastCondition;
    private final Supplier<String> timeoutMessageSupplier;
    private final Consumer<? super TimeoutEvent> timeoutCallback;
    private final String unsatisfiableHoldMessage;

    /**
     * <p>Constructor for ConditionSettings.</p>
     * @param alias                       a {@link String} object.
     * @param catchUncaughtExceptions     a boolean.
     * @param waitConstraint              a {@link WaitConstraint} object.
     * @param pollInterval                a {@link org.awaitility.Duration} object.
     * @param pollDelay                   a {@link org.awaitility.Duration} object.
     * @param conditionEvaluationListener a {@link ConditionEvaluationListener} object.
     * @param ignoreExceptions            a {@link ExceptionIgnorer} object.
     * @param executorLifecycle           Responsible for performing executor service cleanup after each condition evaluation round
     * @param failFastCondition           a Callable that if returns true, fails the test immediately
     * @param timeoutMessageSupplier      supplies text to append to the timeout message, or <code>null</code>
     * @param timeoutCallback             invoked when the await times out, may be <code>null</code>
     * @param unsatisfiableHoldMessage    the timeout message to raise at the start of the await because the hold period
     *                                    (during) cannot fit within the user-set at most timeout and the poll delay,
     *                                    or <code>null</code> if the await can succeed
     */
    ConditionSettings(String alias, boolean catchUncaughtExceptions, WaitConstraint waitConstraint,
                      PollInterval pollInterval, Duration pollDelay, ConditionEvaluationListener conditionEvaluationListener,
                      ExceptionIgnorer ignoreExceptions, ExecutorLifecycle executorLifecycle, final FailFastCondition failFastCondition,
                      Supplier<String> timeoutMessageSupplier, Consumer<? super TimeoutEvent> timeoutCallback,
                      String unsatisfiableHoldMessage) {
        if (waitConstraint == null) {
            throw new IllegalArgumentException("You must specify a maximum waiting time (was null).");
        }
        if (pollInterval == null) {
            throw new IllegalArgumentException("You must specify a poll interval (was null).");
        }
        this.executorLifecycle = executorLifecycle;
        this.alias = alias;
        this.waitConstraint = waitConstraint;
        this.pollInterval = pollInterval;
        this.pollDelay = pollDelay;
        this.catchUncaughtExceptions = catchUncaughtExceptions;
        this.conditionEvaluationListener = conditionEvaluationListener;
        this.ignoreExceptions = ignoreExceptions;
        this.failFastCondition = failFastCondition;
        this.timeoutMessageSupplier = timeoutMessageSupplier;
        this.timeoutCallback = timeoutCallback;
        this.unsatisfiableHoldMessage = unsatisfiableHoldMessage;
    }

    /**
     * <p>Getter for the field <code>alias</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * <p>Returning maximum wait time from field <code>waitConstraint</code>.</p>
     *
     * @return a {@link org.awaitility.Duration} object.
     */
    public Duration getMaxWaitTime() {
        return waitConstraint.getMaxWaitTime();
    }

    /**
     * <p>Returning minimum wait time from field <code>waitConstraint</code>.</p>
     *
     * @return a {@link org.awaitility.Duration} object.
     */
    public Duration getMinWaitTime() {
        return waitConstraint.getMinWaitTime();
    }

    /**
     * <p>Returning hold predicate wait time from field <code>waitConstraint</code>.</p>
     *
     * @return a {@link org.awaitility.Duration} object.
     */
    public Duration getHoldPredicateTime() {
        return waitConstraint.getHoldPredicateTime();
    }

    /**
     * <p>Getter for the field <code>pollInterval</code>.</p>
     *
     * @return a {@link org.awaitility.Duration} object.
     */
    public PollInterval getPollInterval() {
        return pollInterval;
    }

    /**
     * <p>Getter for the field <code>pollDelay</code>.</p>
     *
     * @return a {@link org.awaitility.Duration} object.
     */
    public Duration getPollDelay() {
        return pollDelay;
    }

    /**
     * <p>hasAlias</p>
     *
     * @return a boolean.
     */
    public boolean hasAlias() {
        return alias != null;
    }

    /**
     * <p>shouldCatchUncaughtExceptions.</p>
     *
     * @return a boolean.
     */
    public boolean shouldCatchUncaughtExceptions() {
        return catchUncaughtExceptions;
    }

    /**
     * <p>Getter for the field <code>conditionResultLogger</code></p>
     *
     * @return {@link ConditionEvaluationListener} object.
     */
    public ConditionEvaluationListener getConditionEvaluationListener() {
        return conditionEvaluationListener;
    }

    /**
     * @return true if a particular exception should be ignored
     */
    public boolean shouldExceptionBeIgnored(Throwable e) {
        return ignoreExceptions.shouldIgnoreException(e);
    }

    /**
     * @return The executor lifecycle
     */
    public ExecutorLifecycle getExecutorLifecycle() {
        return executorLifecycle;
    }

    /**
     * @return the fail fast {@link Callable}.
     */
    public FailFastCondition getFailFastCondition() {
        return this.failFastCondition;
    }

    /**
     * @return the supplier of the text that is appended to the timeout message, or <code>null</code> if none was set.
     */
    Supplier<String> getTimeoutMessageSupplier() {
        return timeoutMessageSupplier;
    }

    /**
     * @return the callback to invoke when the await times out, or <code>null</code> if none is set
     */
    Consumer<? super TimeoutEvent> getTimeoutCallback() {
        return timeoutCallback;
    }

    /**
     * @return the timeout message to use when the condition can never be fulfilled because the hold period (during)
     * does not fit within the at most timeout minus the poll delay, or <code>null</code> if the condition can be fulfilled.
     */
    String getUnsatisfiableHoldMessage() {
        return unsatisfiableHoldMessage;
    }
}
