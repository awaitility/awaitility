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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.awaitility.classpath.ClassPathResolver.existInCP;

class ConditionAwaiterImpl implements UncaughtExceptionHandler, ConditionAwaiter {
    protected final ExecutorService executor;
    protected final ConditionEvaluator conditionEvaluator;
    protected final AtomicReference<Throwable> uncaughtThrowable;
    protected final ConditionSettings conditionSettings;
    protected final TimeoutMessageSupplier timeoutMessageSupplierProvider;
    protected Duration evaluationDuration;

    /**
     * <p>Constructor for ConditionAwaiterImpl.</p>
     *
     * @param conditionEvaluator a {@link ConditionEvaluator} object.
     * @param conditionSettings  a {@link org.awaitility.core.ConditionSettings} object.
     */
    ConditionAwaiterImpl(final ConditionEvaluator conditionEvaluator, final ConditionSettings conditionSettings, TimeoutMessageSupplier timeoutMessageSupplier) {
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
        this.executor = conditionSettings.getExecutorLifecycle().supplyExecutorService();
        this.uncaughtThrowable = new AtomicReference<Throwable>();
        this.timeoutMessageSupplierProvider = timeoutMessageSupplier;
    }

    /**
     * <p>await.</p>
     *
     * @param conditionEvaluationHandler The conditionEvaluationHandler
     */
    @Override
    public <T> void await(final ConditionEvaluationHandler<T> conditionEvaluationHandler) {
        final Duration maxWaitTime = conditionSettings.getMaxWaitTime();
        final Duration minWaitTime = conditionSettings.getMinWaitTime();
        evaluationDuration = new Duration(0, MILLISECONDS);
        boolean succeededBeforeTimeout = false;
        final Duration pollDelay = conditionSettings.getPollDelay();
        long pollingStartedNanos = System.nanoTime() - pollDelay.getValueInNS();
        ConditionEvaluationResult lastResult = evaluateCondition(conditionEvaluationHandler, maxWaitTime);

        evaluationDuration = calculateConditionEvaluationDuration(pollDelay, pollingStartedNanos);
        succeededBeforeTimeout = maxWaitTime.compareTo(evaluationDuration) > 0;

        try {
            if (uncaughtThrowable.get() != null) {
                throw uncaughtThrowable.get();
            } else if (lastResult != null && lastResult.hasThrowable()) {
                throw lastResult.getThrowable();
            } else if (!succeededBeforeTimeout) {

                final String message = getTimeoutString(maxWaitTime);

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
            conditionSettings.getExecutorLifecycle().executeNormalCleanupBehavior(executor);
        }
    }

    protected ConditionEvaluationResult evaluateCondition(final ConditionEvaluationHandler conditionEvaluationHandler, final Duration maxWaitTime) {
        ConditionEvaluationResult lastResult = null;
        Future<ConditionEvaluationResult> currentConditionEvaluation = null;
        final Duration pollDelay = conditionSettings.getPollDelay();
        long pollingStartedNanos = System.nanoTime() - pollDelay.getValueInNS();
        int pollCount = 0;
        try {
            if (executor.isShutdown() || executor.isTerminated()) {
                throw new IllegalStateException("The executor service that Awaitility is instructed to use has been shutdown so condition evaluation cannot be performed. Is there something wrong the thread or executor configuration?");
            }

            conditionEvaluationHandler.start();
            if (!pollDelay.isZero()) {
                Thread.sleep(pollDelay.getValueInMS());
            }
            Duration pollInterval = pollDelay;
            while (maxWaitTime.compareTo(evaluationDuration) > 0) {
                pollCount = pollCount + 1;
                // Only wait for the next condition evaluation for at most what's remaining of
                Duration maxWaitTimeForThisCondition = maxWaitTime.minus(evaluationDuration);
                currentConditionEvaluation = executor.submit(new ConditionPoller(pollInterval));
                // Wait for condition evaluation to complete with "maxWaitTimeForThisCondition" or else throw TimeoutException
                lastResult = currentConditionEvaluation.get(maxWaitTimeForThisCondition.getValue(), maxWaitTimeForThisCondition.getTimeUnit());
                if (lastResult.isSuccessful() || lastResult.hasThrowable()) {
                    break;
                }
                pollInterval = conditionSettings.getPollInterval().next(pollCount, pollInterval);
                Thread.sleep(pollInterval.getValueInMS());
                evaluationDuration = calculateConditionEvaluationDuration(pollDelay, pollingStartedNanos);
            }
        } catch (TimeoutException e) {
            lastResult = new ConditionEvaluationResult(false, null, e);
        } catch (ExecutionException e) {
            lastResult = new ConditionEvaluationResult(false, e.getCause(), null);
        } catch (Throwable e) {
            lastResult = new ConditionEvaluationResult(false, e, null);
        } finally {
            if (currentConditionEvaluation != null) {
                // Cancelling future in order to avoid race-condition with last result for Hamcrest matchers
                // See https://github.com/awaitility/awaitility/issues/109
                currentConditionEvaluation.cancel(true);
            }
        }

        return lastResult;
    }

    protected String getTimeoutString(Duration maxWaitTime) {
        long maxTimeout = maxWaitTime.getValue();
        final String maxWaitTimeLowerCase = maxWaitTime.getTimeUnitAsString();
        final String message;
        String timeoutMessage = timeoutMessageSupplierProvider.getTimeoutMessage();

        if (conditionSettings.hasAlias()) {
            message = String.format("Condition with alias '%s' didn't complete within %s %s because %s.",
                    conditionSettings.getAlias(), maxTimeout, maxWaitTimeLowerCase, decapitalize(timeoutMessage));
        } else {
            message = String.format("%s within %s %s.", timeoutMessage, maxTimeout, maxWaitTimeLowerCase);
        }
        return message;
    }

    protected static String decapitalize(String str) {
        if (str == null) {
            return "";
        }
        String firstLetter = str.substring(0, 1).toLowerCase();
        String restLetters = str.substring(1);
        return firstLetter + restLetters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (!conditionSettings.shouldExceptionBeIgnored(throwable)) {
            uncaughtThrowable.set(throwable);
            // We shutdown the executor "now" in order to fail the test immediately
            conditionSettings.getExecutorLifecycle().executeUnexpectedCleanupBehavior(executor);
        }
    }

    protected class ConditionPoller implements Callable<ConditionEvaluationResult> {
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
                    return new ConditionEvaluationResult(false, null, e);
                }
                return new ConditionEvaluationResult(false, e, null);
            }
        }
    }

    static Duration calculateConditionEvaluationDuration(Duration pollDelay, long pollingStarted) {
        return new Duration(System.nanoTime() - pollingStarted - pollDelay.getValueInNS(), NANOSECONDS);
    }
}
