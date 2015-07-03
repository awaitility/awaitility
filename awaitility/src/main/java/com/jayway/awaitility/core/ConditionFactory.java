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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

/**
 * A factory for creating {@link com.jayway.awaitility.core.Condition} objects. It's not recommended to
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
     * The ignore exceptions.
     */
    private final boolean ignoreExceptions;

    /**
     * The alias.
     */
    private final String alias;

    /**
     * The poll delay.
     */
    private final Duration pollDelay;

    /**
     *
     */
    private final ConditionEvaluationListener conditionEvaluationListener;


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
                            boolean catchUncaughtExceptions, ConditionEvaluationListener conditionEvaluationListener) {
        this(alias, timeout, pollInterval, pollDelay, catchUncaughtExceptions, conditionEvaluationListener, false);
    }

    /**
     * Instantiates a new condition factory.
     *
     * @param alias                   the alias
     * @param timeout                 the timeout
     * @param pollInterval            the poll interval
     * @param pollDelay               The poll delay
     * @param catchUncaughtExceptions the catch uncaught exceptions
     * @param ignoreExceptions        the ignore exceptions
     */
    public ConditionFactory(String alias, Duration timeout, Duration pollInterval, Duration pollDelay,
                            boolean catchUncaughtExceptions, ConditionEvaluationListener conditionEvaluationListener, boolean ignoreExceptions) {
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
        this.conditionEvaluationListener = conditionEvaluationListener;
        this.ignoreExceptions = ignoreExceptions;
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
        this(null, timeout, pollInterval, pollDelay, catchUncaughtExceptions, null);
    }

    /**
     * Instantiates a new condition factory.
     *
     * @param timeout                 the timeout
     * @param pollInterval            the poll interval
     * @param pollDelay               The delay before the polling starts
     * @param catchUncaughtExceptions the catch uncaught exceptions
     */
    public ConditionFactory(Duration timeout, Duration pollInterval, Duration pollDelay, boolean catchUncaughtExceptions, ConditionEvaluationListener conditionEvaluationListener) {
        this(null, timeout, pollInterval, pollDelay, catchUncaughtExceptions, conditionEvaluationListener);
    }

    /**
     * Handle condition evaluation results each time evaluation of a condition occurs. Works only with a Hamcrest matcher-based condition.
     *
     * @param conditionEvaluationListener the condition evaluation listener
     * @return the condition factory
     */
    public ConditionFactory conditionEvaluationListener(ConditionEvaluationListener conditionEvaluationListener) {
        return new ConditionFactory(alias, timeout, pollInterval, pollDelay, catchUncaughtExceptions, conditionEvaluationListener);
    }

    /**
     * Await at most <code>timeout</code> before throwing a timeout exception.
     *
     * @param timeout the timeout
     * @return the condition factory
     */
    public ConditionFactory timeout(Duration timeout) {
        return new ConditionFactory(alias, timeout, pollInterval, pollDelay, catchUncaughtExceptions, conditionEvaluationListener);
    }

    /**
     * Await at most <code>timeout</code> before throwing a timeout exception.
     *
     * @param timeout the timeout
     * @return the condition factory
     */
    public ConditionFactory atMost(Duration timeout) {
        return new ConditionFactory(alias, timeout, pollInterval, pollDelay, catchUncaughtExceptions, conditionEvaluationListener);
    }

    /**
     * Await forever until the condition is satisfied. Caution: You can block
     * subsequent tests and the entire build can hang indefinitely, it's
     * recommended to always use a timeout.
     *
     * @return the condition factory
     */
    public ConditionFactory forever() {
        return new ConditionFactory(alias, Duration.FOREVER, pollInterval, pollDelay, catchUncaughtExceptions, conditionEvaluationListener);
    }

    /**
     * Specify the polling interval Awaitility will use for this await
     * statement. This means the frequency in which the condition is checked for
     * completion.
     * <p>
     * Note that the poll delay will be automatically set as to the same value
     * as the interval unless it's specified explicitly using
     * {@link #pollDelay(Duration)}, {@link #pollDelay(long, TimeUnit)} or
     * {@link com.jayway.awaitility.core.ConditionFactory#pollDelay(com.jayway.awaitility.Duration)}, or
     * ConditionFactory#andWithPollDelay(long, TimeUnit)}.
     * </p>
     *
     * @param pollInterval the poll interval
     * @return the condition factory
     */
    public ConditionFactory pollInterval(Duration pollInterval) {
        return new ConditionFactory(alias, timeout, pollInterval, pollDelay, catchUncaughtExceptions, conditionEvaluationListener);
    }

    /**
     * Await at most <code>timeout</code> before throwing a timeout exception.
     *
     * @param timeout the timeout
     * @param unit    the unit
     * @return the condition factory
     */
    public ConditionFactory timeout(long timeout, TimeUnit unit) {
        return new ConditionFactory(alias, new Duration(timeout, unit), pollInterval, pollDelay,
                catchUncaughtExceptions, conditionEvaluationListener);
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
                catchUncaughtExceptions, conditionEvaluationListener);
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
        return new ConditionFactory(alias, this.timeout, pollInterval, pollDelay, catchUncaughtExceptions, conditionEvaluationListener);
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
                catchUncaughtExceptions, conditionEvaluationListener);
    }

    /**
     * Specify the polling interval Awaitility will use for this await
     * statement. This means the frequency in which the condition is checked for
     * completion.
     * <p>&nbsp;</p>
     * Note that the poll delay will be automatically set as to the same value
     * as the interval unless it's specified explicitly using
     * {@link #pollDelay(Duration)}, {@link #pollDelay(long, TimeUnit)} or
     * {@link com.jayway.awaitility.core.ConditionFactory#pollDelay(com.jayway.awaitility.Duration)} , or
     * ConditionFactory#andWithPollDelay(long, TimeUnit)}.
     *
     * @param pollInterval the poll interval
     * @param unit         the unit
     * @return the condition factory
     */
    public ConditionFactory pollInterval(long pollInterval, TimeUnit unit) {
        return new ConditionFactory(alias, timeout, new Duration(pollInterval, unit), pollDelay,
                catchUncaughtExceptions, conditionEvaluationListener);
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
        return new ConditionFactory(alias, timeout, pollInterval, pollDelay, true, conditionEvaluationListener);
    }

    /**
     * Instruct Awaitility to ignore exceptions, both caught and uncaught, that
     * occur during evaluation. This is useful in situations where the evaluated
     * conditions may temporarily throw exceptions.
     * <p>
     * <em>This effectively rules out the effect of {@link #catchUncaughtExceptions}.</em>
     *
     * @return the condition factory.
     */
    public ConditionFactory ignoreExceptions() {
        return new ConditionFactory(alias, timeout, pollInterval, pollDelay, true, conditionEvaluationListener, true);
    }

    /**
     * Await for an asynchronous operation. This method returns the same
     * {@link com.jayway.awaitility.core.ConditionFactory} instance and is used only to get a more
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
        return new ConditionFactory(alias, timeout, pollInterval, pollDelay, catchUncaughtExceptions, conditionEvaluationListener);
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
     * <p>&nbsp;</p>
     * <pre>
     * await().untilCall(to(orderService).size(), is(greaterThan(2)));
     * </pre>
     *
     * @param <T>     the generic type
     * @param ignore  the return value of the method call
     * @param matcher The condition that must be met when
     * @return a T object.
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
     * Await until a {@link java.util.concurrent.Callable} supplies a value matching the specified
     * {@link org.hamcrest.Matcher}. E.g.
     * <p>&nbsp;</p>
     * <pre>
     * await().until(numberOfPersons(), is(greaterThan(2)));
     * </pre>
     * <p>&nbsp;</p>
     * where "numberOfPersons()" returns a standard {@link java.util.concurrent.Callable}:
     * <p>&nbsp;</p>
     * <pre>
     * private Callable&lt;Integer&gt; numberOfPersons() {
     * 	return new Callable&lt;Integer&gt;() {
     * 		public Integer call() {
     * 			return personRepository.size();
     *        }
     *    };
     * }
     * </pre>
     * <p>&nbsp;</p>
     * Using a generic {@link java.util.concurrent.Callable} as done by using this version of "until"
     * allows you to reuse the "numberOfPersons()" definition in multiple await
     * statements. I.e. you can easily create another await statement (perhaps
     * in a different test case) using e.g.
     * <p>&nbsp;</p>
     * <pre>
     * await().until(numberOfPersons(), is(equalTo(6)));
     * </pre>
     *
     * @param <T>      the generic type
     * @param supplier the supplier that is responsible for getting the value that
     *                 should be matched.
     * @param matcher  the matcher The hamcrest matcher that checks whether the
     *                 condition is fulfilled.
     * @return a T object.
     * @throws com.jayway.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public <T> T until(final Callable<T> supplier, final Matcher<? super T> matcher) {
        return until(new CallableHamcrestCondition<T>(supplier, matcher, generateConditionSettings()));
    }

    /**
     * Await until a {@link java.lang.Runnable} supplier execution passes (ends without throwing an exception). E.g. with Java 8:
     * <p>&nbsp;</p>
     * <pre>
     * await().until(() -&gt; Assertions.assertThat(personRepository.size()).isEqualTo(6));
     * </pre>
     * or
     * <pre>
     * await().until(() -&gt; assertEquals(6, personRepository.size()));
     * </pre>
     * <p>&nbsp;</p>
     * This method is intended to benefit from lambda expressions introduced in Java 8. It allows to use standard AssertJ/FEST Assert assertions
     * (by the way also standard JUnit/TestNG assertions) to test asynchronous calls and systems.
     * <p>&nbsp;</p>
     * {@link java.lang.AssertionError} instances thrown by the supplier are treated as an assertion failure and proper error message is propagated on timeout.
     * Other exceptions are rethrown immediately as an execution errors.
     * <p>&nbsp;</p>
     * Why technically it is completely valid to use plain Runnable class in Java 7 code, the resulting expression is very verbose and can decrease
     * the readability of the test case, e.g.
     * <p>&nbsp;</p>
     * <pre>
     * await().untilPass(new Runnable() {
     *     public void run() {
     *         Assertions.assertThat(personRepository.size()).isEqualTo(6);
     *     }
     * });
     * </pre>
     *
     * @param supplier the supplier that is responsible for executing the assertion and throwing AssertionError on failure.
     * @throws com.jayway.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     * @since 1.6.0
     */
    public void until(final Runnable supplier) {
        until(new AssertionCondition(supplier, generateConditionSettings()));
    }

    /**
     * Await until a Atomic variable has a value matching the specified
     * {@link org.hamcrest.Matcher}. E.g.
     * <p>&nbsp;</p>
     * <pre>
     * await().untilAtomic(myAtomic, is(greaterThan(2)));
     * </pre>
     *
     * @param atomic  the atomic variable
     * @param matcher the matcher The hamcrest matcher that checks whether the
     *                condition is fulfilled.
     * @return a {@link java.lang.Integer} object.
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
     * {@link org.hamcrest.Matcher}. E.g.
     * <p>&nbsp;</p>
     * <pre>
     * await().untilAtomic(myAtomic, is(greaterThan(2)));
     * </pre>
     *
     * @param atomic  the atomic variable
     * @param matcher the matcher The hamcrest matcher that checks whether the
     *                condition is fulfilled.
     * @return a {@link java.lang.Long} object.
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
     * {@link org.hamcrest.Matcher}. E.g.
     * <p>&nbsp;</p>
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
        untilAtomic(atomic, anyOf(is(Boolean.TRUE), is(true)));
    }

    /**
     * Await until a Atomic boolean becomes false.
     *
     * @param atomic the atomic variable
     * @throws com.jayway.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public void untilFalse(final AtomicBoolean atomic) {
        untilAtomic(atomic, anyOf(is(Boolean.FALSE), is(false)));
    }

    /**
     * Await until a Atomic variable has a value matching the specified
     * {@link org.hamcrest.Matcher}. E.g.
     * <p>&nbsp;</p>
     * <pre>
     * await().untilAtomic(myAtomic, is(greaterThan(2)));
     * </pre>
     *
     * @param atomic  the atomic variable
     * @param matcher the matcher The hamcrest matcher that checks whether the
     *                condition is fulfilled.
     * @param <V>     a V object.
     * @return a V object.
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
     * Await until a {@link java.util.concurrent.Callable} returns <code>true</code>. This is method
     * is not as generic as the other variants of "until" but it allows for a
     * more precise and in some cases even more english-like syntax. E.g.
     * <p>&nbsp;</p>
     * <pre>
     * await().until(numberOfPersonsIsEqualToThree());
     * </pre>
     * <p>&nbsp;</p>
     * where "numberOfPersonsIsEqualToThree()" returns a standard
     * {@link java.util.concurrent.Callable} of type {@link java.lang.Boolean}:
     * <p>&nbsp;</p>
     * <pre>
     * private Callable&lt;Boolean&gt; numberOfPersons() {
     * 	return new Callable&lt;Boolean&gt;() {
     * 		public Boolean call() {
     * 			return personRepository.size() == 3;
     *        }
     *    };
     * }
     * </pre>
     *
     * @param <T>                the generic type
     * @param conditionEvaluator the condition evaluator
     * @throws com.jayway.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public <T> void until(Callable<Boolean> conditionEvaluator) {
        until(new CallableCondition(conditionEvaluator, generateConditionSettings()));
    }

    private ConditionSettings generateConditionSettings() {
        return new ConditionSettings(alias, catchUncaughtExceptions, timeout, pollInterval, pollDelay, conditionEvaluationListener,
                ignoreExceptions);
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

    private static boolean isInstanceOf(Object object, String className) {
        try {
            object.getClass().isAssignableFrom(loadClass(className));
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private static Class<?> loadClass(String className) throws ClassNotFoundException {
        return Class.forName(className, false, Thread.currentThread().getContextClassLoader());
    }
}
