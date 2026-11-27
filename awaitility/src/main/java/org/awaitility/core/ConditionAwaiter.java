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


import org.awaitility.core.FailFastCondition.CallableFailFastCondition;
import org.awaitility.core.FailFastCondition.CallableFailFastCondition.FailFastAssertion;

import java.lang.Thread.UncaughtExceptionHandler;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

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
        OriginalDefaultUncaughtExceptionHandler.set(Thread.getDefaultUncaughtExceptionHandler());

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
        final ConditionTimeoutException timeout = tryAwait(conditionEvaluationHandler);
        if (timeout != null) {
            throw timeout;
        }
    }

    /**
     * Same as {@link #await(ConditionEvaluationHandler)}, but the {@link ConditionTimeoutException} that it would throw
     * is returned instead. The listener is notified about the timeout in exactly the same way. Every other exception
     * (from the condition, from fail fast, uncaught exceptions etc) is still thrown.
     *
     * @param conditionEvaluationHandler The conditionEvaluationHandler
     * @return the timeout exception, or <code>null</code> if the condition was fulfilled in time
     */
    <T> ConditionTimeoutException tryAwait(final ConditionEvaluationHandler<T> conditionEvaluationHandler) {
        final Duration pollDelay = conditionSettings.getPollDelay();
        final Duration maxWaitTime = conditionSettings.getMaxWaitTime();
        final Duration minWaitTime = conditionSettings.getMinWaitTime();
        final Duration holdPredicateWaitTime = conditionSettings.getHoldPredicateTime();
        // Set when the hold period (during) cannot fit within the user's at most timeout, see ConditionFactory
        final String unsatisfiableHoldMessage = conditionSettings.getUnsatisfiableHoldMessage();

        long pollingStartedNanos = System.nanoTime() - pollDelay.toNanos();

        int pollCount = 0;
        boolean succeededBeforeTimeout = false;
        ConditionEvaluationResult lastResult = null;
        Duration evaluationDuration = Duration.of(0, MILLIS);
        Future<ConditionEvaluationResult> currentConditionEvaluation = null;
        long firstSucceedSinceStarted = 0L;
        boolean timedOutWhileWaitingForCondition = false;
        try {
            if (executor.isShutdown() || executor.isTerminated()) {
                throw new IllegalStateException("The executor service that Awaitility is instructed to use has been shutdown so condition evaluation cannot be performed. Is there something wrong the thread or executor configuration?");
            }

            conditionEvaluationHandler.start();
            // When the condition can never be fulfilled we skip polling and go straight to the timeout handling below
            final boolean canSucceed = unsatisfiableHoldMessage == null;
            if (canSucceed && !pollDelay.isZero()) {
                sleepUninterruptibly(pollDelay.toNanos(), NANOSECONDS);
            }
            Duration pollInterval = pollDelay;
            while (canSucceed && maxWaitTime.compareTo(evaluationDuration) > 0) {
                executeFailFastConditionIfDefined();
                pollCount = pollCount + 1;
                // Only wait for the next condition evaluation for at most what's remaining of
                Duration maxWaitTimeForThisCondition = maxWaitTime.minus(evaluationDuration);
                currentConditionEvaluation = executor.submit(new ConditionPoller(pollInterval));
                // Wait for condition evaluation to complete with "maxWaitTimeForThisCondition" or else throw TimeoutException
                timedOutWhileWaitingForCondition = true;
                lastResult = ChronoUnit.FOREVER.getDuration().equals(maxWaitTime) ? getUninterruptibly(currentConditionEvaluation) : getUninterruptibly(currentConditionEvaluation, maxWaitTimeForThisCondition);
                timedOutWhileWaitingForCondition = false;
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
                evaluationDuration = calculateConditionEvaluationDuration(pollDelay, pollingStartedNanos, firstSucceedSinceStarted, minWaitTime, holdPredicateWaitTime);
            }
            evaluationDuration = calculateConditionEvaluationDuration(pollDelay, pollingStartedNanos, firstSucceedSinceStarted, minWaitTime, holdPredicateWaitTime);
            succeededBeforeTimeout = canSucceed && maxWaitTime.compareTo(evaluationDuration) > 0;
        } catch (TimeoutException e) {
            if (!timedOutWhileWaitingForCondition || lastResult == null || !lastResult.hasTrace()) {
                lastResult = new ConditionEvaluationResult(false, null, e);
            }
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

        ConditionTimeoutException timeout = null;
        try {
            if (uncaughtThrowable.get() != null) {
                throw uncaughtThrowable.get();
            } else if (lastResult != null && lastResult.hasThrowable()) {
                throw lastResult.getThrowable();
            } else if (!succeededBeforeTimeout) {
                final String message = createTimeoutMessage(unsatisfiableHoldMessage, lastResult, maxWaitTime, holdPredicateWaitTime);

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
                TimeoutMessage timeoutMessageWithCustomText = appendCustomTimeoutMessage(message);
                ConditionTimeoutException timeoutException = timeoutMessageWithCustomText.addSupplierFailureTo(
                        new ConditionTimeoutException(timeoutMessageWithCustomText.message, cause));
                conditionEvaluationHandler.handleTimeout(timeoutMessageWithCustomText.message, false, timeoutException);
                timeout = timeoutException;
            } else if (evaluationDuration.compareTo(minWaitTime) < 0) {
                String message = String.format("Condition was evaluated in %s which is earlier than expected minimum timeout %s",
                        formatAsString(evaluationDuration), formatAsString(minWaitTime));
                TimeoutMessage timeoutMessageWithCustomText = appendCustomTimeoutMessage(message);
                ConditionTimeoutException timeoutException = timeoutMessageWithCustomText.addSupplierFailureTo(
                        new ConditionTimeoutException(timeoutMessageWithCustomText.message));
                conditionEvaluationHandler.handleTimeout(timeoutMessageWithCustomText.message, true, timeoutException);
                timeout = timeoutException;
            }
        } catch (Throwable e) {
            CheckedExceptionRethrower.safeRethrow(e);
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(OriginalDefaultUncaughtExceptionHandler.get());
            uncaughtThrowable.set(null);
            conditionSettings.getExecutorLifecycle().executeNormalCleanupBehavior(executor);
        }
        return timeout;
    }

    private void executeFailFastConditionIfDefined() throws Throwable {
        FailFastCondition failFastCondition = conditionSettings.getFailFastCondition();
        if (failFastCondition == null) {
            return;
        }

        if (failFastCondition instanceof CallableFailFastCondition) {
            CallableFailFastCondition callableFailFastCondition = (CallableFailFastCondition) failFastCondition;
            Boolean terminalFailureReached = callableFailFastCondition.getFailFastCondition().call();
            if (terminalFailureReached != null && terminalFailureReached) {
                throw newTerminalFailureException(callableFailFastCondition, FailFastCondition.DEFAULT_FAILURE_REASON, null);
            }
        } else if (failFastCondition instanceof FailFastAssertion) {
            FailFastAssertion failFastAssertion = (FailFastAssertion) failFastCondition;
            try {
                failFastAssertion.getFailFastAssertion().run();
            } catch (Throwable e) {
                throw newTerminalFailureException(failFastAssertion, e.getMessage(), e);
            }
        }
    }

    /**
     * Creates the {@link TerminalFailureException}. The failure reason may come from a user supplied {@link java.util.function.Supplier}.
     * If that supplier throws, the terminal failure must still be reported, so <code>fallbackReason</code> is used instead
     * (with a note) and the supplier's exception is added as a suppressed exception.
     */
    private static TerminalFailureException newTerminalFailureException(FailFastCondition failFastCondition, String fallbackReason, Throwable cause) {
        String failureReason;
        Throwable reasonFailure = null;
        try {
            failureReason = failFastCondition.getFailFastFailureReason();
        } catch (Throwable t) {
            reasonFailure = t;
            failureReason = (fallbackReason == null ? FailFastCondition.DEFAULT_FAILURE_REASON : fallbackReason)
                    + " (the fail fast failure reason supplier threw " + t + ")";
        }
        if (failureReason == null) {
            failureReason = fallbackReason;
        }
        TerminalFailureException terminalFailureException = new TerminalFailureException(failureReason, cause);
        if (reasonFailure != null) {
            terminalFailureException.addSuppressed(reasonFailure);
        }
        return terminalFailureException;
    }

    /**
     * Appends the text of the user's timeout message supplier, if any, to the standard timeout message.
     * The supplier is called at most once. If it throws, the standard message is kept and the
     * supplier's exception is remembered so it can be added as suppressed to the timeout exception.
     */
    private TimeoutMessage appendCustomTimeoutMessage(String standardMessage) {
        Supplier<String> timeoutMessageSupplier = conditionSettings.getTimeoutMessageSupplier();
        if (timeoutMessageSupplier == null) {
            return new TimeoutMessage(standardMessage, null);
        }
        try {
            String customMessage = timeoutMessageSupplier.get();
            if (customMessage == null || customMessage.isEmpty()) {
                return new TimeoutMessage(standardMessage, null);
            }
            return new TimeoutMessage(standardMessage + "\n" + customMessage, null);
        } catch (Throwable e) {
            return new TimeoutMessage(standardMessage, e);
        }
    }

    private static class TimeoutMessage {
        private final String message;
        private final Throwable supplierFailure;

        TimeoutMessage(String message, Throwable supplierFailure) {
            this.message = message;
            this.supplierFailure = supplierFailure;
        }

        ConditionTimeoutException addSupplierFailureTo(ConditionTimeoutException exception) {
            if (supplierFailure != null) {
                exception.addSuppressed(supplierFailure);
            }
            return exception;
        }
    }

    private String createTimeoutMessage(String unsatisfiableHoldMessage, ConditionEvaluationResult lastResult,
                                        Duration maxWaitTime, Duration holdPredicateWaitTime) {
        if (unsatisfiableHoldMessage != null) {
            return unsatisfiableHoldMessage;
        }
        String durationAsString = formatAsString(maxWaitTime);
        if (!holdPredicateWaitTime.isZero() && lastResult != null && lastResult.isSuccessful()) {
            // The condition matched at the end but had not held for the whole during period. Don't use the condition's
            // own timeout message since it can read like "expected X but was X" (issue 264, message idea from pull request 309).
            String holdAsString = formatAsString(holdPredicateWaitTime);
            if (conditionSettings.hasAlias()) {
                return String.format("Condition with alias '%s' was fulfilled but did not remain fulfilled for %s within %s.",
                        conditionSettings.getAlias(), holdAsString, durationAsString);
            }
            return String.format("Condition was fulfilled but did not remain fulfilled for %s within %s.", holdAsString, durationAsString);
        }
        String timeoutMessage = getTimeoutMessage();
        if (conditionSettings.hasAlias()) {
            return String.format("Condition with alias '%s' didn't complete within %s because %s.",
                    conditionSettings.getAlias(), durationAsString, decapitalize(timeoutMessage));
        }
        return String.format("%s within %s.", timeoutMessage, durationAsString);
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

    static Duration calculateConditionEvaluationDuration(Duration pollDelay, long pollingStarted, long firstSucceedSinceStarted, Duration minWaitTime, Duration holdPredicateWaitTime) {
        final long now = System.nanoTime();
        long calculatedDuration = now - pollingStarted - pollDelay.toNanos();
        if (firstSucceedSinceStarted > 0 && minWaitTime.isZero() && holdPredicateWaitTime.isZero()) {
            calculatedDuration = now - firstSucceedSinceStarted;
        }
        final long potentiallyCompensatedDuration = Math.max(calculatedDuration, 1L);
        return Duration.of(potentiallyCompensatedDuration, NANOS);
    }
}
