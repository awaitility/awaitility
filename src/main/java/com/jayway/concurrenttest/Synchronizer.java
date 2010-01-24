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

import java.util.concurrent.TimeUnit;

import com.jayway.concurrenttest.synchronizer.AwaitOperationImpl;
import com.jayway.concurrenttest.synchronizer.ConditionSpecification;
import com.jayway.concurrenttest.synchronizer.Duration;
import com.jayway.concurrenttest.synchronizer.SynchronizerOperation;
import com.jayway.concurrenttest.synchronizer.SynchronizerOperationOptions;

public class Synchronizer extends SynchronizerOperationOptions {
    private static volatile Duration defaultPollInterval = Duration.FIVE_HUNDRED_MILLISECONDS;

    private static volatile Duration defaultTimeout = null;
    
    public static void reset() {
    	defaultPollInterval = Duration.FIVE_HUNDRED_MILLISECONDS;
    	defaultTimeout = null;
    }

    public static void block(ConditionSpecification conditionSpecification) throws Exception {
        block(defaultTimeout == null ? forever() : defaultTimeout, conditionSpecification);
    }

    public static void block(long timeout, TimeUnit unit, ConditionSpecification conditionSpecification) throws Exception {
        block(duration(timeout, unit), conditionSpecification);
    }

    public static void block(Duration duration, ConditionSpecification conditionSpecification) throws Exception {
        block(duration, conditionSpecification, null);
    }

    public static void block(ConditionSpecification conditionSpecification, Duration pollInterval) throws Exception {
        block(forever(), conditionSpecification, pollInterval);
    }

    public static void block(long timeout, TimeUnit unit, ConditionSpecification conditionSpecification, Duration pollInterval)
            throws Exception {
        block(duration(timeout, unit), conditionSpecification, pollInterval);
    }

    public static void block(Duration duration, ConditionSpecification conditionSpecification, Duration pollInterval)
            throws Exception {
        await(duration, conditionSpecification, pollInterval).join();
    }

    public static SynchronizerOperation await(long timeout, TimeUnit unit, ConditionSpecification conditionSpecification) {
        return await(duration(timeout, unit), conditionSpecification);
    }

    public static SynchronizerOperation await(ConditionSpecification conditionSpecification) {
        return await(defaultTimeout == null ? forever() : defaultTimeout, conditionSpecification);
    }

    public static SynchronizerOperation await(Duration duration, ConditionSpecification conditionSpecification) {
        return await(duration, conditionSpecification, null);
    }

    public static SynchronizerOperation await(long timeout, TimeUnit unit, ConditionSpecification conditionSpecification,
            Duration pollInterval) {
        return await(duration(timeout, unit), conditionSpecification, pollInterval);
    }

    public static SynchronizerOperation await(ConditionSpecification conditionSpecification, Duration pollInterval) {
        return await(forever(), conditionSpecification, pollInterval);
    }

    public static SynchronizerOperation await(Duration duration, ConditionSpecification conditionSpecification,
            Duration pollInterval) {
        if (pollInterval == null) {
        	pollInterval = defaultPollInterval;
        }
        if (duration == null) {
            duration = defaultTimeout;
        }
        return new AwaitOperationImpl(duration, conditionSpecification, pollInterval);
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
        Synchronizer.defaultTimeout = defaultTimeout;
    }
}