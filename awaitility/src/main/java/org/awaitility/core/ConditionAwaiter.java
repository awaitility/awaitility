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

import java.beans.Introspector;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.classpath.ClassPathResolver.existInCP;

abstract class ConditionAwaiter implements UncaughtExceptionHandler {
    private final ExecutorService executor;
    private final ConditionEvaluator conditionEvaluator;
    private final AtomicReference<Throwable> uncaughtThrowable;
    private final ConditionSettings conditionSettings;

    /**
     * <p>Constructor for ConditionAwaiter.</p>
     *
     * @param conditionEvaluator a {@link ConditionEvaluator} object.
     * @param conditionSettings  a {@link org.awaitility.core.ConditionSettings} object.
     */
    ConditionAwaiter(final ConditionEvaluator conditionEvaluator, final ConditionSettings conditionSettings) {
        if (conditionEvaluator == null) {
            throw new IllegalArgumentException("You must specify a condition (was null).");
        }
        if (conditionSettings == null) {
            throw new IllegalArgumentException("You must specify the condition settings (was null).");
        }
        if (conditionSettings.shouldCatchUncaughtExceptions()) {
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
        this.conditionSettings = conditionSettings;
        this.conditionEvaluator = conditionEvaluator;
        this.executor = conditionSettings.getPollExecutorService();
        this.uncaughtThrowable = new AtomicReference<Throwable>();
    }

    /**
     * <p>await.</p>
     *
     * @param conditionEvaluationHandler The conditionEvaluationHandler
     */
    @SuppressWarnings("deprecation")
    public <T> void await(final ConditionEvaluationHandler<T> conditionEvaluationHandler) {
        final Duration pollDelay = conditionSettings.getPollDelay();
        final Duration maxWaitTime = conditionSettings.getMaxWaitTime();
        final Duration minWaitTime = conditionSettings.getMinWaitTime();

        final long maxTimeout = maxWaitTime.getValue();
        final TimeUnit maxTimeoutUnit = maxWaitTime.getTimeUnit();

        long pollingStarted = System.currentTimeMillis() - pollDelay.getValueInMS();

        int pollCount = 0;
        boolean succeededBeforeTimeout = false;
        ConditionEvaluationResult lastResult = null;
        Duration evaluationDuration = new Duration(0, MILLISECONDS);
        try {
            conditionEvaluationHandler.start();
            if (!pollDelay.isZero()) {
                Thread.sleep(pollDelay.getValueInMS());
            }
            Duration pollInterval = pollDelay;
            while (!executor.isShutdown() && maxWaitTime.compareTo(evaluationDuration) > 0) {
                pollCount = pollCount + 1;
                lastResult = executor.submit(new ConditionPoller(pollInterval)).get(maxTimeout, maxTimeoutUnit);
                if (lastResult.isSuccessful() || lastResult.hasThrowable()) {
                    break;
                }
                pollInterval = conditionSettings.getPollInterval().next(pollCount, pollInterval);
                Thread.sleep(pollInterval.getValueInMS());
                evaluationDuration = calculateConditionEvaluationDuration(pollDelay, pollingStarted);
            }
            evaluationDuration = calculateConditionEvaluationDuration(pollDelay, pollingStarted);
            succeededBeforeTimeout = maxWaitTime.compareTo(evaluationDuration) > 0;
        } catch (Throwable e1) {
            final Throwable throwable;
            if (e1 instanceof ExecutionException) {
                throwable = e1.getCause();
            } else {
                throwable = e1;
            }
            lastResult = new ConditionEvaluationResult(false, throwable, null);
        }

        try {
            if (uncaughtThrowable.get() != null) {
                throw uncaughtThrowable.get();
            } else if (lastResult != null && lastResult.hasThrowable()) {
                throw lastResult.getThrowable();
            } else if (!succeededBeforeTimeout) {
                final String maxWaitTimeLowerCase = maxWaitTime.getTimeUnitAsString();
                final String message;
                if (conditionSettings.hasAlias()) {
                    message = String.format("Condition with alias '%s' didn't complete within %s %s because %s.",
                            conditionSettings.getAlias(), maxTimeout, maxWaitTimeLowerCase, Introspector.decapitalize(getTimeoutMessage()));
                } else {
                    message = String.format("%s within %s %s.", getTimeoutMessage(), maxTimeout, maxWaitTimeLowerCase);
                }

                Throwable cause = lastResult != null && lastResult.hasTrace() ? lastResult.getTrace() : null;
                // Not all systems support deadlock detection so ignore if ThreadMXBean & ManagementFactory is not in classpath
                if (existInCP("java.lang.management.ThreadMXBean") && existInCP("java.lang.management.ManagementFactory")) {
                    java.lang.management.ThreadMXBean bean = java.lang.management.ManagementFactory.getThreadMXBean();
                    try {
                        long[] threadIds = bean.findDeadlockedThreads();
                        if (threadIds != null) {
                            cause = new DeadlockException(threadIds);
                        }
                    } catch (UnsupportedOperationException ignored) {
                        // findDeadLockedThreads() not supported on this VM,
                        // don't init trace and move on.
                    }
                }
                throw new ConditionTimeoutException(message, cause);
            } else if (evaluationDuration.compareTo(minWaitTime) < 0) {
                String message = String.format("Condition was evaluated in %s %s which is earlier than expected " +
                                "minimum timeout %s %s", evaluationDuration.getValue(), evaluationDuration.getTimeUnit(),
                        minWaitTime.getValue(), minWaitTime.getTimeUnit());
                throw new ConditionTimeoutException(message);
            }
        } catch (Throwable e) {
            CheckedExceptionRethrower.safeRethrow(e);
        } finally {
            uncaughtThrowable.set(null);
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    executor.awaitTermination(1, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                CheckedExceptionRethrower.safeRethrow(e);
            }
        }
    }

    /**
     * <p>getTimeoutMessage.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    protected abstract String getTimeoutMessage();

    /**
     * {@inheritDoc}
     */
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (!conditionSettings.shouldExceptionBeIgnored(throwable)) {
            uncaughtThrowable.set(throwable);
            // We shutdown the executor "now" in order to fail the test immediately
            executor.shutdownNow();
        }
    }

    private class ConditionPoller implements Callable<ConditionEvaluationResult> {
        private final Duration delayed;

        /**
         * @param delayed The duration of the poll interval
         */
        ConditionPoller(Duration delayed) {
            this.delayed = delayed;
        }

        @Override
        public ConditionEvaluationResult call() throws Exception {
            try {
                return conditionEvaluator.eval(delayed);
            } catch (Throwable e) {
                if (conditionSettings.shouldExceptionBeIgnored(e)) {
                    return new ConditionEvaluationResult(false);
                }
                return new ConditionEvaluationResult(false, e, null);
            }
        }
    }

    static Duration calculateConditionEvaluationDuration(Duration pollDelay, long pollingStarted) {
        final long calculatedDuration = System.currentTimeMillis() - pollingStarted - pollDelay.getValueInMS();
        // System.currentTimeMillis() is not strictly monotonic and may appear to run backwards,
        // e.g. when a thread transitions to a different core or an NTP update, and the underlying
        // source may be fairly coarse. Thus, the value between calls can cause a negative calculation on
        // a heavily loaded system. Because of this we return a duration of minimum 1 millis.
        // See https://github.com/awaitility/awaitility/issues/95
        final long potentiallyCompensatedDuration = Math.max(calculatedDuration, 1L);
        return new Duration(potentiallyCompensatedDuration, MILLISECONDS);
    }
}
