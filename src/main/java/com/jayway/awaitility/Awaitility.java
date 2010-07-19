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

import static com.jayway.awaitility.Duration.SAME_AS_POLL_INTERVAL;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.jayway.awaitility.core.ConditionFactory;
import com.jayway.awaitility.core.MethodCallRecorder;

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
 * Wait forever until the call to <code>orderService.orderCount()</code> is
 * greater than 3.
 * 
 * <pre>
 * await().forever().until(callTo(orderService).orderCount(), greaterThan(3));
 * </pre>
 * 
 * Advanced usage: Use a poll interval of 100 milliseconds with an initial delay
 * of 20 milliseconds until customer status is equal to "REGISTERED". This
 * example also uses a named await by specifying an alias
 * ("customer registration"). This makes it easy to find out which await
 * statement that failed if you have multiple awaits in the same test.
 * 
 * <pre>
 * withPollInterval(ONE_HUNDERED_MILLISECONDS).andWithPollDelay(20, MILLISECONDS).await(&quot;customer registration&quot;)
 * 		.until(costumerStatus(), equalTo(REGISTERED));
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
 * In order to use Awaitility effectively it's recommended to statically import
 * the following methods from the Awaitility framework:
 * <ul>
 * <li>com.jayway.awaitility.Awaitlity.*</li>
 * <li>com.jayway.awaitility.Duration.*</li>
 * </ul>
 * It may also be useful to import these methods:
 * <ul>
 * <li>java.util.concurrent.TimeUnit.*</li>
 * <li>org.hamcrest.Matchers.*</li>
 * <li>org.junit.Assert.*</li>
 * </ul>
 * </p>
 * <p>
 * A word on poll interval and poll delay: Awaitility starts to check the
 * specified condition (the one you create using the Awaitility DSL) matches for
 * the first time after a "poll delay" (the initial delay before the polling
 * begins). By default Awaitility uses the same poll delay as poll interval
 * which means that it checks the condition periodically first after the given
 * poll delay, and subsequently with the given poll interval; that is conditions
 * are checked after pollDelay then pollDelay+pollInterval, then pollDelay + 2
 * pollInterval, and so on.<br>
 * <b>Note:</b> If you change the poll interval the poll delay will also change
 * to match the specified poll interval <i>unless</i> you've specified a poll
 * delay explicitly.
 * <p>
 * Note that since Awaitility uses polling to verify that a condition matches
 * it's not recommended to use it for precise performance testing. In these
 * cases it's better to use an AOP framework such as AspectJ's compile-time
 * weaving.
 * </p>
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
	 * Instruct Awaitility to catch uncaught exceptions from other threads by
	 * default. This is useful in multi-threaded systems when you want your test
	 * to fail regardless of which thread throwing the exception. Default is
	 * <code>true</code>.
	 */
	public static void catchUncaughtExceptionsByDefault() {
		defaultCatchUncaughtExceptions = true;
	}

	/**
	 * Instruct Awaitility not to catch uncaught exceptions from other threads.
	 * Your test will not fail if another thread throws an exception.
	 */
	public static void doNotCatchUncaughtExceptionsByDefault() {
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
	 * Start building an await statement.
	 * 
	 * @return the condition factory
	 */
	public static ConditionFactory await() {
		return await(null);
	}

	/**
	 * Start building a named await statement. This is useful is cases when you
	 * have several awaits in your test and you need to tell them apart. If a
	 * named await timeout's the <code>alias</code> will be displayed indicating
	 * which await statement that failed.
	 * 
	 * @param alias
	 *            the alias that will be shown if the await timeouts.
	 * @return the condition factory
	 */
	public static ConditionFactory await(String alias) {
		return new ConditionFactory(alias, defaultTimeout, defaultPollInterval, defaultPollDelay,
				defaultCatchUncaughtExceptions);
	}

	/**
	 * Catching uncaught exceptions in other threads. This will make the await
	 * statement fail even if exceptions occur in other threads. This is the
	 * default behavior.
	 * 
	 * @return the condition factory
	 */
	public static ConditionFactory catchUncaughtExceptions() {
		return new ConditionFactory(defaultTimeout, defaultPollInterval, defaultPollDelay, true);
	}

	/**
	 * Don't catch uncaught exceptions in other threads. This will <i>not</i>
	 * make the await statement fail if exceptions occur in other threads.
	 * 
	 * 
	 * @return the condition factory
	 */
	public static ConditionFactory dontCatchUncaughtExceptions() {
		return new ConditionFactory(defaultTimeout, defaultPollInterval, defaultPollDelay, false);
	}

	/**
	 * Specify the poll interval. This defines how often Awaitility will poll
	 * for a result matching the stop criteria.
	 * <p>
	 * Note that the poll delay will be automatically set as to the same value
	 * as the interval unless it's specified explicitly using
	 * {@link #withPollDelay(Duration)}, {@link #withPollDelay(long, TimeUnit)}
	 * or {@link ConditionFactory#andWithPollDelay(Duration), or
	 * ConditionFactory#andWithPollDelay(long, TimeUnit)}.
	 * </p>
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
	 * Specify the poll interval by passing a {@link Duration}. This defines how
	 * often Awaitility will poll for a result matching the stop criteria.
	 * <p>
	 * Note that the poll delay will be automatically set as to the same value
	 * as the interval unless it's specified explicitly using
	 * {@link #withPollDelay(Duration)}, {@link #withPollDelay(long, TimeUnit)}
	 * or {@link ConditionFactory#andWithPollDelay(Duration), or
	 * ConditionFactory#andWithPollDelay(long, TimeUnit)}.
	 * </p>
	 * 
	 * @param pollInterval
	 *            the poll interval
	 * @return the condition factory
	 */
	public static ConditionFactory withPollInterval(Duration pollInterval) {
		return new ConditionFactory(defaultTimeout, pollInterval, defaultPollDelay, defaultCatchUncaughtExceptions);
	}

	/**
	 * Specify the delay that will be used before Awaitility starts polling for
	 * the result the first time. If you don't specify a poll delay explicitly
	 * it'll be the same as the poll interval.
	 * 
	 * @param pollDelay
	 *            the poll delay
	 * @return the condition factory
	 */
	public static ConditionFactory withPollDelay(Duration pollDelay) {
		return new ConditionFactory(defaultTimeout, defaultPollInterval, pollDelay, defaultCatchUncaughtExceptions);
	}

	/**
	 * Specify the delay that will be used before Awaitility starts polling for
	 * the result the first time. If you don't specify a poll delay explicitly
	 * it'll be the same as the poll interval.
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
	 * Specify the timeout for the await statement. When the
	 * <code>timeout</code> has been reached a {@link TimeoutException} is
	 * thrown.
	 * 
	 * @param timeout
	 *            the timeout
	 * @return the condition factory
	 */
	public static ConditionFactory withTimeout(Duration timeout) {
		return new ConditionFactory(timeout, defaultPollInterval, defaultPollDelay, defaultCatchUncaughtExceptions);
	}

	/**
	 * Specify the timeout for the await statement. When the
	 * <code>timeout</code> has been reached a {@link TimeoutException} is
	 * thrown.
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
	 * An alternative to using {@link #await()} if you want to specify a timeout
	 * directly.
	 * 
	 * @param timeout
	 *            the timeout
	 * @return the condition factory
	 */
	public static ConditionFactory waitAtMost(Duration timeout) {
		return new ConditionFactory(timeout, defaultPollInterval, defaultPollDelay, defaultCatchUncaughtExceptions);
	}

	/**
	 * An alternative to using {@link #await()} if you want to specify a timeout
	 * directly.
	 * 
	 * @param value
	 *            the value
	 * @param unit
	 *            the unit
	 * @return the condition factory
	 */
	public static ConditionFactory waitAtMost(long value, TimeUnit unit) {
		return new ConditionFactory(new Duration(value, unit), defaultPollInterval, defaultPollDelay,
				defaultCatchUncaughtExceptions);
	}

	/**
	 * Sets the default poll interval that all await statements will use.
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
	 * Sets the default poll delay all await statements will use.
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
	 * Sets the default timeout all await statements will use.
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
	 * Sets the default poll interval that all await statements will use.
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
	 * Sets the default poll delay that all await statements will use.
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
	 * Sets the default timeout that all await statements will use.
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

	/**
	 * Await until a specific method invocation returns something. E.g.
	 * 
	 * <pre>
	 * await().until(callTo(service).getCount(), greaterThan(2));
	 * </pre>
	 * 
	 * Here we tell Awaitility to wait until the <code>service.getCount()</code>
	 * method returns a value that is greater than 2.
	 * 
	 * @param <S>
	 *            The type of the service.
	 * @param service
	 *            the service that contains the method of interest.
	 * @return A proxy of the service
	 */
	@SuppressWarnings("unchecked")
	public static <S> S callTo(S service) {
		return (S) MethodCallRecorder.createProxy(service);
	}
}
