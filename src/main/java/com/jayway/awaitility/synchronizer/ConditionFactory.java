package com.jayway.awaitility.synchronizer;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;

/**
 * A factory for creating Condition objects.
 */
public class ConditionFactory {

	private final Duration timeout;

	private final Duration pollInterval;

	private final boolean catchUncaughtExceptions;

	private final String alias;

	/**
	 * Instantiates a new condition factory.
	 * 
	 * @param alias
	 *            the alias
	 * @param timeout
	 *            the timeout
	 * @param pollInterval
	 *            the poll interval
	 * @param catchUncaughtExceptions
	 *            the catch uncaught exceptions
	 */
	public ConditionFactory(String alias, Duration timeout, Duration pollInterval, boolean catchUncaughtExceptions) {
		if (pollInterval == null) {
			throw new IllegalArgumentException("pollInterval cannot be null");
		}
		if (timeout == null) {
			throw new IllegalArgumentException("timeout cannot be null");
		}
		this.alias = alias;
		this.timeout = timeout;
		this.pollInterval = pollInterval;
		this.catchUncaughtExceptions = catchUncaughtExceptions;

	}

	/**
	 * Instantiates a new condition factory.
	 * 
	 * @param timeout
	 *            the timeout
	 * @param pollInterval
	 *            the poll interval
	 * @param catchUncaughtExceptions
	 *            the catch uncaught exceptions
	 */
	public ConditionFactory(Duration timeout, Duration pollInterval, boolean catchUncaughtExceptions) {
		this(null, timeout, pollInterval, catchUncaughtExceptions);
	}

	/**
	 * Await at most <code>timeout</code> before throwing a timeout exception.
	 * 
	 * @param timeout
	 *            the timeout
	 * @return the condition factory
	 */
	public ConditionFactory andWithTimeout(Duration timeout) {
		return new ConditionFactory(alias, timeout, pollInterval, catchUncaughtExceptions);
	}

	/**
	 * Await at most <code>timeout</code> before throwing a timeout exception.
	 * 
	 * @param timeout
	 *            the timeout
	 * @return the condition factory
	 */
	public ConditionFactory atMost(Duration timeout) {
		return new ConditionFactory(alias, timeout, pollInterval, catchUncaughtExceptions);
	}

	/**
	 * Await forever until the condition is satisfied. Caution: You can block
	 * subsequent tests and the entire build can hang indefinitely, it's
	 * recommended to always use a timeout.
	 * 
	 * @return the condition factory
	 */
	public ConditionFactory forever() {
		return new ConditionFactory(alias, Duration.FOREVER, pollInterval, catchUncaughtExceptions);
	}

	/**
	 * Specify the polling interval Awaitility will use for this await
	 * statement. This means the frequency in which the condition is checked for
	 * completetion.
	 * 
	 * @param pollInterval
	 *            the poll interval
	 * @return the condition factory
	 */
	public ConditionFactory andWithPollInterval(Duration pollInterval) {
		return new ConditionFactory(alias, timeout, pollInterval, catchUncaughtExceptions);
	}

	/**
	 * And with timeout.
	 * 
	 * @param timeout
	 *            the timeout
	 * @param unit
	 *            the unit
	 * @return the condition factory
	 */
	public ConditionFactory andWithTimeout(long timeout, TimeUnit unit) {
		return new ConditionFactory(alias, new Duration(timeout, unit), pollInterval, catchUncaughtExceptions);
	}

	/**
	 * At most.
	 * 
	 * @param timeout
	 *            the timeout
	 * @param unit
	 *            the unit
	 * @return the condition factory
	 */
	public ConditionFactory atMost(long timeout, TimeUnit unit) {
		return new ConditionFactory(alias, new Duration(timeout, unit), pollInterval, catchUncaughtExceptions);
	}

	/**
	 * And with poll interval.
	 * 
	 * @param pollInterval
	 *            the poll interval
	 * @param unit
	 *            the unit
	 * @return the condition factory
	 */
	public ConditionFactory andWithPollInterval(long pollInterval, TimeUnit unit) {
		return new ConditionFactory(alias, timeout, new Duration(pollInterval, unit), catchUncaughtExceptions);
	}

	/**
	 * And catch uncaught exceptions.
	 * 
	 * @return the condition factory
	 */
	public ConditionFactory andCatchUncaughtExceptions() {
		return new ConditionFactory(alias, timeout, pollInterval, true);
	}

	/**
	 * Await for an asynchronous operation.
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
		return new ConditionFactory(alias, timeout, pollInterval, catchUncaughtExceptions);
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
		return (S) ProxyCreator.create(service);
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
		until(new MethodCaller<T>(ProxyState.getLastTarget(), ProxyState.getLastMethod(), ProxyState.getLastArgs()),
				matcher);
	}

	/**
	 * Until.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param supplier
	 *            the supplier
	 * @param matcher
	 *            the matcher
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
	 * Until.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param conditionEvaluator
	 *            the condition evaluator
	 * @throws Exception
	 *             the exception
	 */
	public <T> void until(Callable<Boolean> conditionEvaluator) throws Exception {
		AwaitConditionImpl condition = new AwaitConditionImpl(alias, timeout, conditionEvaluator, pollInterval);
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
