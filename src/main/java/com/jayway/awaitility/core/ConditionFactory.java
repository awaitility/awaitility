package com.jayway.awaitility.core;

import static com.jayway.awaitility.Duration.SAME_AS_POLL_INTERVAL;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;

import com.jayway.awaitility.Duration;

/**
 * A factory for creating {@link Condition} objects. It's not recommended to
 * instantiate this class directly.
 */
public class ConditionFactory {

	/** The timeout. */
	private final Duration timeout;

	/** The poll interval. */
	private final Duration pollInterval;

	/** The catch uncaught exceptions. */
	private final boolean catchUncaughtExceptions;

	/** The alias. */
	private final String alias;

	/** The poll delay. */
	private final Duration pollDelay;

	/**
	 * Instantiates a new condition factory.
	 * 
	 * @param alias
	 *            the alias
	 * @param timeout
	 *            the timeout
	 * @param pollInterval
	 *            the poll interval
	 * @param pollDelay
	 *            The poll delay
	 * @param catchUncaughtExceptions
	 *            the catch uncaught exceptions
	 */
	public ConditionFactory(String alias, Duration timeout, Duration pollInterval, Duration pollDelay,
			boolean catchUncaughtExceptions) {
		if (pollInterval == null) {
			throw new IllegalArgumentException("pollInterval cannot be null");
		}
		if (timeout == null) {
			throw new IllegalArgumentException("timeout cannot be null");
		}
		if (pollDelay == null) {
			throw new IllegalArgumentException("pollDelay cannot be null");
		}

		this.alias = alias;
		this.timeout = timeout;
		this.pollInterval = pollInterval;
		this.catchUncaughtExceptions = catchUncaughtExceptions;
		this.pollDelay = pollDelay;
	}

	/**
	 * Instantiates a new condition factory.
	 * 
	 * @param timeout
	 *            the timeout
	 * @param pollInterval
	 *            the poll interval
	 * @param pollDelay
	 *            The delay before the polling starts
	 * @param catchUncaughtExceptions
	 *            the catch uncaught exceptions
	 */
	public ConditionFactory(Duration timeout, Duration pollInterval, Duration pollDelay, boolean catchUncaughtExceptions) {
		this(null, timeout, pollInterval, pollInterval, catchUncaughtExceptions);
	}

	/**
	 * Await at most <code>timeout</code> before throwing a timeout exception.
	 * 
	 * @param timeout
	 *            the timeout
	 * @return the condition factory
	 */
	public ConditionFactory andWithTimeout(Duration timeout) {
		return new ConditionFactory(alias, timeout, pollInterval, pollInterval, catchUncaughtExceptions);
	}

	/**
	 * Await at most <code>timeout</code> before throwing a timeout exception.
	 * 
	 * @param timeout
	 *            the timeout
	 * @return the condition factory
	 */
	public ConditionFactory atMost(Duration timeout) {
		return new ConditionFactory(alias, timeout, pollInterval, pollInterval, catchUncaughtExceptions);
	}

	/**
	 * Await forever until the condition is satisfied. Caution: You can block
	 * subsequent tests and the entire build can hang indefinitely, it's
	 * recommended to always use a timeout.
	 * 
	 * @return the condition factory
	 */
	public ConditionFactory forever() {
		return new ConditionFactory(alias, Duration.FOREVER, pollInterval, pollInterval, catchUncaughtExceptions);
	}

	/**
	 * Specify the polling interval Awaitility will use for this await
	 * statement. This means the frequency in which the condition is checked for
	 * completion.
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
	public ConditionFactory andWithPollInterval(Duration pollInterval) {
		return new ConditionFactory(alias, timeout, pollInterval, pollInterval, catchUncaughtExceptions);
	}

	/**
	 * Await at most <code>timeout</code> before throwing a timeout exception.
	 * 
	 * @param timeout
	 *            the timeout
	 * @param unit
	 *            the unit
	 * @return the condition factory
	 */
	public ConditionFactory andWithTimeout(long timeout, TimeUnit unit) {
		return new ConditionFactory(alias, new Duration(timeout, unit), pollInterval, pollInterval,
				catchUncaughtExceptions);
	}

	/**
	 * Specify the delay that will be used before Awaitility starts polling for
	 * the result the first time. If you don't specify a poll delay explicitly
	 * it'll be the same as the poll interval.
	 * 
	 * 
	 * @param delay
	 *            the delay
	 * @param unit
	 *            the unit
	 * @return the condition factory
	 */
	public ConditionFactory andWithPollDelay(long delay, TimeUnit unit) {
		return new ConditionFactory(alias, this.timeout, pollInterval, new Duration(delay, unit),
				catchUncaughtExceptions);
	}

	/**
	 * Specify the delay that will be used before Awaitility starts polling for
	 * the result the first time. If you don't specify a poll delay explicitly
	 * it'll be the same as the poll interval.
	 * 
	 * 
	 * @param pollDelay
	 *            the poll delay
	 * @return the condition factory
	 */
	public ConditionFactory andWithPollDelay(Duration pollDelay) {
		return new ConditionFactory(alias, this.timeout, pollInterval, pollDelay, catchUncaughtExceptions);
	}

	/**
	 * Await at most <code>timeout</code> before throwing a timeout exception.
	 * 
	 * @param timeout
	 *            the timeout
	 * @param unit
	 *            the unit
	 * @return the condition factory
	 */
	public ConditionFactory atMost(long timeout, TimeUnit unit) {
		return new ConditionFactory(alias, new Duration(timeout, unit), pollInterval, pollInterval,
				catchUncaughtExceptions);
	}

	/**
	 * Specify the polling interval Awaitility will use for this await
	 * statement. This means the frequency in which the condition is checked for
	 * completion.
	 * 
	 * <p>
	 * Note that the poll delay will be automatically set as to the same value
	 * as the interval unless it's specified explicitly using
	 * {@link #withPollDelay(Duration)}, {@link #withPollDelay(long, TimeUnit)}
	 * or {@link ConditionFactory#andWithPollDelay(Duration), or
	 * ConditionFactory#andWithPollDelay(long, TimeUnit)}.
	 * </p>
	 * 
	 * 
	 * @param pollInterval
	 *            the poll interval
	 * @param unit
	 *            the unit
	 * @return the condition factory
	 */
	public ConditionFactory andWithPollInterval(long pollInterval, TimeUnit unit) {
		return new ConditionFactory(alias, timeout, new Duration(pollInterval, unit), pollDelay,
				catchUncaughtExceptions);
	}

	/**
	 * Instruct Awaitility to catch uncaught exceptions from other threads. This
	 * is useful in multi-threaded systems when you want your test to fail
	 * regardless of which thread throwing the exception. Default is
	 * <code>true</code>.
	 * 
	 * @return the condition factory
	 */
	public ConditionFactory andCatchUncaughtExceptions() {
		return new ConditionFactory(alias, timeout, pollInterval, pollInterval, true);
	}

	/**
	 * Await for an asynchronous operation. This method returns the same
	 * {@link ConditionFactory} instance and is used only to get a more
	 * fluent-like syntax.
	 * 
	 * @return the condition factory
	 * @throws Exception
	 *             the exception
	 */
	public ConditionFactory await() throws Exception {
		return this;
	}

	/**
	 * Await for an asynchronous operation and give this await instance a
	 * particular name. This is useful in cases when you have several await
	 * statements in one test and you want to know which one that fails (the
	 * alias will be shown if a timeout exception occurs).
	 * 
	 * @param alias
	 *            the alias
	 * @return the condition factory
	 * @throws Exception
	 *             the exception
	 */
	public ConditionFactory await(String alias) throws Exception {
		return new ConditionFactory(alias, timeout, pollInterval, pollInterval, catchUncaughtExceptions);
	}

	/**
	 * A method to increase the readability of the Awaitility DSL. It simply
	 * returns the same condition factory instance.
	 * 
	 * @return the condition factory
	 * @throws Exception
	 *             the exception
	 */
	public ConditionFactory and() throws Exception {
		return this;
	}

	/**
	 * Don't catch uncaught exceptions in other threads. This will <i>not</i>
	 * make the await statement fail if exceptions occur in other threads.
	 * 
	 * @return the condition factory
	 */
	public ConditionFactory andDontCatchUncaughtExceptions() {
		return new ConditionFactory(timeout, pollInterval, pollDelay, false);
	}

	/**
	 * Specify the condition that must be met when waiting for a method call.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param ignore
	 *            the return value of the method call
	 * @param matcher
	 *            The condition that must be met when
	 * @throws Exception
	 *             the exception
	 */
	public <T> void until(T ignore, final Matcher<T> matcher) throws Exception {
		until(new MethodCaller<T>(MethodCallRecorder.getLastTarget(), MethodCallRecorder.getLastMethod(),
				MethodCallRecorder.getLastArgs()), matcher);
	}

	/**
	 * Await until a {@link Callable} supplies a value matching the specified
	 * {@link Matcher}. E.g.
	 * 
	 * <pre>
	 * await().until(numberOfPersons(), is(greaterThan(2)));
	 * </pre>
	 * 
	 * where "numberOfPersons()" returns a standard {@link Callable}:
	 * 
	 * <pre>
	 * private Callable&lt;Integer&gt; numberOfPersons() {
	 * 	return new Callable&lt;Integer&gt;() {
	 * 		public Integer call() throws Exception {
	 * 			return personRepository.size();
	 * 		}
	 * 	};
	 * }
	 * </pre>
	 * 
	 * Using a generic {@link Callable} as done by using this version of "until"
	 * allows you to reuse the "numberOfPersons()" definition in multiple await
	 * statements. I.e. you can easily create another await statement (perhaps
	 * in a different test case) using e.g.
	 * 
	 * <pre>
	 * await().until(numberOfPersons(), is(equalTo(6)));
	 * </pre>
	 * 
	 * @param <T>
	 *            the generic type
	 * @param supplier
	 *            the supplier that is responsible for getting the value that
	 *            should be matched.
	 * @param matcher
	 *            the matcher The hamcrest matcher that checks whether the
	 *            condition is fulfilled.
	 * @throws Exception
	 *             the exception
	 */
	public <T> void until(final Callable<T> supplier, final Matcher<T> matcher) throws Exception {
		if (supplier == null) {
			throw new IllegalArgumentException("You must specify a supplier (was null).");
		}
		if (matcher == null) {
			throw new IllegalArgumentException("You must specify a matcher (was null).");
		}
		until(new ConditionEvaluator() {
			public Boolean call() throws Exception {
				return matcher.matches(supplier.call());
			}
		});
	}

	/**
	 * Await until a {@link Callable} returns <code>true</code>. This is method
	 * is not as generic as the other variants of "until" but it allows for a
	 * more precise and in some cases even more english-like syntax. E.g.
	 * 
	 * <pre>
	 * await().until(numberOfPersonsIsEqualToThree());
	 * </pre>
	 * 
	 * where "numberOfPersonsIsEqualToThree()" returns a standard
	 * {@link Callable} of type {@link Boolean}:
	 * 
	 * <pre>
	 * private Callable&lt;Boolean&gt; numberOfPersons() {
	 * 	return new Callable&lt;Boolean&gt;() {
	 * 		public Boolean call() throws Exception {
	 * 			return personRepository.size() == 3;
	 * 		}
	 * 	};
	 * }
	 * 
	 * @param <T>
	 *            the generic type
	 * @param conditionEvaluator
	 *            the condition evaluator
	 * @throws Exception
	 * the exception
	 */
	public <T> void until(Callable<Boolean> conditionEvaluator) throws Exception {
		Duration pollDelayToUse = pollDelay == SAME_AS_POLL_INTERVAL ? pollInterval : pollDelay;
		Condition condition = new AwaitConditionImpl(alias, timeout, conditionEvaluator, pollInterval, pollDelayToUse);
		if (catchUncaughtExceptions) {
			condition.andCatchAllUncaughtExceptions();
		}
		condition.await();
	}

	/**
	 * The Class MethodCaller.
	 * 
	 * @param <T>
	 *            the generic type
	 */
	static class MethodCaller<T> implements Callable<T> {

		/** The target. */
		private final Object target;

		/** The method. */
		private final Method method;

		/** The args. */
		private final Object[] args;

		/**
		 * Instantiates a new method caller.
		 * 
		 * @param target
		 *            the target
		 * @param method
		 *            the method
		 * @param args
		 *            the args
		 */
		public MethodCaller(Object target, Method method, Object[] args) {
			this.target = target;
			this.method = method;
			this.args = args;
			method.setAccessible(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Callable#call()
		 */
		@SuppressWarnings("unchecked")
		public T call() throws Exception {
			return (T) method.invoke(target, args);
		}
	}
}
