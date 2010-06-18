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
package com.jayway.awaitility;

import static com.jayway.awaitility.synchronizer.Duration.SAME_AS_POLL_INTERVAL;

import java.util.concurrent.TimeUnit;

import com.jayway.awaitility.synchronizer.ConditionFactory;
import com.jayway.awaitility.synchronizer.Duration;

/**
 * Awaitility is a small Java DSL for synchronizing (waiting for) asynchronous
 * operations. It makes it very easy to test asynchronous code. Examples:
 * <p>
 * Wait at most 5 seconds until customer status has been updated:
 * 
 * <pre>
 * await().atMost(5, SECONDS).until(customerStatusHasUpdated());
 * </pre>
 * 
 * Wait forever until the call to <code>orderService).orderCount()</code> is
 * greater than 3.
 * 
 * <pre>
 * await().until(callTo(orderService).orderCount(), greaterThan(3));
 * </pre>
 * 
 * Advanced usage: Use a poll interval of 100 milliseconds with an initial delay
 * of 20 milliseconds until customer status is equal to "REGISTERED". This
 * example also uses a named await by specifying an alias
 * ("customer registration"). This makes it easy to find out which await
 * statement that failed if you have multiple awaits in the same test.
 * 
 * <pre>
 * withPollInterval(ONE_HUNDERED_MILLISECONDS).andWithPollDelay(20, MILLISECONDS).await(&quot;customer registration&quot;).until(
 * 		costumerStatus(), equalTo(REGISTERED));
 * </pre>
 * 
 * You can also specify a default timeout, poll interval and poll delay using:
 * 
 * <pre>
 * Awaitility.setDefaultTimeout(..)
 * Awaitility.setDefaultPollInterval(..)
 * Awaitility.setDefaultPollDelay(..)
 * </pre>
 * 
 * You can also reset to the default values using {@link Awaitility#reset()}.
 * <p>
 * A word on poll interval and poll delay: Awaitility starts to check the
 * specified condition (the one you create using the Awaitility DSL) matches for
 * the first time after the specified poll delay (the initial delay before the
 * polling begins). By default Awaitility uses the same poll delay as poll
 * interval which means that it checks the condition periodically first after
 * the given poll delay, and subsequently with the given poll interval; that is
 * executions will commence after pollDelay then pollDelay+pollInterval, then
 * pollDelay + 2 * pollInterval, and so on. <br><b>Note:</b> If you change the poll
 * interval the poll delay will also change to match the specified poll interval
 * <i>unless</i> you've specified a poll delay explicitly.
 * <p>
 * Note that since Awaitility uses polling to verify that a condition matches
 * it's not recommended to use it for precise performance testing. In these
 * cases it's better to use an AOP framework such as AspectJ's compile-time
 * weaving.
 */
public class Awaitility {

	/** The default poll interval (100 ms) */
	private static volatile Duration defaultPollInterval = Duration.ONE_HUNDRED_MILLISECONDS;

	/** The default timeout (10 seconds) */
	private static volatile Duration defaultTimeout = Duration.TEN_SECONDS;

	/** The default poll delay (same as {@link #defaultPollInterval}) */
	private static volatile Duration defaultPollDelay = SAME_AS_POLL_INTERVAL;

	/** Catch all uncaught exceptions by default? */
	private static volatile boolean defaultCatchUncaughtExceptions = true;

	/**
	 * Instruct Awaitility to catch uncaught exceptions from other threads. This
	 * is useful in multi-threaded systems when you want your test to fail
	 * regardless of which thread throwing the exception. Default is
	 * <code>true</code>.
	 */
	public static void catchUncaughtExceptions() {
		defaultCatchUncaughtExceptions = true;
	}

	/**
	 * Instruct Awaitility not to catch uncaught exceptions from other threads.
	 * Your test will not fail if another thread throws an exception.
	 */
	public static void doNotCatchUncaughtExceptions() {
		defaultCatchUncaughtExceptions = false;
	}

	/**
	 * Reset the timeout, poll interval, poll delay, uncaught exception handling
	 * to their default values:
	 * <p>
	 * <ul>
	 * <li>timeout - 10 seconds</li>
	 * <li>poll interval - 100 milliseconds</li>
	 * <li>poll delay - 100 milliseconds</li>
	 * <li>Catch all uncaught exceptions - true</li>
	 * </ul>
	 */
	public static void reset() {
		defaultPollInterval = Duration.ONE_HUNDRED_MILLISECONDS;
		defaultPollDelay = SAME_AS_POLL_INTERVAL;
		defaultTimeout = Duration.TEN_SECONDS;
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
		return new ConditionFactory(alias, defaultTimeout, defaultPollInterval, defaultPollDelay,
				defaultCatchUncaughtExceptions);
	}

	/**
	 * Catching uncaught exceptions.
	 * 
	 * @return the condition factory
	 */
	public static ConditionFactory catchingUncaughtExceptions() {
		return new ConditionFactory(defaultTimeout, defaultPollInterval, defaultPollDelay, true);
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
		return new ConditionFactory(defaultTimeout, new Duration(time, unit), defaultPollInterval,
				defaultCatchUncaughtExceptions);
	}

	/**
	 * With poll interval.
	 * 
	 * @param pollInterval
	 *            the poll interval
	 * @return the condition factory
	 */
	public static ConditionFactory withPollInterval(Duration pollInterval) {
		return new ConditionFactory(defaultTimeout, pollInterval, defaultPollDelay, defaultCatchUncaughtExceptions);
	}

	/**
	 * With poll delay.
	 * 
	 * @param pollDelay
	 *            the poll delay
	 * @return the condition factory
	 */
	public static ConditionFactory withPollDelay(Duration pollDelay) {
		return new ConditionFactory(defaultTimeout, defaultPollInterval, pollDelay, defaultCatchUncaughtExceptions);
	}

	/**
	 * With poll delay.
	 * 
	 * @param time
	 *            the time
	 * @param unit
	 *            the unit
	 * @return the condition factory
	 */
	public static ConditionFactory withPollDelay(long time, TimeUnit unit) {
		return new ConditionFactory(defaultTimeout, defaultPollInterval, new Duration(time, unit),
				defaultCatchUncaughtExceptions);
	}

	/**
	 * With timeout.
	 * 
	 * @param timeout
	 *            the timeout
	 * @return the condition factory
	 */
	public static ConditionFactory withTimeout(Duration timeout) {
		return new ConditionFactory(timeout, defaultPollInterval, defaultPollDelay, defaultCatchUncaughtExceptions);
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
		return new ConditionFactory(new Duration(timeout, timeUnit), defaultPollInterval, defaultPollDelay,
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
		return new ConditionFactory(timeout, defaultPollInterval, defaultPollDelay, defaultCatchUncaughtExceptions);
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
	 * Sets the default poll delay.
	 * 
	 * @param pollDelay
	 *            the poll delay
	 * @param unit
	 *            the unit
	 */
	public static void setDefaultPollDelay(long pollDelay, TimeUnit unit) {
		defaultPollDelay = new Duration(pollDelay, unit);
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
	 * Sets the default poll delay.
	 * 
	 * @param pollDelay
	 *            the new default poll delay
	 */
	public static void setDefaultPollDelay(Duration pollDelay) {
		if (pollDelay == null) {
			throw new IllegalArgumentException("You must specify a poll delay (was null).");
		}
		defaultPollDelay = pollDelay;
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
		Awaitility.defaultTimeout = defaultTimeout;
	}
}
