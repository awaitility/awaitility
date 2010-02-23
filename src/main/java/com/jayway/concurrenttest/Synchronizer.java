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
import com.jayway.concurrenttest.synchronizer.Condition;
import com.jayway.concurrenttest.synchronizer.Duration;
import com.jayway.concurrenttest.synchronizer.SynchronizerOperation;
import com.jayway.concurrenttest.synchronizer.SynchronizerOperationOptions;

public class Synchronizer extends SynchronizerOperationOptions {
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

    public static void await(Condition condition) throws Exception {
        await(defaultTimeout, condition);
    }

    public static void await(long timeout, TimeUnit unit, Condition condition) throws Exception {
        await(duration(timeout, unit), condition);
    }

    public static void await(Duration duration, Condition condition) throws Exception {
        await(duration, condition, null);
    }

    public static void await(Condition condition, Duration pollInterval) throws Exception {
        await(defaultTimeout, condition, pollInterval);
    }

    public static void await(long timeout, TimeUnit unit, Condition condition, Duration pollInterval)
            throws Exception {
        await(duration(timeout, unit), condition, pollInterval);
    }

    public static void await(Duration duration, Condition condition, Duration pollInterval)
            throws Exception {
        SynchronizerOperation operation = defineCondition(duration, condition, pollInterval);
        if (defaultCatchUncaughtExceptions) {
        	operation.andCatchAllUncaughtExceptions();
        }
        operation.join();
    }

    public static SynchronizerOperation defineCondition(long timeout, TimeUnit unit, Condition condition) {
        return defineCondition(duration(timeout, unit), condition);
    }

    public static SynchronizerOperation defineCondition(Condition condition) {
        return defineCondition(defaultTimeout, condition);
    }

    public static SynchronizerOperation defineCondition(Duration duration, Condition condition) {
        return defineCondition(duration, condition, null);
    }

    public static SynchronizerOperation defineCondition(long timeout, TimeUnit unit, Condition condition,
            Duration pollInterval) {
        return defineCondition(duration(timeout, unit), condition, pollInterval);
    }

    public static SynchronizerOperation defineCondition(Condition condition, Duration pollInterval) {
        return defineCondition(defaultTimeout, condition, pollInterval);
    }

    public static SynchronizerOperation defineCondition(Duration duration, Condition condition,
            Duration pollInterval) {
        if (pollInterval == null) {
        	pollInterval = defaultPollInterval;
        }
        if (duration == null) {
            duration = defaultTimeout;
        }
        return new AwaitOperationImpl(duration, condition, pollInterval);
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