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
package com.jayway.concurrenttest;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.jayway.concurrenttest.synchronizer.ConditionFactory;
import com.jayway.concurrenttest.synchronizer.ConditionOptions;
import com.jayway.concurrenttest.synchronizer.Duration;

public class Synchronizer extends ConditionOptions {
    private static volatile Duration defaultPollInterval = Duration.FIVE_HUNDRED_MILLISECONDS;

    private static volatile Duration defaultTimeout = Duration.FOREVER;

    private static volatile boolean defaultCatchUncaughtExceptions = false;

    public static void catchUncaughtExceptions() {
        defaultCatchUncaughtExceptions = true;
    }

    public static void reset() {
        defaultPollInterval = Duration.FIVE_HUNDRED_MILLISECONDS;
        defaultTimeout = Duration.FOREVER;
        defaultCatchUncaughtExceptions = false;
        Thread.setDefaultUncaughtExceptionHandler(null);
    }

    public static void await(Callable<Boolean> condition) throws Exception {
        await(defaultTimeout, condition);
    }

    public static void await(long timeout, TimeUnit unit, Callable<Boolean> condition) throws Exception {
        await(duration(timeout, unit), condition);
    }

    public static void await(Duration duration, Callable<Boolean> condition) throws Exception {
        await(duration, condition, defaultPollInterval);
    }

    public static void await(Callable<Boolean> condition, Duration pollInterval) throws Exception {
        await(defaultTimeout, condition, pollInterval);
    }

    public static void await(long timeout, TimeUnit unit, Callable<Boolean> conditionEvaluator, Duration pollInterval)
            throws Exception {
        await(duration(timeout, unit), conditionEvaluator, pollInterval);
    }

    public static void await(Duration duration, Callable<Boolean> conditionEvaluator, Duration pollInterval)
            throws Exception {
        new ConditionFactory(duration, pollInterval, defaultCatchUncaughtExceptions).await(conditionEvaluator);
    }

    public static ConditionFactory await() {
        return new ConditionFactory(defaultTimeout, defaultTimeout, defaultCatchUncaughtExceptions);
    }

    public static ConditionFactory catchingUncaughExceptions() {
        return new ConditionFactory(defaultTimeout, defaultPollInterval, true);
    }

    public static ConditionFactory withPollInterval(long time, TimeUnit unit) {
        return new ConditionFactory(defaultTimeout, new Duration(time, unit), defaultCatchUncaughtExceptions);
    }

    public static ConditionFactory withPollInterval(Duration pollInterval) {
        return new ConditionFactory(defaultTimeout, pollInterval, defaultCatchUncaughtExceptions);
    }

    public static ConditionFactory withTimeout(Duration timeout) {
        return new ConditionFactory(timeout, defaultPollInterval, defaultCatchUncaughtExceptions);
    }

    public static ConditionFactory waitAtMost(Duration timeout) {
        return new ConditionFactory(timeout, defaultPollInterval, defaultCatchUncaughtExceptions);
    }

    public static void setDefaultPollInterval(long pollInterval, TimeUnit unit) {
        defaultPollInterval = new Duration(pollInterval, unit);
    }

    public static void setDefaultTimeout(long timeout, TimeUnit unit) {
        defaultTimeout = duration(timeout, unit);
    }

    public static void setDefaultPollInterval(Duration pollInterval) {
        if (pollInterval == null) {
            throw new IllegalArgumentException("You must specify a poll interval (was null).");
        }
        defaultPollInterval = pollInterval;
    }

    public static void setDefaultTimeout(Duration defaultTimeout) {
        if (defaultTimeout == null) {
            throw new IllegalArgumentException("You must specify a default timeout (was null).");
        }
        Synchronizer.defaultTimeout = defaultTimeout;
    }
}
