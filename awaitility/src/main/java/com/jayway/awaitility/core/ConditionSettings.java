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
package com.jayway.awaitility.core;

import com.jayway.awaitility.Duration;
import com.jayway.awaitility.pollinterval.PollInterval;

import static com.jayway.awaitility.Duration.SAME_AS_POLL_INTERVAL;

class ConditionSettings {
    private final String alias;
    private final Duration maxWaitTime;
    private final PollInterval pollInterval;
    private final Duration pollDelay;
    private final boolean catchUncaughtExceptions;
    private final ExceptionIgnorer ignoreExceptions;
    private final ConditionEvaluationListener conditionEvaluationListener;

    /**
     * <p>Constructor for ConditionSettings.</p>
     *
     * @param alias                       a {@link java.lang.String} object.
     * @param catchUncaughtExceptions     a boolean.
     * @param maxWaitTime                 a {@link com.jayway.awaitility.Duration} object.
     * @param pollInterval                a {@link com.jayway.awaitility.Duration} object.
     * @param pollDelay                   a {@link com.jayway.awaitility.Duration} object.
     * @param conditionEvaluationListener a {@link ConditionEvaluationListener} object.
     * @param ignoreExceptions            a boolean.
     */
    public ConditionSettings(String alias, boolean catchUncaughtExceptions, Duration maxWaitTime,
                             PollInterval pollInterval, Duration pollDelay, ConditionEvaluationListener conditionEvaluationListener,
                             ExceptionIgnorer ignoreExceptions) {
        if (maxWaitTime == null) {
            throw new IllegalArgumentException("You must specify a maximum waiting time (was null).");
        }
        if (pollInterval == null) {
            throw new IllegalArgumentException("You must specify a poll interval (was null).");
        }
        if (pollDelay == null) {
            throw new IllegalArgumentException("You must specify a poll delay (was null).");
        }
        this.alias = alias;
        this.maxWaitTime = maxWaitTime;
        this.pollInterval = pollInterval;
        this.pollDelay = pollDelay == SAME_AS_POLL_INTERVAL ? pollInterval.next(1, pollDelay) : pollDelay;
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
     * <p>Getter for the field <code>maxWaitTime</code>.</p>
     *
     * @return a {@link com.jayway.awaitility.Duration} object.
     */
    public Duration getMaxWaitTime() {
        return maxWaitTime;
    }

    /**
     * <p>Getter for the field <code>pollInterval</code>.</p>
     *
     * @return a {@link com.jayway.awaitility.Duration} object.
     */
    public PollInterval getPollInterval() {
        return pollInterval;
    }

    /**
     * <p>Getter for the field <code>pollDelay</code>.</p>
     *
     * @return a {@link com.jayway.awaitility.Duration} object.
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
    public boolean shouldExceptionBeIgnored(Exception e) {
        return ignoreExceptions.shouldIgnoreException(e);
    }
}
