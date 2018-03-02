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

import org.awaitility.Duration;
import org.awaitility.constraint.WaitConstraint;
import org.awaitility.pollinterval.PollInterval;

public class ConditionSettings {
    private final String alias;
    private final WaitConstraint waitConstraint;
    private final PollInterval pollInterval;
    private final Duration pollDelay;
    private final boolean catchUncaughtExceptions;
    private final ExceptionIgnorer ignoreExceptions;
    private final ConditionEvaluationListener conditionEvaluationListener;
    private final ExecutorLifecycle executorLifecycle;

    /**
     * <p>Constructor for ConditionSettings.</p>
     *
     * @param alias                       a {@link java.lang.String} object.
     * @param catchUncaughtExceptions     a boolean.
     * @param waitConstraint              a {@link org.awaitility.constraint.WaitConstraint} object.
     * @param pollInterval                a {@link org.awaitility.Duration} object.
     * @param pollDelay                   a {@link org.awaitility.Duration} object.
     * @param conditionEvaluationListener a {@link ConditionEvaluationListener} object.
     * @param ignoreExceptions            a {@link ExceptionIgnorer} object.
     * @param executorLifecycle           Responsible for performing executor service cleanup after each condition evaluation round
     */
    ConditionSettings(String alias, boolean catchUncaughtExceptions, WaitConstraint waitConstraint,
                      PollInterval pollInterval, Duration pollDelay, ConditionEvaluationListener conditionEvaluationListener,
                      ExceptionIgnorer ignoreExceptions, ExecutorLifecycle executorLifecycle) {
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
}
