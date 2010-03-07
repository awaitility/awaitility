package com.jayway.concurrenttest.synchronizer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;

public class ConditionFactory {
	private final Duration timeout;
	private final Duration pollInterval;
	private final boolean catchUncaughtExceptions;

	public ConditionFactory(Duration timeout, Duration pollInterval, boolean catchUncaughtExceptions) {
		if (pollInterval == null) {
			throw new IllegalArgumentException("pollInterval cannot be null");
		}
		if (timeout == null) {
			throw new IllegalArgumentException("timeout cannot be null");
		}
		this.timeout = timeout;
		this.pollInterval = pollInterval;
		this.catchUncaughtExceptions = catchUncaughtExceptions;
	}

	public ConditionFactory andTimeout(Duration timeout) {
		return new ConditionFactory(timeout, pollInterval, catchUncaughtExceptions);
	}

	public ConditionFactory atMost(Duration timeout) {
		return new ConditionFactory(timeout, pollInterval, catchUncaughtExceptions);
	}

	public ConditionFactory forever() {
		return new ConditionFactory(Duration.FOREVER, pollInterval, catchUncaughtExceptions);
	}

	public ConditionFactory andPollInterval(Duration pollInterval) {
		return new ConditionFactory(timeout, pollInterval, catchUncaughtExceptions);
	}

	public ConditionFactory andTimeout(long timeout, TimeUnit unit) {
		return new ConditionFactory(new Duration(timeout, unit), pollInterval, catchUncaughtExceptions);
	}

	public ConditionFactory atMost(long timeout, TimeUnit unit) {
		return new ConditionFactory(new Duration(timeout, unit), pollInterval, catchUncaughtExceptions);
	}

	public ConditionFactory andPollInterval(long pollInterval, TimeUnit unit) {
		return new ConditionFactory(timeout, new Duration(pollInterval, unit), catchUncaughtExceptions);
	}

	public ConditionFactory andCatchUncaughtExceptions() {
		return new ConditionFactory(timeout, pollInterval, true);
	}

	public ConditionFactory await() throws Exception {
		return this;
	}

	public ConditionFactory and() throws Exception {
		return this;
	}

	private static Object lastTarget;
	private static Method lastMethod;
	private static Object[] lastArgs;

	@SuppressWarnings("unchecked")
	public static <S> S callTo(S service) {
		Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), service.getClass()
				.getInterfaces(), new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				lastMethod = method;
				lastArgs = args;
				return TypeUtils.getDefaultValue(method.getReturnType());
			}

		});
		lastTarget = service;
		return (S) proxy;
	}

	public <T> void until(T ignore, final Matcher<T> matcher) throws Exception {
		until(new MethodCaller<T>(lastTarget, lastMethod, lastArgs), matcher);
	}

	public <T> void until(final Callable<T> supplier, final Matcher<T> matcher) throws Exception {
		if (supplier == null) {
			throw new IllegalArgumentException("You must specify a supplier (was null).");
		}
		if (matcher == null) {
			throw new IllegalArgumentException("You must specify a matcher (was null).");
		}
		until(new ConditionEvaluator() {
			@Override
			public Boolean call() throws Exception {
				return matcher.matches(supplier.call());
			}
		});
	}

	public <T> void until(Callable<Boolean> conditionEvaluator) throws Exception {
		AwaitConditionImpl condition = new AwaitConditionImpl(timeout, conditionEvaluator, pollInterval);
		if (catchUncaughtExceptions) {
			condition.andCatchAllUncaughtExceptions();
		}
		condition.await();
	}

	static class MethodCaller<T> implements Callable<T> {
		private final Object target;
		private final Method method;
		private final Object[] args;

		public MethodCaller(Object target, Method method, Object[] args) {
			this.target = target;
			this.method = method;
			this.args = args;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T call() throws Exception {
			return (T) method.invoke(target, args);
		}
	}

}
