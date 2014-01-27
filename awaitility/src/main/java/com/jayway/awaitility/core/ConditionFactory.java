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
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A factory for creating {@link Condition} objects. It's not recommended to
 * instantiate this class directly.
 */
public class ConditionFactory {

    /**
     * The timeout.
     */
    private final Duration timeout;

    /**
     * The poll interval.
     */
    private final Duration pollInterval;

    /**
     * The catch uncaught exceptions.
     */
    private final boolean catchUncaughtExceptions;

    /**
     * The alias.
     */
    private final String alias;

    /**
     * The poll delay.
     */
    private final Duration pollDelay;

    /**
     * Instantiates a new condition factory.
     *
     * @param alias                   the alias
     * @param timeout                 the timeout
     * @param pollInterval            the poll interval
     * @param pollDelay               The poll delay
     * @param catchUncaughtExceptions the catch uncaught exceptions
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

        final long timeoutInMS = timeout.getValueInMS();
        if (!timeout.isForever() && timeoutInMS <= pollInterval.getValueInMS()) {
            throw new IllegalStateException(String.format("Timeout (%s %s) must be greater than the poll interval (%s %s).",
                    timeout.getValue(), timeout.getTimeUnitAsString(), pollInterval.getValue(), pollInterval.getTimeUnitAsString()));
        } else if ((!pollDelay.isForever() && !timeout.isForever()) && timeoutInMS <= pollDelay.getValueInMS()) {
            throw new IllegalStateException(String.format("Timeout (%s %s) must be greater than the poll delay (%s %s).",
                    timeout.getValue(), timeout.getTimeUnitAsString(), pollDelay.getValue(), pollDelay.getTimeUnitAsString()));
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
     * @param timeout                 the timeout
     * @param pollInterval            the poll interval
     * @param pollDelay               The delay before the polling starts
     * @param catchUncaughtExceptions the catch uncaught exceptions
     */
    public ConditionFactory(Duration timeout, Duration pollInterval, Duration pollDelay, boolean catchUncaughtExceptions) {
        this(null, timeout, pollInterval, pollDelay, catchUncaughtExceptions);
    }

    /**
     * Await at most <code>timeout</code> before throwing a timeout exception.
     *
     * @param timeout the timeout
     * @return the condition factory
     */
    public ConditionFactory timeout(Duration timeout) {
        return new ConditionFactory(alias, timeout, pollInterval, pollInterval, catchUncaughtExceptions);
    }

    /**
     * Await at most <code>timeout</code> before throwing a timeout exception.
     *
     * @param timeout the timeout
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
     * {@link #pollDelay(Duration)}, {@link #pollDelay(long, TimeUnit)} or
     * {@link ConditionFactory#pollDelay(com.jayway.awaitility.Duration)}, or
     * ConditionFactory#andWithPollDelay(long, TimeUnit)}.
     * </p>
     *
     * @param pollInterval the poll interval
     * @return the condition factory
     */
    public ConditionFactory pollInterval(Duration pollInterval) {
        return new ConditionFactory(alias, timeout, pollInterval, pollInterval, catchUncaughtExceptions);
    }

    /**
     * Await at most <code>timeout</code> before throwing a timeout exception.
     *
     * @param timeout the timeout
     * @param unit    the unit
     * @return the condition factory
     */
    public ConditionFactory timeout(long timeout, TimeUnit unit) {
        return new ConditionFactory(alias, new Duration(timeout, unit), pollInterval, pollInterval,
                catchUncaughtExceptions);
    }

    /**
     * Specify the delay that will be used before Awaitility starts polling for
     * the result the first time. If you don't specify a poll delay explicitly
     * it'll be the same as the poll interval.
     *
     * @param delay the delay
     * @param unit  the unit
     * @return the condition factory
     */
    public ConditionFactory pollDelay(long delay, TimeUnit unit) {
        return new ConditionFactory(alias, this.timeout, pollInterval, new Duration(delay, unit),
                catchUncaughtExceptions);
    }

    /**
     * Specify the delay that will be used before Awaitility starts polling for
     * the result the first time. If you don't specify a poll delay explicitly
     * it'll be the same as the poll interval.
     *
     * @param pollDelay the poll delay
     * @return the condition factory
     */
    public ConditionFactory pollDelay(Duration pollDelay) {
        return new ConditionFactory(alias, this.timeout, pollInterval, pollDelay, catchUncaughtExceptions);
    }

    /**
     * Await at most <code>timeout</code> before throwing a timeout exception.
     *
     * @param timeout the timeout
     * @param unit    the unit
     * @return the condition factory
     */
    public ConditionFactory atMost(long timeout, TimeUnit unit) {
        return new ConditionFactory(alias, new Duration(timeout, unit), pollInterval, pollDelay,
                catchUncaughtExceptions);
    }

    /**
     * Specify the polling interval Awaitility will use for this await
     * statement. This means the frequency in which the condition is checked for
     * completion.
     * <p/>
     * <p>
     * Note that the poll delay will be automatically set as to the same value
     * as the interval unless it's specified explicitly using
     * {@link #pollDelay(Duration)}, {@link #pollDelay(long, TimeUnit)} or
     * {@link ConditionFactory#pollDelay(com.jayway.awaitility.Duration)} , or
     * ConditionFactory#andWithPollDelay(long, TimeUnit)}.
     * </p>
     *
     * @param pollInterval the poll interval
     * @param unit         the unit
     * @return the condition factory
     */
    public ConditionFactory pollInterval(long pollInterval, TimeUnit unit) {
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
    public ConditionFactory catchUncaughtExceptions() {
        return new ConditionFactory(alias, timeout, pollInterval, pollInterval, true);
    }

    /**
     * Await for an asynchronous operation. This method returns the same
     * {@link ConditionFactory} instance and is used only to get a more
     * fluent-like syntax.
     *
     * @return the condition factory
     */
    public ConditionFactory await() {
        return this;
    }

    /**
     * Await for an asynchronous operation and give this await instance a
     * particular name. This is useful in cases when you have several await
     * statements in one test and you want to know which one that fails (the
     * alias will be shown if a timeout exception occurs).
     *
     * @param alias the alias
     * @return the condition factory
     */
    public ConditionFactory await(String alias) {
        return new ConditionFactory(alias, timeout, pollInterval, pollInterval, catchUncaughtExceptions);
    }

    /**
     * A method to increase the readability of the Awaitility DSL. It simply
     * returns the same condition factory instance.
     *
     * @return the condition factory
     */
    public ConditionFactory and() {
        return this;
    }

    /**
     * A method to increase the readability of the Awaitility DSL. It simply
     * returns the same condition factory instance.
     *
     * @return the condition factory
     */
    public ConditionFactory with() {
        return this;
    }

    /**
     * A method to increase the readability of the Awaitility DSL. It simply
     * returns the same condition factory instance.
     *
     * @return the condition factory
     */
    public ConditionFactory then() {
        return this;
    }

    /**
     * A method to increase the readability of the Awaitility DSL. It simply
     * returns the same condition factory instance.
     *
     * @return the condition factory
     */
    public ConditionFactory given() {
        return this;
    }

    /**
     * Don't catch uncaught exceptions in other threads. This will <i>not</i>
     * make the await statement fail if exceptions occur in other threads.
     *
     * @return the condition factory
     */
    public ConditionFactory dontCatchUncaughtExceptions() {
        return new ConditionFactory(timeout, pollInterval, pollDelay, false);
    }

    /**
     * Specify the condition that must be met when waiting for a method call.
     * E.g.
     * <p/>
     * <pre>
     * await().untilCall(to(orderService).size(), is(greaterThan(2)));
     * </pre>
     *
     * @param <T>     the generic type
     * @param ignore  the return value of the method call
     * @param matcher The condition that must be met when
     * @throws com.jayway.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public <T> T untilCall(T ignore, final Matcher<? super T> matcher) {
        final MethodCaller<T> supplier = new MethodCaller<T>(MethodCallRecorder.getLastTarget(), MethodCallRecorder
                .getLastMethod(), MethodCallRecorder.getLastArgs());
        MethodCallRecorder.reset();
        final ProxyHamcrestCondition<T> proxyCondition = new ProxyHamcrestCondition<T>(supplier, matcher, generateConditionSettings());
        return until(proxyCondition);
    }

    /**
     * Await until a {@link Callable} supplies a value matching the specified
     * {@link Matcher}. E.g.
     * <p/>
     * <pre>
     * await().until(numberOfPersons(), is(greaterThan(2)));
     * </pre>
     * <p/>
     * where "numberOfPersons()" returns a standard {@link Callable}:
     * <p/>
     * <pre>
     * private Callable&lt;Integer&gt; numberOfPersons() {
     * 	return new Callable&lt;Integer&gt;() {
     * 		public Integer call() {
     * 			return personRepository.size();
     *        }
     *    };
     * }
     * </pre>
     * <p/>
     * Using a generic {@link Callable} as done by using this version of "until"
     * allows you to reuse the "numberOfPersons()" definition in multiple await
     * statements. I.e. you can easily create another await statement (perhaps
     * in a different test case) using e.g.
     * <p/>
     * <pre>
     * await().until(numberOfPersons(), is(equalTo(6)));
     * </pre>
     *
     * @param <T>      the generic type
     * @param supplier the supplier that is responsible for getting the value that
     *                 should be matched.
     * @param matcher  the matcher The hamcrest matcher that checks whether the
     *                 condition is fulfilled.
     * @throws com.jayway.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public <T> T until(final Callable<T> supplier, final Matcher<? super T> matcher) {
        return until(new CallableHamcrestCondition<T>(supplier, matcher, generateConditionSettings()));
    }

    /**
     * Await until a Atomic variable has a value matching the specified
     * {@link Matcher}. E.g.
     * <p/>
     * <pre>
     * await().untilAtomic(myAtomic, is(greaterThan(2)));
     * </pre>
     *
     * @param atomic  the atomic variable
     * @param matcher the matcher The hamcrest matcher that checks whether the
     *                condition is fulfilled.
     * @throws com.jayway.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public Integer untilAtomic(final AtomicInteger atomic, final Matcher<? super Integer> matcher) {
        return until(new CallableHamcrestCondition<Integer>(new Callable<Integer>() {
            public Integer call() {
                return atomic.get();
            }
        }, matcher, generateConditionSettings()));
    }

    /**
     * Await until a Atomic variable has a value matching the specified
     * {@link Matcher}. E.g.
     * <p/>
     * <pre>
     * await().untilAtomic(myAtomic, is(greaterThan(2)));
     * </pre>
     *
     * @param atomic  the atomic variable
     * @param matcher the matcher The hamcrest matcher that checks whether the
     *                condition is fulfilled.
     * @throws com.jayway.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public Long untilAtomic(final AtomicLong atomic, final Matcher<? super Long> matcher) {
        return until(new CallableHamcrestCondition<Long>(new Callable<Long>() {
            public Long call() {
                return atomic.get();
            }
        }, matcher, generateConditionSettings()));
    }

    /**
     * Await until a Atomic variable has a value matching the specified
     * {@link Matcher}. E.g.
     * <p/>
     * <pre>
     * await().untilAtomic(myAtomic, is(greaterThan(2)));
     * </pre>
     *
     * @param atomic  the atomic variable
     * @param matcher the matcher The hamcrest matcher that checks whether the
     *                condition is fulfilled.
     * @throws com.jayway.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public void untilAtomic(final AtomicBoolean atomic, final Matcher<? super Boolean> matcher) {
        until(new CallableHamcrestCondition<Boolean>(new Callable<Boolean>() {
            public Boolean call() {
                return atomic.get();
            }
        }, matcher, generateConditionSettings()));
    }

    /**
     * Await until a Atomic boolean becomes true.
     *
     * @param atomic the atomic variable
     * @throws com.jayway.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public void untilTrue(final AtomicBoolean atomic) {
        untilAtomic(atomic, Matchers.is(Boolean.TRUE));
    }

    /**
     * Await until a Atomic boolean becomes false.
     *
     * @param atomic the atomic variable
     * @throws com.jayway.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public void untilFalse(final AtomicBoolean atomic) {
        untilAtomic(atomic, Matchers.is(Boolean.FALSE));
    }

    /**
     * Await until a Atomic variable has a value matching the specified
     * {@link Matcher}. E.g.
     * <p/>
     * <pre>
     * await().untilAtomic(myAtomic, is(greaterThan(2)));
     * </pre>
     *
     * @param atomic  the atomic variable
     * @param matcher the matcher The hamcrest matcher that checks whether the
     *                condition is fulfilled.
     * @throws com.jayway.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public <V> V untilAtomic(final AtomicReference<V> atomic, final Matcher<? super V> matcher) {
        return until(new CallableHamcrestCondition<V>(new Callable<V>() {
            public V call() {
                return atomic.get();
            }
        }, matcher, generateConditionSettings()));
    }

    /**
     * Await until a {@link Callable} returns <code>true</code>. This is method
     * is not as generic as the other variants of "until" but it allows for a
     * more precise and in some cases even more english-like syntax. E.g.
     * <p/>
     * <pre>
     * await().until(numberOfPersonsIsEqualToThree());
     * </pre>
     * <p/>
     * where "numberOfPersonsIsEqualToThree()" returns a standard
     * {@link Callable} of type {@link Boolean}:
     * <p/>
     * <pre>
     * private Callable&lt;Boolean&gt; numberOfPersons() {
     * 	return new Callable&lt;Boolean&gt;() {
     * 		public Boolean call() {
     * 			return personRepository.size() == 3;
     *        }
     *    };
     * }
     *
     * @param <T>                the generic type
     * @param conditionEvaluator the condition evaluator
     * @throws com.jayway.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public <T> void until(Callable<Boolean> conditionEvaluator) {
        until(new CallableCondition(conditionEvaluator, generateConditionSettings()));
    }

    private ConditionSettings generateConditionSettings() {
        return new ConditionSettings(alias, catchUncaughtExceptions, timeout, pollInterval, pollDelay);
    }

    private <T> T until(Condition<T> condition) {
        return condition.await();
    }

    /**
     * The Class MethodCaller.
     *
     * @param <T> the generic type
     */
    static class MethodCaller<T> implements Callable<T> {

        /**
         * The target.
         */
        final Object target;

        /**
         * The method.
         */
        final Method method;

        /**
         * The args.
         */
        final Object[] args;

        /**
         * Instantiates a new method caller.
         *
         * @param target the target
         * @param method the method
         * @param args   the args
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
        public T call() {
            try {
                return (T) method.invoke(target, args);
            } catch (IllegalAccessException e) {
                return SafeExceptionRethrower.safeRethrow(e);
            } catch (InvocationTargetException e) {
                return SafeExceptionRethrower.safeRethrow(e.getCause());
            }
        }
    }
}
