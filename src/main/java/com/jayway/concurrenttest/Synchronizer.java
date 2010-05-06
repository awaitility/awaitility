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

import com.jayway.concurrenttest.synchronizer.ConditionFactory;
import com.jayway.concurrenttest.synchronizer.Duration;

/**
 * 
 */
public class Synchronizer {

	private static volatile Duration defaultPollInterval = Duration.ONE_HUNDRED_MILLISECONDS;

	private static volatile Duration defaultTimeout = Duration.TEN_SECONDS;

	private static volatile boolean defaultCatchUncaughtExceptions = true;

	/**
	 * Catch uncaught exceptions.
	 */
	public static void catchUncaughtExceptions() {
		defaultCatchUncaughtExceptions = true;
	}

	/**
	 * Do not catch uncaught exceptions.
	 */
	public static void doNotCatchUncaughtExceptions() {
		defaultCatchUncaughtExceptions = false;
	}

	/**
	 * Reset.
	 */
	public static void reset() {
		defaultPollInterval = Duration.FIVE_HUNDRED_MILLISECONDS;
		defaultTimeout = Duration.FOREVER;
		defaultCatchUncaughtExceptions = false;
		Thread.setDefaultUncaughtExceptionHandler(null);
	}

	/**
	 * Await.
	 * 
	 * @return the condition factory
	 */
	public static ConditionFactory await() {
		return await(null);
	}

	/**
	 * Await.
	 * 
	 * @param alias
	 *            the alias
	 * @return the condition factory
	 */
	public static ConditionFactory await(String alias) {
		return new ConditionFactory(alias, defaultTimeout, defaultPollInterval, defaultCatchUncaughtExceptions);
	}

	/**
	 * Catching uncaught exceptions.
	 * 
	 * @return the condition factory
	 */
	public static ConditionFactory catchingUncaughtExceptions() {
		return new ConditionFactory(defaultTimeout, defaultPollInterval, true);
	}

	/**
	 * Specify the poll interval. This defines how often Awaitility will poll
	 * for a result matching the stop criteria.
	 * 
	 * @param time
	 *            the time
	 * @param unit
	 *            the unit
	 * @return the condition factory
	 */
	public static ConditionFactory withPollInterval(long time, TimeUnit unit) {
		return new ConditionFactory(defaultTimeout, new Duration(time, unit), defaultCatchUncaughtExceptions);
	}

	/**
	 * With poll interval.
	 * 
	 * @param pollInterval
	 *            the poll interval
	 * @return the condition factory
	 */
	public static ConditionFactory withPollInterval(Duration pollInterval) {
		return new ConditionFactory(defaultTimeout, pollInterval, defaultCatchUncaughtExceptions);
	}

	/**
	 * With timeout.
	 * 
	 * @param timeout
	 *            the timeout
	 * @return the condition factory
	 */
	public static ConditionFactory withTimeout(Duration timeout) {
		return new ConditionFactory(timeout, defaultPollInterval, defaultCatchUncaughtExceptions);
	}

	/**
	 * With timeout.
	 * 
	 * @param timeout
	 *            the timeout
	 * @param timeUnit
	 *            the time unit
	 * @return the condition factory
	 */
	public static ConditionFactory withTimeout(long timeout, TimeUnit timeUnit) {
		return new ConditionFactory(new Duration(timeout, timeUnit), defaultPollInterval,
				defaultCatchUncaughtExceptions);
	}

	/**
	 * Wait at most.
	 * 
	 * @param timeout
	 *            the timeout
	 * @return the condition factory
	 */
	public static ConditionFactory waitAtMost(Duration timeout) {
		return new ConditionFactory(timeout, defaultPollInterval, defaultCatchUncaughtExceptions);
	}

	/**
	 * Sets the default poll interval.
	 * 
	 * @param pollInterval
	 *            the poll interval
	 * @param unit
	 *            the unit
	 */
	public static void setDefaultPollInterval(long pollInterval, TimeUnit unit) {
		defaultPollInterval = new Duration(pollInterval, unit);
	}

	/**
	 * Sets the default timeout.
	 * 
	 * @param timeout
	 *            the timeout
	 * @param unit
	 *            the unit
	 */
	public static void setDefaultTimeout(long timeout, TimeUnit unit) {
		defaultTimeout = new Duration(timeout, unit);
	}

	/**
	 * Sets the default poll interval.
	 * 
	 * @param pollInterval
	 *            the new default poll interval
	 */
	public static void setDefaultPollInterval(Duration pollInterval) {
		if (pollInterval == null) {
			throw new IllegalArgumentException("You must specify a poll interval (was null).");
		}
		defaultPollInterval = pollInterval;
	}

	/**
	 * Sets the default timeout.
	 * 
	 * @param defaultTimeout
	 *            the new default timeout
	 */
	public static void setDefaultTimeout(Duration defaultTimeout) {
		if (defaultTimeout == null) {
			throw new IllegalArgumentException("You must specify a default timeout (was null).");
		}
		Synchronizer.defaultTimeout = defaultTimeout;
	}
}
