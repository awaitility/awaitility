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


import java.lang.Thread.UncaughtExceptionHandler;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.NANOS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.awaitility.classpath.ClassPathResolver.existInCP;
import static org.awaitility.core.TemporalDuration.formatAsString;
import static org.awaitility.core.Uninterruptibles.getUninterruptibly;
import static org.awaitility.core.Uninterruptibles.sleepUninterruptibly;

abstract class ConditionAwaiter implements UncaughtExceptionHandler {
    private final ExecutorService executor;
    private final ConditionEvaluator conditionEvaluator;
    private final AtomicReference<Throwable> uncaughtThrowable;
    private final ConditionSettings conditionSettings;

    private final UncaughtExceptionHandler originalDefaultUncaughtExceptionHandler;

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
        // in order to solve https://github.com/awaitility/awaitility/issues/152 the original handler will be set back at the end
        originalDefaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        if (conditionSettings.shouldCatchUncaughtExceptions()) {
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
        this.conditionSettings = conditionSettings;
        this.conditionEvaluator = conditionEvaluator;
        this.executor = conditionSettings.getExecutorLifecycle().supplyExecutorService();
        this.uncaughtThrowable = new AtomicReference<>();
    }

    /**
     * <p>await.</p>
     *
     * @param conditionEvaluationHandler The conditionEvaluationHandler
     */
    public <T> void await(final ConditionEvaluationHandler<T> conditionEvaluationHandler) {
        final Duration pollDelay = conditionSettings.getPollDelay();
        final Duration maxWaitTime = conditionSettings.getMaxWaitTime();
        final Duration minWaitTime = conditionSettings.getMinWaitTime();
        final Duration holdPredicateWaitTime = conditionSettings.getHoldPredicateTime();

        long pollingStartedNanos = System.nanoTime() - pollDelay.toNanos();

        int pollCount = 0;
        boolean succeededBeforeTimeout = false;
        ConditionEvaluationResult lastResult = null;
        Duration evaluationDuration = Duration.of(0, MILLIS);
        Future<ConditionEvaluationResult> currentConditionEvaluation = null;
        long firstSucceedSinceStarted = 0L;
        try {
            if (executor.isShutdown() || executor.isTerminated()) {
                throw new IllegalStateException("The executor service that Awaitility is instructed to use has been shutdown so condition evaluation cannot be performed. Is there something wrong the thread or executor configuration?");
            }

            conditionEvaluationHandler.start();
            if (!pollDelay.isZero()) {
                sleepUninterruptibly(pollDelay.toNanos(), NANOSECONDS);
            }
            Duration pollInterval = pollDelay;
            while (maxWaitTime.compareTo(evaluationDuration) > 0) {
                executeFailFastConditionIfDefined();
                pollCount = pollCount + 1;
                // Only wait for the next condition evaluation for at most what's remaining of
                Duration maxWaitTimeForThisCondition = maxWaitTime.minus(evaluationDuration);
                currentConditionEvaluation = executor.submit(new ConditionPoller(pollInterval));
                // Wait for condition evaluation to complete with "maxWaitTimeForThisCondition" or else throw TimeoutException
                lastResult = ChronoUnit.FOREVER.getDuration().equals(maxWaitTime) ? getUninterruptibly(currentConditionEvaluation) : getUninterruptibly(currentConditionEvaluation, maxWaitTimeForThisCondition);
                if (lastResult.isSuccessful() && firstSucceedSinceStarted == 0L) {
                    firstSucceedSinceStarted = System.nanoTime();
                } else if (lastResult.isError()) {
                    firstSucceedSinceStarted = 0L;
                }
                if (lastResult.isSuccessful() && (System.nanoTime() - firstSucceedSinceStarted >= holdPredicateWaitTime.toNanos()) || lastResult.hasThrowable()) {
                    break;
                }
                if (lastResult.hasTrace()) {
                    conditionEvaluationHandler.handleIgnoredException(lastResult.getTrace());
                }

                pollInterval = conditionSettings.getPollInterval().next(pollCount, pollInterval);
                sleepUninterruptibly(pollInterval.toNanos(), NANOSECONDS);
                evaluationDuration = calculateConditionEvaluationDuration(pollDelay, pollingStartedNanos);
            }
            evaluationDuration = calculateConditionEvaluationDuration(pollDelay, pollingStartedNanos);
            succeededBeforeTimeout = maxWaitTime.compareTo(evaluationDuration) > 0;
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

        try {
            if (uncaughtThrowable.get() != null) {
                throw uncaughtThrowable.get();
            } else if (lastResult != null && lastResult.hasThrowable()) {
                throw lastResult.getThrowable();
            } else if (!succeededBeforeTimeout) {
                final String message;
                String timeoutMessage = getTimeoutMessage();
                String durationAsString = formatAsString(maxWaitTime);
                if (conditionSettings.hasAlias()) {
                    message = String.format("Condition with alias '%s' didn't complete within %s because %s.",
                            conditionSettings.getAlias(), durationAsString, decapitalize(timeoutMessage));
                } else {
                    message = String.format("%s within %s.", timeoutMessage, durationAsString);
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
                conditionEvaluationHandler.handleTimeout(message, false);
                throw new ConditionTimeoutException(message, cause);
            } else if (evaluationDuration.compareTo(minWaitTime) < 0) {
                String message = String.format("Condition was evaluated in %s which is earlier than expected minimum timeout %s",
                        formatAsString(evaluationDuration), formatAsString(minWaitTime));
                conditionEvaluationHandler.handleTimeout(message, true);
                throw new ConditionTimeoutException(message);
            }
        } catch (Throwable e) {
            CheckedExceptionRethrower.safeRethrow(e);
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(originalDefaultUncaughtExceptionHandler);
            uncaughtThrowable.set(null);
            conditionSettings.getExecutorLifecycle().executeNormalCleanupBehavior(executor);
        }
    }

    private void executeFailFastConditionIfDefined() throws Exception {
        FailFastCondition failFastCondition = conditionSettings.getFailFastCondition();
        if (failFastCondition != null) {
            Boolean terminalFailureReached = failFastCondition.getFailFastCondition().call();
            if (terminalFailureReached != null && terminalFailureReached) {
                String failureReason = failFastCondition.getFailFastFailureReason();
                throw new TerminalFailureException(failureReason);
            }
        }
    }

    private static String decapitalize(String str) {
        if (str == null) {
            return "";
        }
        String firstLetter = str.substring(0, 1).toLowerCase();
        String restLetters = str.substring(1);
        return firstLetter + restLetters;
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
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (!conditionSettings.shouldExceptionBeIgnored(throwable)) {
            uncaughtThrowable.set(throwable);
            // We shutdown the executor "now" in order to fail the test immediately
            conditionSettings.getExecutorLifecycle().executeUnexpectedCleanupBehavior(executor);
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
        public ConditionEvaluationResult call() {
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
        final long calculatedDuration = System.nanoTime() - pollingStarted - pollDelay.toNanos();
        final long potentiallyCompensatedDuration = Math.max(calculatedDuration, 1L);
        return Duration.of(potentiallyCompensatedDuration, NANOS);
    }
}
