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
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final CountDownLatch latch;
    private Throwable throwable = null;
    private final ConditionSettings conditionSettings;

    /**
     * <p>Constructor for ConditionAwaiter.</p>
     *
     * @param condition         a {@link java.util.concurrent.Callable} object.
     * @param conditionSettings a {@link com.jayway.awaitility.core.ConditionSettings} object.
     */
    public ConditionAwaiter(final Callable<Boolean> condition,
                            final ConditionSettings conditionSettings) {
        if (condition == null) {
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
        Runnable poller = new Runnable() {
            public void run() {
                try {
                    if (condition.call()) {
                        latch.countDown();
                    }
                } catch (Exception e) {
                    if (!conditionSettings.shouldExceptionBeIgnored(e)) {
                        throwable = e;
                        latch.countDown();
                    }
                }
            }
        };
        executor.scheduleWithFixedDelay(poller, conditionSettings.getPollDelay().getValueInMS(), conditionSettings
                .getPollInterval().getValueInMS(), TimeUnit.MILLISECONDS);
    }

    /**
     * <p>await.</p>
     */
    public void await() {
        try {
            try {
                final Duration maxWaitTime = conditionSettings.getMaxWaitTime();
                final long timeout = maxWaitTime.getValue();
                final boolean finishedBeforeTimeout;
                if (maxWaitTime == Duration.FOREVER) {
                    latch.await();
                    finishedBeforeTimeout = true;
                } else if (maxWaitTime == Duration.SAME_AS_POLL_INTERVAL) {
                    throw new IllegalStateException("Cannot use 'SAME_AS_POLL_INTERVAL' as maximum wait time.");
                } else {
                    finishedBeforeTimeout = latch.await(timeout, maxWaitTime.getTimeUnit());
                }
                if (throwable != null) {
                    throw throwable;
                } else if (!finishedBeforeTimeout) {
                    final String maxWaitTimeLowerCase = maxWaitTime.getTimeUnitAsString();
                    final String message;
                    if (conditionSettings.hasAlias()) {
                        message = String.format("Condition with alias '%s' didn't complete within %s %s because %s.",
                                conditionSettings.getAlias(), timeout, maxWaitTimeLowerCase, Introspector.decapitalize(getTimeoutMessage()));
                    } else {
                        message = String.format("%s within %s %s.", getTimeoutMessage(), timeout, maxWaitTimeLowerCase);
                    }

                    ConditionTimeoutException e = new ConditionTimeoutException(message);

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
                }
            } finally {
                executor.shutdown();
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            }
        } catch (Throwable e) {
            SafeExceptionRethrower.safeRethrow(e);
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
        this.throwable = throwable;
        if (latch.getCount() != 0) {
            latch.countDown();
        }
    }
}
