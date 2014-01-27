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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.*;

abstract class ConditionAwaiter implements UncaughtExceptionHandler {
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final CountDownLatch latch;
    private Exception exception = null;
    private final ConditionSettings conditionSettings;

    public ConditionAwaiter(final Callable<Boolean> condition,
                            ConditionSettings conditionSettings) {
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
                    exception = e;
                    latch.countDown();
                }
            }
        };
        executor.scheduleWithFixedDelay(poller, conditionSettings.getPollDelay().getValueInMS(), conditionSettings
                .getPollInterval().getValueInMS(), TimeUnit.MILLISECONDS);
    }

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
                if (exception != null) {
                    throw exception;
                } else if (!finishedBeforeTimeout) {
                    final String maxWaitTimeLowerCase = maxWaitTime.getTimeUnitAsString();
                    final String message;
                    if (conditionSettings.hasAlias()) {
                        message = String.format("Condition with alias '%s' didn't complete within %s %s.",
                                conditionSettings.getAlias(), timeout, maxWaitTimeLowerCase);
                    } else {
                        message = String.format("%s within %s %s.", getTimeoutMessage(), timeout, maxWaitTimeLowerCase);
                    }
                    throw new ConditionTimeoutException(message);
                }
            } finally {
                executor.shutdown();
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            }
        } catch (Exception e) {
            SafeExceptionRethrower.safeRethrow(e);
        }
    }

    protected abstract String getTimeoutMessage();

    public void uncaughtException(Thread thread, Throwable throwable) {
        if (throwable instanceof Exception) {
            exception = (Exception) throwable;
            if (latch.getCount() != 0) {
                latch.countDown();
            }
        } else {
            SafeExceptionRethrower.safeRethrow(throwable);
        }
    }
}
