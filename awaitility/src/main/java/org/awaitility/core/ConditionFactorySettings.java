/*
 * Copyright 2026 the original author or authors.
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
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Immutable holder of the settings that a {@link ConditionFactory} carries from one DSL call to the next.
 * Each <code>with..</code> method returns a copy with one setting changed. Internal to Awaitility.
 */
final class ConditionFactorySettings {

    /**
     * The alias.
     */
    private final String alias;

    /**
     * Timing constraint.
     */
    private final WaitConstraint timeoutConstraint;

    /**
     * Whether the at most part of the timing constraint was set by the user
     * (atMost, timeout, between, forever or waitAtMost) rather than taken from the defaults.
     */
    private final boolean atMostSetByUser;

    /**
     * The poll interval.
     */
    private final PollInterval pollInterval;

    /**
     * The poll delay.
     */
    private final Duration pollDelay;

    /**
     * The catch uncaught exceptions.
     */
    private final boolean catchUncaughtExceptions;

    /**
     * The ignore exceptions.
     */
    private final ExceptionIgnorer exceptionsIgnorer;

    /**
     * The condition evaluation listener
     */
    private final ConditionEvaluationListener conditionEvaluationListener;

    /**
     * The executor lifecycle
     */
    private final ExecutorLifecycle executorLifecycle;

    /**
     * If this condition if ever false, indicates our condition will never be true.
     */
    private final FailFastCondition failFastCondition;

    /**
     * Supplies extra text that is appended to the timeout message, or <code>null</code> if none.
     */
    private final Supplier<String> timeoutMessageSupplier;

    /**
     * Callback invoked when the await times out, or <code>null</code>. Kept apart from the condition evaluation
     * listener so that changing the listener (or enabling logging) doesn't remove it.
     */
    private final Consumer<? super TimeoutEvent> timeoutCallback;

    /**
     * Creates settings whose at most constraint is not set by the user and that have no timeout message or timeout callback.
     */
    ConditionFactorySettings(String alias, WaitConstraint timeoutConstraint, PollInterval pollInterval, Duration pollDelay,
                             boolean catchUncaughtExceptions, ExceptionIgnorer exceptionsIgnorer,
                             ConditionEvaluationListener conditionEvaluationListener, ExecutorLifecycle executorLifecycle,
                             FailFastCondition failFastCondition) {
        this(alias, timeoutConstraint, false, pollInterval, pollDelay, catchUncaughtExceptions, exceptionsIgnorer,
                conditionEvaluationListener, executorLifecycle, failFastCondition, null, null);
    }

    private ConditionFactorySettings(String alias, WaitConstraint timeoutConstraint, boolean atMostSetByUser,
                                     PollInterval pollInterval, Duration pollDelay, boolean catchUncaughtExceptions,
                                     ExceptionIgnorer exceptionsIgnorer, ConditionEvaluationListener conditionEvaluationListener,
                                     ExecutorLifecycle executorLifecycle, FailFastCondition failFastCondition,
                                     Supplier<String> timeoutMessageSupplier, Consumer<? super TimeoutEvent> timeoutCallback) {
        this.alias = alias;
        this.timeoutConstraint = timeoutConstraint;
        this.atMostSetByUser = atMostSetByUser;
        this.pollInterval = pollInterval;
        this.pollDelay = pollDelay;
        this.catchUncaughtExceptions = catchUncaughtExceptions;
        this.exceptionsIgnorer = exceptionsIgnorer;
        this.conditionEvaluationListener = conditionEvaluationListener;
        this.executorLifecycle = executorLifecycle;
        this.failFastCondition = failFastCondition;
        this.timeoutMessageSupplier = timeoutMessageSupplier;
        this.timeoutCallback = timeoutCallback;
    }

    ConditionFactorySettings withAlias(String alias) {
        return new ConditionFactorySettings(alias, timeoutConstraint, atMostSetByUser, pollInterval, pollDelay,
                catchUncaughtExceptions, exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition, timeoutMessageSupplier, timeoutCallback);
    }

    /**
     * Replaces the timing constraint and keeps whether at most was set by the user (used by e.g. atLeast and during).
     */
    ConditionFactorySettings withTimeoutConstraint(WaitConstraint timeoutConstraint) {
        return new ConditionFactorySettings(alias, timeoutConstraint, atMostSetByUser, pollInterval, pollDelay,
                catchUncaughtExceptions, exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition, timeoutMessageSupplier, timeoutCallback);
    }

    /**
     * Replaces the timing constraint and records that at most was set by the user (used by atMost and forever).
     */
    ConditionFactorySettings withUserSetAtMost(WaitConstraint timeoutConstraint) {
        return new ConditionFactorySettings(alias, timeoutConstraint, true, pollInterval, pollDelay,
                catchUncaughtExceptions, exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition, timeoutMessageSupplier, timeoutCallback);
    }

    ConditionFactorySettings withPollInterval(PollInterval pollInterval) {
        return new ConditionFactorySettings(alias, timeoutConstraint, atMostSetByUser, pollInterval, pollDelay,
                catchUncaughtExceptions, exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition, timeoutMessageSupplier, timeoutCallback);
    }

    ConditionFactorySettings withPollDelay(Duration pollDelay) {
        return new ConditionFactorySettings(alias, timeoutConstraint, atMostSetByUser, pollInterval, pollDelay,
                catchUncaughtExceptions, exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition, timeoutMessageSupplier, timeoutCallback);
    }

    ConditionFactorySettings withCatchUncaughtExceptions(boolean catchUncaughtExceptions) {
        return new ConditionFactorySettings(alias, timeoutConstraint, atMostSetByUser, pollInterval, pollDelay,
                catchUncaughtExceptions, exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition, timeoutMessageSupplier, timeoutCallback);
    }

    ConditionFactorySettings withExceptionsIgnorer(ExceptionIgnorer exceptionsIgnorer) {
        return new ConditionFactorySettings(alias, timeoutConstraint, atMostSetByUser, pollInterval, pollDelay,
                catchUncaughtExceptions, exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition, timeoutMessageSupplier, timeoutCallback);
    }

    ConditionFactorySettings withConditionEvaluationListener(ConditionEvaluationListener conditionEvaluationListener) {
        return new ConditionFactorySettings(alias, timeoutConstraint, atMostSetByUser, pollInterval, pollDelay,
                catchUncaughtExceptions, exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition, timeoutMessageSupplier, timeoutCallback);
    }

    ConditionFactorySettings withExecutorLifecycle(ExecutorLifecycle executorLifecycle) {
        return new ConditionFactorySettings(alias, timeoutConstraint, atMostSetByUser, pollInterval, pollDelay,
                catchUncaughtExceptions, exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition, timeoutMessageSupplier, timeoutCallback);
    }

    ConditionFactorySettings withFailFastCondition(FailFastCondition failFastCondition) {
        return new ConditionFactorySettings(alias, timeoutConstraint, atMostSetByUser, pollInterval, pollDelay,
                catchUncaughtExceptions, exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition, timeoutMessageSupplier, timeoutCallback);
    }

    ConditionFactorySettings withTimeoutMessageSupplier(Supplier<String> timeoutMessageSupplier) {
        return new ConditionFactorySettings(alias, timeoutConstraint, atMostSetByUser, pollInterval, pollDelay,
                catchUncaughtExceptions, exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition, timeoutMessageSupplier, timeoutCallback);
    }

    ConditionFactorySettings withTimeoutCallback(Consumer<? super TimeoutEvent> timeoutCallback) {
        return new ConditionFactorySettings(alias, timeoutConstraint, atMostSetByUser, pollInterval, pollDelay,
                catchUncaughtExceptions, exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition, timeoutMessageSupplier, timeoutCallback);
    }

    String getAlias() {
        return alias;
    }

    WaitConstraint getTimeoutConstraint() {
        return timeoutConstraint;
    }

    boolean isAtMostSetByUser() {
        return atMostSetByUser;
    }

    PollInterval getPollInterval() {
        return pollInterval;
    }

    Duration getPollDelay() {
        return pollDelay;
    }

    boolean isCatchUncaughtExceptions() {
        return catchUncaughtExceptions;
    }

    ExceptionIgnorer getExceptionsIgnorer() {
        return exceptionsIgnorer;
    }

    ConditionEvaluationListener getConditionEvaluationListener() {
        return conditionEvaluationListener;
    }

    ExecutorLifecycle getExecutorLifecycle() {
        return executorLifecycle;
    }

    FailFastCondition getFailFastCondition() {
        return failFastCondition;
    }

    Supplier<String> getTimeoutMessageSupplier() {
        return timeoutMessageSupplier;
    }

    Consumer<? super TimeoutEvent> getTimeoutCallback() {
        return timeoutCallback;
    }
}
