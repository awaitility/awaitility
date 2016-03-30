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

import java.beans.Introspector;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.*;

import static com.jayway.awaitility.classpath.ClassPathResolver.existInCP;

abstract class ConditionAwaiter implements UncaughtExceptionHandler {
    private final ExecutorService executor;
    private final CountDownLatch latch;
    private final ConditionEvaluator conditionEvaluator;
    private Throwable throwable = null;
    private final ConditionSettings conditionSettings;

    /**
     * <p>Constructor for ConditionAwaiter.</p>
     *
     * @param conditionEvaluator a {@link ConditionEvaluator} object.
     * @param conditionSettings  a {@link com.jayway.awaitility.core.ConditionSettings} object.
     */
    public ConditionAwaiter(final ConditionEvaluator conditionEvaluator,
                            final ConditionSettings conditionSettings) {
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
        this.latch = new CountDownLatch(1);
        this.conditionEvaluator = conditionEvaluator;
        this.executor = initExecutorService();
    }

    private boolean conditionCompleted() {
        return latch.getCount() == 0;
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

        final long maxTimeout = maxWaitTime.getValue();
        final TimeUnit maxTimeoutUnit = maxWaitTime.getTimeUnit();

        long pollingStarted = System.currentTimeMillis() - pollDelay.getValueInMS();
        pollSchedulingThread(conditionEvaluationHandler, pollDelay, maxWaitTime).start();

        try {
            try {
                final boolean finishedBeforeTimeout;
                if (maxWaitTime == Duration.FOREVER) {
                    latch.await();
                    finishedBeforeTimeout = true;
                } else if (maxWaitTime == Duration.SAME_AS_POLL_INTERVAL) {
                    throw new IllegalStateException("Cannot use 'SAME_AS_POLL_INTERVAL' as maximum wait time.");
                } else {
                    finishedBeforeTimeout = latch.await(maxTimeout, maxTimeoutUnit);
                }

                Duration evaluationDuration =
                        new Duration(System.currentTimeMillis() - pollingStarted, TimeUnit.MILLISECONDS)
                                .minus(pollDelay);

                if (throwable != null) {
                    throw throwable;
                } else if (!finishedBeforeTimeout) {
                    final String maxWaitTimeLowerCase = maxWaitTime.getTimeUnitAsString();
                    final String message;
                    if (conditionSettings.hasAlias()) {
                        message = String.format("Condition with alias '%s' didn't complete within %s %s because %s.",
                                conditionSettings.getAlias(), maxTimeout, maxWaitTimeLowerCase, Introspector.decapitalize(getTimeoutMessage()));
                    } else {
                        message = String.format("%s within %s %s.", getTimeoutMessage(), maxTimeout, maxWaitTimeLowerCase);
                    }

                    ConditionTimeoutException e = new ConditionTimeoutException(message);

                    // Not all systems support deadlock detection so ignore if ThreadMXBean & ManagementFactory is not in classpath
                    if (existInCP("java.lang.management.ThreadMXBean") && existInCP("java.lang.management.ManagementFactory")) {
                        java.lang.management.ThreadMXBean bean = java.lang.management.ManagementFactory.getThreadMXBean();
                        try {
                            long[] threadIds = bean.findDeadlockedThreads();
                            if (threadIds != null)
                                e.initCause(new DeadlockException(threadIds));
                        } catch (UnsupportedOperationException ignored) {
                            // findDeadLockedThreads() not supported on this VM,
                            // don't init cause and move on.
                        }
                    }

                    throw e;
                } else if (evaluationDuration.compareTo(minWaitTime) < 0) {
                    String message = String.format("Condition was evaluated in %s %s which is earlier than expected " +
                                    "minimum timeout %s %s", evaluationDuration.getValue(), evaluationDuration.getTimeUnit(),
                            minWaitTime.getValue(), minWaitTime.getTimeUnit());
                    throw new ConditionTimeoutException(message);
                }
            } finally {
                executor.shutdown();
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            }
        } catch (Throwable e) {
            CheckedExceptionRethrower.safeRethrow(e);
        }
    }

    private <T> Thread pollSchedulingThread(final ConditionEvaluationHandler<T> conditionEvaluationHandler,
                                            final Duration pollDelay, final Duration maxWaitTime) {
        final long maxTimeout = maxWaitTime.getValue();
        final TimeUnit maxTimeoutUnit = maxWaitTime.getTimeUnit();

        return new Thread(new Runnable() {
            public void run() {
                int pollCount = 0;
                try {
                    conditionEvaluationHandler.start();
                    if (!pollDelay.isZero()) {
                        Thread.sleep(pollDelay.getValueInMS());
                    }
                    Duration pollInterval = pollDelay;
                    while (!executor.isShutdown()) {
                        if (conditionCompleted()) {
                            break;
                        }
                        pollCount = pollCount + 1;
                        Future<?> future = executor.submit(new ConditionPoller(pollInterval));
                        if (maxWaitTime == Duration.FOREVER) {
                            future.get();
                        } else {
                            future.get(maxTimeout, maxTimeoutUnit);
                        }
                        pollInterval = conditionSettings.getPollInterval().next(pollCount, pollInterval);
                        Thread.sleep(pollInterval.getValueInMS());
                    }
                } catch (Exception e) {
                    throwable = e;
                }
            }
        });
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
        this.throwable = throwable;
        if (latch.getCount() != 0) {
            latch.countDown();
        }
    }

    private ExecutorService initExecutorService() {
        return Executors.newSingleThreadExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread thread;
                if (conditionSettings.hasAlias()) {
                    thread = new Thread(Thread.currentThread().getThreadGroup(), r, "awaitility[" + conditionSettings.getAlias() + ']');
                } else {
                    thread = new Thread(Thread.currentThread().getThreadGroup(), r, "awaitility-thread");
                }
                return thread;
            }
        });
    }

    private class ConditionPoller implements Runnable {
        private final Duration delayed;

        /**
         * @param delayed The duration of the poll interval
         */
        public ConditionPoller(Duration delayed) {
            this.delayed = delayed;
        }

        public void run() {
            try {
                if (conditionEvaluator.eval(delayed)) {
                    latch.countDown();
                }
            } catch (Exception e) {
                if (!conditionSettings.shouldExceptionBeIgnored(e)) {
                    throwable = e;
                    latch.countDown();
                }
            }
        }
    }
}
