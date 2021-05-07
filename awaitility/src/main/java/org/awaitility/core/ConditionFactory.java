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
package org.awaitility.core;

import org.awaitility.constraint.AtMostWaitConstraint;
import org.awaitility.constraint.WaitConstraint;
import org.awaitility.pollinterval.FixedPollInterval;
import org.awaitility.pollinterval.PollInterval;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.awaitility.core.ForeverDuration.isForever;
import static org.awaitility.core.TemporalDuration.formatAsString;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

/**
 * A factory for creating {@link org.awaitility.core.Condition} objects. It's not recommended to
 * instantiate this class directly.
 */
public class ConditionFactory {

    /**
     * Timing constraint.
     */
    private final WaitConstraint timeoutConstraint;

    /**
     * The poll interval.
     */
    private final PollInterval pollInterval;

    /**
     * The catch uncaught exceptions.
     */
    private final boolean catchUncaughtExceptions;

    /**
     * The ignore exceptions.
     */
    private final ExceptionIgnorer exceptionsIgnorer;

    /**
     * The alias.
     */
    private final String alias;

    /**
     * The poll delay.
     */
    private final Duration pollDelay;

    /**
     * The condition evaluation listener
     */
    private final ConditionEvaluationListener conditionEvaluationListener;

    /**
     * The executor lifecycle
     */
    private final ExecutorLifecycle executorLifecycle;

    /**
     * If this condition if ever false, indicates our condition will never be true.
     */
    private final FailFastCondition failFastCondition;

    /**
     * Instantiates a new condition factory.
     *
     * @param alias                       the alias
     * @param timeoutConstraint           the timeout constraint
     * @param pollInterval                the poll interval
     * @param pollDelay                   The poll delay
     * @param catchUncaughtExceptions     the catch uncaught exceptions
     * @param exceptionsIgnorer           Determine which exceptions that should ignored
     * @param conditionEvaluationListener Determine which exceptions that should ignored
     * @param executorLifecycle           The executor service and the lifecycle of the executor service that'll be used to evaluate the condition during polling
     * @param failFastCondition           If this condition if ever false, indicates our condition will never be true.
     */
    public ConditionFactory(final String alias, WaitConstraint timeoutConstraint, PollInterval pollInterval, Duration pollDelay,
                            boolean catchUncaughtExceptions, ExceptionIgnorer exceptionsIgnorer,
                            ConditionEvaluationListener conditionEvaluationListener, ExecutorLifecycle executorLifecycle, final FailFastCondition failFastCondition) {
        if (pollInterval == null) {
            throw new IllegalArgumentException("pollInterval cannot be null");
        }
        if (timeoutConstraint == null) {
            throw new IllegalArgumentException("timeout cannot be null");
        }

        this.alias = alias;
        this.timeoutConstraint = timeoutConstraint;
        this.pollInterval = pollInterval;
        this.catchUncaughtExceptions = catchUncaughtExceptions;
        this.pollDelay = pollDelay;
        this.conditionEvaluationListener = conditionEvaluationListener;
        this.exceptionsIgnorer = exceptionsIgnorer;
        this.executorLifecycle = executorLifecycle;
        this.failFastCondition = failFastCondition;
    }

    /**
     * Handle condition evaluation results each time evaluation of a condition occurs. Works only with a Hamcrest matcher-based condition.
     *
     * @param conditionEvaluationListener the condition evaluation listener
     * @return the condition factory
     */
    public ConditionFactory conditionEvaluationListener(ConditionEvaluationListener conditionEvaluationListener) {
        return new ConditionFactory(alias, timeoutConstraint, pollInterval, pollDelay, catchUncaughtExceptions,
                exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition);
    }

    /**
     * Await at most <code>timeout</code> before throwing a timeout exception.
     *
     * @param timeout the timeout
     * @return the condition factory
     */
    public ConditionFactory timeout(Duration timeout) {
        return atMost(timeout);
    }

    /**
     * Await at most <code>timeout</code> before throwing a timeout exception.
     *
     * @param timeout the timeout
     * @return the condition factory
     */
    public ConditionFactory atMost(Duration timeout) {
        return new ConditionFactory(alias, timeoutConstraint.withMaxWaitTime(timeout), pollInterval, pollDelay,
                catchUncaughtExceptions, exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition);
    }

    /**
     * Await at the predicate holds during at least <code>timeout</code>
     *
     * @param timeout the timeout
     * @return the condition factory
     */
    public ConditionFactory during(Duration timeout) {
        return new ConditionFactory(alias, timeoutConstraint.withHoldPredicateTime(timeout), pollInterval, pollDelay,
                catchUncaughtExceptions, exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition);
    }

    /**
     * Await at the predicate holds during at least <code>timeout</code>
     *
     * @param timeout the timeout
     * @param unit    the unit
     * @return the condition factory
     */
    public ConditionFactory during(long timeout, TimeUnit unit) {
        return during(DurationFactory.of(timeout, unit));
    }

    /**
     * Set the alias
     *
     * @param alias alias
     * @return the condition factory
     * @see org.awaitility.Awaitility#await(String)
     */
    public ConditionFactory alias(String alias) {
        return new ConditionFactory(alias, timeoutConstraint, pollInterval, pollDelay,
                catchUncaughtExceptions, exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition);
    }

    /**
     * Condition has to be evaluated not earlier than <code>timeout</code> before throwing a timeout exception.
     *
     * @param timeout the timeout
     * @return the condition factory
     */
    public ConditionFactory atLeast(Duration timeout) {
        return new ConditionFactory(alias, timeoutConstraint.withMinWaitTime(timeout), pollInterval, pollDelay,
                catchUncaughtExceptions, exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition);
    }

    /**
     * Condition has to be evaluated not earlier than <code>timeout</code> before throwing a timeout exception.
     *
     * @param timeout the timeout
     * @param unit    the unit
     * @return the condition factory
     */
    public ConditionFactory atLeast(long timeout, TimeUnit unit) {
        return atLeast(DurationFactory.of(timeout, unit));
    }

    /**
     * Specifies the duration window which has to be satisfied during operation execution. In case operation is executed
     * before <code>atLeast</code> or after <code>atMost</code> timeout exception is thrown.
     *
     * @param atLeast lower part of execution window
     * @param atMost  upper part of execution window
     * @return the condition factory
     */
    public ConditionFactory between(Duration atLeast, Duration atMost) {
        return atLeast(atLeast).and().atMost(atMost);
    }

    /**
     * Specifies the duration window which has to be satisfied during operation execution. In case operation is executed
     * before <code>atLeastDuration</code> or after <code>atMostDuration</code> timeout exception is thrown.
     *
     * @param atLeastDuration lower part of execution window
     * @param atMostDuration  upper part of execution window
     * @return the condition factory
     */
    public ConditionFactory between(long atLeastDuration, TimeUnit atLeastTimeUnit, long atMostDuration, TimeUnit atMostTimeUnit) {
        return between(DurationFactory.of(atLeastDuration, atLeastTimeUnit), DurationFactory.of(atMostDuration, atMostTimeUnit));
    }

    /**
     * Await forever until the condition is satisfied. Caution: You can block
     * subsequent tests and the entire build can hang indefinitely, it's
     * recommended to always use a timeout.
     *
     * @return the condition factory
     */
    public ConditionFactory forever() {
        return new ConditionFactory(alias, AtMostWaitConstraint.FOREVER, pollInterval, pollDelay,
                catchUncaughtExceptions, exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition);
    }

    /**
     * Specify the polling interval Awaitility will use for this await
     * statement. This means the frequency in which the condition is checked for
     * completion.
     * <p>
     * Note that the poll delay will be automatically set as to the same value
     * as the interval (if using a {@link FixedPollInterval}) unless it's specified explicitly using
     * {@link #pollDelay(Duration)}, {@link #pollDelay(long, TimeUnit)} or
     * {@link org.awaitility.core.ConditionFactory#pollDelay(java.time.Duration)}.
     * </p>
     *
     * @param pollInterval the poll interval
     * @return the condition factory
     */
    public ConditionFactory pollInterval(Duration pollInterval) {
        return new ConditionFactory(alias, timeoutConstraint, new FixedPollInterval(pollInterval), pollDelay, catchUncaughtExceptions,
                exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition);
    }

    /**
     * Await at most <code>timeout</code> before throwing a timeout exception.
     *
     * @param timeout the timeout
     * @param unit    the unit
     * @return the condition factory
     */
    public ConditionFactory timeout(long timeout, TimeUnit unit) {
        return atMost(timeout, unit);
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
        return new ConditionFactory(alias, timeoutConstraint, pollInterval, DurationFactory.of(delay, unit),
                catchUncaughtExceptions, exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition);
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
        if (pollDelay == null) {
            throw new IllegalArgumentException("pollDelay cannot be null");
        }
        return new ConditionFactory(alias, timeoutConstraint, pollInterval, pollDelay, catchUncaughtExceptions,
                exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition);
    }

    /**
     * Await at most <code>timeout</code> before throwing a timeout exception.
     *
     * @param timeout the timeout
     * @param unit    the unit
     * @return the condition factory
     */
    public ConditionFactory atMost(long timeout, TimeUnit unit) {
        return atMost(DurationFactory.of(timeout, unit));
    }

    /**
     * Specify the polling interval Awaitility will use for this await
     * statement. This means the frequency in which the condition is checked for
     * completion.
     * <p>&nbsp;</p>
     * Note that the poll delay will be automatically set as to the same value
     * as the interval unless it's specified explicitly using
     * {@link #pollDelay(Duration)}, {@link #pollDelay(long, TimeUnit)} or
     * {@link org.awaitility.core.ConditionFactory#pollDelay(java.time.Duration)} , or
     * ConditionFactory#andWithPollDelay(long, TimeUnit)}. This is the same as creating a {@link FixedPollInterval}.
     *
     * @param pollInterval the poll interval
     * @param unit         the unit
     * @return the condition factory
     * @see FixedPollInterval
     */
    public ConditionFactory pollInterval(long pollInterval, TimeUnit unit) {
        PollInterval fixedPollInterval = new FixedPollInterval(DurationFactory.of(pollInterval, unit));
        return new ConditionFactory(alias, timeoutConstraint, fixedPollInterval, definePollDelay(pollDelay, fixedPollInterval),
                catchUncaughtExceptions, exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition);
    }

    public ConditionFactory pollInterval(PollInterval pollInterval) {
        return new ConditionFactory(alias, timeoutConstraint, pollInterval, definePollDelay(pollDelay, pollInterval), catchUncaughtExceptions,
                exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition);
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
        return new ConditionFactory(alias, timeoutConstraint, pollInterval, pollDelay, true, exceptionsIgnorer,
                conditionEvaluationListener, executorLifecycle, failFastCondition);
    }

    /**
     * Instruct Awaitility to ignore exceptions instance of the supplied exceptionType type.
     * Exceptions will be treated as evaluating to <code>false</code>.
     * This is useful in situations where the evaluated conditions may temporarily throw exceptions.
     * <p/>
     * <p>If you want to ignore a specific exceptionType then use {@link #ignoreException(Class)}</p>
     *
     * @param exceptionType The exception type (hierarchy) to ignore
     * @return the condition factory
     */
    public ConditionFactory ignoreExceptionsInstanceOf(final Class<? extends Throwable> exceptionType) {
        if (exceptionType == null) {
            throw new IllegalArgumentException("exceptionType cannot be null");
        }
        return new ConditionFactory(alias, timeoutConstraint, pollInterval, pollDelay, catchUncaughtExceptions,
                new PredicateExceptionIgnorer(e -> exceptionType.isAssignableFrom(e.getClass())),
                conditionEvaluationListener, executorLifecycle, failFastCondition);
    }

    /**
     * Instruct Awaitility to ignore a specific exception and <i>no</i> subclasses of this exception.
     * Exceptions will be treated as evaluating to <code>false</code>.
     * This is useful in situations where the evaluated conditions may temporarily throw exceptions.
     * <p>If you want to ignore a subtypes of this exception then use {@link #ignoreExceptionsInstanceOf(Class)}} </p>
     *
     * @param exceptionType The exception type to ignore
     * @return the condition factory
     */
    public ConditionFactory ignoreException(final Class<? extends Throwable> exceptionType) {
        if (exceptionType == null) {
            throw new IllegalArgumentException("exception cannot be null");
        }
        return new ConditionFactory(alias, timeoutConstraint, pollInterval, pollDelay, catchUncaughtExceptions,
                new PredicateExceptionIgnorer(e -> e.getClass().equals(exceptionType)),
                conditionEvaluationListener, executorLifecycle, failFastCondition);
    }

    /**
     * Instruct Awaitility to ignore <i>all</i> exceptions that occur during evaluation.
     * Exceptions will be treated as evaluating to
     * <code>false</code>. This is useful in situations where the evaluated
     * conditions may temporarily throw exceptions.
     *
     * @return the condition factory.
     */
    public ConditionFactory ignoreExceptions() {
        return ignoreExceptionsMatching(e -> true);
    }

    /**
     * Instruct Awaitility to not ignore any exceptions that occur during evaluation.
     * This is only useful if Awaitility is configured to ignore exceptions by default but you want to
     * have a different behavior for a single test case.
     *
     * @return the condition factory.
     */
    public ConditionFactory ignoreNoExceptions() {
        return ignoreExceptionsMatching(e -> false);
    }

    /**
     * Instruct Awaitility to ignore exceptions that occur during evaluation and matches the supplied Hamcrest matcher.
     * Exceptions will be treated as evaluating to
     * <code>false</code>. This is useful in situations where the evaluated conditions may temporarily throw exceptions.
     *
     * @return the condition factory.
     */
    public ConditionFactory ignoreExceptionsMatching(Matcher<? super Throwable> matcher) {
        return new ConditionFactory(alias, timeoutConstraint, pollInterval, pollDelay, catchUncaughtExceptions,
                new HamcrestExceptionIgnorer(matcher), conditionEvaluationListener, executorLifecycle, failFastCondition);
    }

    /**
     * Instruct Awaitility to ignore exceptions that occur during evaluation and matches the supplied <code>predicate</code>.
     * Exceptions will be treated as evaluating to
     * <code>false</code>. This is useful in situations where the evaluated conditions may temporarily throw exceptions.
     *
     * @return the condition factory.
     */
    public ConditionFactory ignoreExceptionsMatching(Predicate<? super Throwable> predicate) {
        return new ConditionFactory(alias, timeoutConstraint, pollInterval, pollDelay, catchUncaughtExceptions,
                new PredicateExceptionIgnorer(predicate), conditionEvaluationListener, executorLifecycle, failFastCondition);
    }

    /**
     * Await for an asynchronous operation. This method returns the same
     * {@link org.awaitility.core.ConditionFactory} instance and is used only to get a more
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
        return new ConditionFactory(alias, timeoutConstraint, pollInterval, pollDelay, catchUncaughtExceptions,
                exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition);
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
        return new ConditionFactory(alias, timeoutConstraint, pollInterval, pollDelay, false,
                exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, failFastCondition);
    }

    /**
     * Specify the executor service whose threads will be used to evaluate the poll condition in Awaitility.
     * Note that the executor service must be shutdown manually!
     * <p>
     * This is an advanced feature and it should only be used sparingly.
     *
     * @param executorService The executor service that Awaitility will use when polling condition evaluations
     * @return the condition factory
     */
    public ConditionFactory pollExecutorService(ExecutorService executorService) {
        if (executorService instanceof ScheduledExecutorService) {
            throw new IllegalArgumentException("Poll executor service cannot be an instance of " + ScheduledExecutorService.class.getName());
        }
        return new ConditionFactory(alias, timeoutConstraint, pollInterval, pollDelay, false,
                exceptionsIgnorer, conditionEvaluationListener, ExecutorLifecycle.withoutCleanup(executorService), failFastCondition);
    }

    /**
     * Specify a thread supplier whose thread will be used to evaluate the poll condition in Awaitility.
     * The supplier will be called only once and the thread it returns will be reused during all condition evaluations.
     * This is an advanced feature and it should only be used sparingly.
     *
     * @param threadSupplier A supplier of the thread that Awaitility will use when polling
     * @return the condition factory
     */
    public ConditionFactory pollThread(final Function<Runnable, Thread> threadSupplier) {
        return new ConditionFactory(alias, timeoutConstraint, pollInterval, pollDelay, false,
                exceptionsIgnorer, conditionEvaluationListener, ExecutorLifecycle.withNormalCleanupBehavior(() -> InternalExecutorServiceFactory.create(threadSupplier)), failFastCondition);
    }

    /**
     * Instructs Awaitility to execute the polling of the condition from the same as the test.
     * This is an advanced feature and you should be careful when combining this with conditions that
     * wait forever (or a long time) since Awaitility cannot interrupt the thread when it's using the same
     * thread as the test. For safety you should always combine tests using this feature with a test framework specific timeout,
     * for example in JUnit:
     * <pre>
     * @Test(timeout = 2000L)
     * public void myTest() {
     *     Awaitility.pollInSameThread();
     *     await().forever().until(...);
     * }
     * </pre>
     *
     * @return the condition factory
     */
    public ConditionFactory pollInSameThread() {
        return new ConditionFactory(alias, timeoutConstraint, pollInterval, pollDelay, false,
                exceptionsIgnorer, conditionEvaluationListener, ExecutorLifecycle.withNormalCleanupBehavior(InternalExecutorServiceFactory::sameThreadExecutorService), failFastCondition);
    }

    /**
     * If the supplied Callable <i>ever</i> returns false, it indicates our condition will <i>never</i> be true, and if so fail the system immediately.
     * Throws a {@link TerminalFailureException} if fail fast condition evaluates to <code>true</code>. If you want to specify a more descriptive error message
     * then use {@link #failFast(String, Callable)}.
     *
     * @param failFastCondition The terminal failure condition
     * @return the condition factory
     * @see #failFast(String, Callable)
     */
    public ConditionFactory failFast(Callable<Boolean> failFastCondition) {
        if (failFastCondition == null) {
            throw new IllegalArgumentException("failFastCondition cannot be null");
        }
        return new ConditionFactory(alias, timeoutConstraint, pollInterval, pollDelay, catchUncaughtExceptions,
                exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, new FailFastCondition(null, failFastCondition));
    }

    /**
     * If the supplied Callable <i>ever</i> returns false, it indicates our condition will <i>never</i> be true, and if so fail the system immediately.
     * Throws a {@link TerminalFailureException} if fail fast condition evaluates to <code>true</code>.
     *
     * @param failFastCondition The terminal failure condition
     * @param failFastFailureReason     A descriptive reason why the fail fast condition has failed, will be included in the {@link TerminalFailureException} thrown if <code>failFastCondition</code> evaluates to <code>true</code>.
     * @return the condition factory
     */
    public ConditionFactory failFast(String failFastFailureReason, Callable<Boolean> failFastCondition) {
        if (failFastCondition == null) {
            throw new IllegalArgumentException("failFastCondition cannot be null");
        } else if (failFastFailureReason == null) {
            throw new IllegalArgumentException("failFastFailureReason cannot be null");
        }

        return new ConditionFactory(alias, timeoutConstraint, pollInterval, pollDelay, catchUncaughtExceptions,
                exceptionsIgnorer, conditionEvaluationListener, executorLifecycle, new FailFastCondition(failFastFailureReason, failFastCondition));
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
     * @throws org.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public <T> T until(final Callable<T> supplier, final Matcher<? super T> matcher) {
        return until(new CallableHamcrestCondition<>(supplier, matcher, generateConditionSettings()));
    }

    /**
     * Wait until the given supplier matches the supplied predicate. For example:
     *
     * <pre>
     * await().until(myRepository::count, cnt -> cnt == 2);
     * </pre>
     *
     * @param supplier  The supplier that returns the object that will be evaluated by the predicate.
     * @param predicate The predicate that must match
     * @param <T>       the generic type
     * @return a T object.
     * @since 3.1.1
     */
    public <T> T until(final Callable<T> supplier, final Predicate<? super T> predicate) {
        return until(supplier, new TypeSafeMatcher<T>() {
            @Override
            protected void describeMismatchSafely(T item, Description description) {
                description.appendText("it returned <false> for input of ").appendValue(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("the predicate to return <true>");
            }

            @Override
            protected boolean matchesSafely(T item) {
                return predicate.test(item);
            }
        });
    }

    /**
     * Await until a {@link java.lang.Runnable} supplier execution passes (ends without throwing an exception). E.g. with Java 8:
     * <p>&nbsp;</p>
     * <pre>
     * await().untilAsserted(() -&gt; Assertions.assertThat(personRepository.size()).isEqualTo(6));
     * </pre>
     * or
     * <pre>
     * await().untilAsserted(() -&gt; assertEquals(6, personRepository.size()));
     * </pre>
     * <p>&nbsp;</p>
     * This method is intended to benefit from lambda expressions introduced in Java 8. It allows to use standard AssertJ/FEST Assert assertions
     * (by the way also standard JUnit/TestNG assertions) to test asynchronous calls and systems.
     * <p>&nbsp;</p>
     * {@link java.lang.AssertionError} instances thrown by the supplier are treated as an assertion failure and proper error message is propagated on timeout.
     * Other exceptions are rethrown immediately as an execution errors.
     * <p>&nbsp;</p>
     * While technically it is completely valid to use plain Runnable class in Java 7 code, the resulting expression is very verbose and can decrease
     * the readability of the test case, e.g.
     * <p>&nbsp;</p>
     * <pre>
     * await().untilAsserted(new Runnable() {
     *     public void run() {
     *         Assertions.assertThat(personRepository.size()).isEqualTo(6);
     *     }
     * });
     * </pre>
     * <p>&nbsp;</p>
     * <b>NOTE:</b><br>
     * Be <i>VERY</i> careful so that you're not using this method incorrectly in languages (like Kotlin and Groovy) that doesn't
     * disambiguate between a {@link ThrowingRunnable} that doesn't return anything (void) and {@link Callable} that returns a value.
     * For example in Kotlin you can do like this:
     * <p>&nbsp;</p>
     * <pre>
     * await().untilAsserted { true == false }
     * </pre>
     * and the compiler won't complain with an error (as is the case in Java). If you were to execute this test in Kotlin it'll pass!
     *
     * @param assertion the supplier that is responsible for executing the assertion and throwing AssertionError on failure.
     * @throws org.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     * @since 1.6.0
     */
    public void untilAsserted(final ThrowingRunnable assertion) {
        until(new AssertionCondition(assertion, generateConditionSettings()));
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
     * @throws org.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public Integer untilAtomic(final AtomicInteger atomic, final Matcher<? super Integer> matcher) {
        return until(new CallableHamcrestCondition<>(new Callable<Integer>() {
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
     * @throws org.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public Long untilAtomic(final AtomicLong atomic, final Matcher<? super Long> matcher) {
        return until(new CallableHamcrestCondition<>(atomic::get, matcher, generateConditionSettings()));
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
     * @throws org.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public void untilAtomic(final AtomicBoolean atomic, final Matcher<? super Boolean> matcher) {
        until(new CallableHamcrestCondition<>(atomic::get, matcher, generateConditionSettings()));
    }

    /**
     * Await until a Atomic boolean becomes true.
     *
     * @param atomic the atomic variable
     * @throws org.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public void untilTrue(final AtomicBoolean atomic) {
        untilAtomic(atomic, anyOf(is(Boolean.TRUE), is(true)));
    }

    /**
     * Await until a Atomic boolean becomes false.
     *
     * @param atomic the atomic variable
     * @throws org.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public void untilFalse(final AtomicBoolean atomic) {
        untilAtomic(atomic, anyOf(is(Boolean.FALSE), is(false)));
    }

    /**
     * Await until a {@link LongAdder} has a value matching the specified {@link org.hamcrest.Matcher}. E.g.
     * <p>&nbsp;</p>
     * <pre>
     * await().untilAdder(myLongAdder, is(greaterThan(2L)));
     * </pre>
     *
     * @param adder   the {@link LongAdder} variable
     * @param matcher the matcher The hamcrest matcher that checks whether the condition is fulfilled.
     * @throws org.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public void untilAdder(final LongAdder adder, final Matcher<? super Long> matcher) {
        until(new CallableHamcrestCondition<>(adder::longValue, matcher, generateConditionSettings()));
    }

    /**
     * Await until a {@link DoubleAdder} has a value matching the specified {@link org.hamcrest.Matcher}. E.g.
     * <p>&nbsp;</p>
     * <pre>
     * await().untilAdder(myDoubleAdder, is(greaterThan(2.0d)));
     * </pre>
     *
     * @param adder   the {@link DoubleAdder} variable
     * @param matcher the matcher The hamcrest matcher that checks whether the condition is fulfilled.
     * @throws org.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public void untilAdder(final DoubleAdder adder, final Matcher<? super Double> matcher) {
        until(new CallableHamcrestCondition<>(adder::doubleValue, matcher, generateConditionSettings()));
    }

    /**
     * Await until a {@link LongAccumulator} has a value matching the specified {@link org.hamcrest.Matcher}. E.g.
     * <p>&nbsp;</p>
     * <pre>
     * await().untilAccumulator(myLongAccumulator, is(greaterThan(2L)));
     * </pre>
     *
     * @param accumulator the {@link LongAccumulator} variable
     * @param matcher     the matcher The hamcrest matcher that checks whether the condition is fulfilled.
     * @throws org.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public void untilAccumulator(final LongAccumulator accumulator, final Matcher<? super Long> matcher) {
        until(new CallableHamcrestCondition<>(accumulator::longValue, matcher, generateConditionSettings()));
    }

    /**
     * Await until a {@link DoubleAccumulator} has a value matching the specified {@link org.hamcrest.Matcher}. E.g.
     * <p>&nbsp;</p>
     * <pre>
     * await().untilAccumulator(myDoubleAccumulator, is(greaterThan(2.0d)));
     * </pre>
     *
     * @param accumulator the {@link DoubleAccumulator} variable
     * @param matcher     the matcher The hamcrest matcher that checks whether the condition is fulfilled.
     * @throws org.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public void untilAccumulator(final DoubleAccumulator accumulator, final Matcher<? super Double> matcher) {
        until(new CallableHamcrestCondition<>(accumulator::doubleValue, matcher, generateConditionSettings()));
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
     * @throws org.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public <V> V untilAtomic(final AtomicReference<V> atomic, final Matcher<? super V> matcher) {
        return until(new CallableHamcrestCondition<>(atomic::get, matcher, generateConditionSettings()));
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
     * @param conditionEvaluator the condition evaluator
     * @throws org.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     */
    public void until(Callable<Boolean> conditionEvaluator) {
        until(new CallableCondition(conditionEvaluator, generateConditionSettings()));
    }

    private ConditionSettings generateConditionSettings() {
        Duration actualPollDelay = definePollDelay(pollDelay, pollInterval);

        if (isForever(actualPollDelay)) {
            throw new IllegalArgumentException("Cannot delay polling forever");
        }

        Duration timeout = timeoutConstraint.getMaxWaitTime();
        if (!isForever(timeout) && timeout.toNanos() <= actualPollDelay.toNanos()) {
            throw new IllegalArgumentException(String.format("Timeout (%s) must be greater than the poll delay (%s).",
                    formatAsString(timeout), formatAsString(actualPollDelay)));
        } else if ((!isForever(actualPollDelay) && !isForever(timeout)) && timeout.toNanos() <= actualPollDelay.toNanos()) {
            throw new IllegalArgumentException(String.format("Timeout (%s) must be greater than the poll delay (%s).",
                    formatAsString(timeout), formatAsString(actualPollDelay)));
        }

        ExecutorLifecycle executorLifecycle;
        if (this.executorLifecycle == null) {
            executorLifecycle = ExecutorLifecycle.withNormalCleanupBehavior(() -> InternalExecutorServiceFactory.create((r, threadName) -> new Thread(Thread.currentThread().getThreadGroup(), r, threadName), alias));
        } else {
            executorLifecycle = this.executorLifecycle;
        }

        return new ConditionSettings(alias, catchUncaughtExceptions, timeoutConstraint, pollInterval, actualPollDelay,
                conditionEvaluationListener, exceptionsIgnorer, executorLifecycle, failFastCondition);
    }

    private <T> T until(Condition<T> condition) {
        return condition.await();
    }

    /**
     * Ensures backward compatibility (especially that poll delay is the same as poll interval for fixed poll interval).
     * It also make sure that poll delay is {@link Duration#ZERO} for all other poll intervals if poll delay was not explicitly
     * defined. If poll delay was explicitly defined the it will just be returned.
     *
     * @param pollDelay    The poll delay
     * @param pollInterval The chosen (or default) poll interval
     * @return The poll delay to use
     */
    private Duration definePollDelay(Duration pollDelay, PollInterval pollInterval) {
        final Duration pollDelayToUse;
        // If a poll delay is null then a poll delay has not been explicitly defined by the user
        if (pollDelay == null) {
            if (pollInterval instanceof FixedPollInterval) {
                pollDelayToUse = pollInterval.next(1, Duration.ZERO); // Will return same poll delay as poll interval
            } else {
                pollDelayToUse = Duration.ZERO; // Default poll delay for non-fixed poll intervals
            }
        } else {
            // Poll delay was explicitly defined, use it!
            pollDelayToUse = pollDelay;
        }
        return pollDelayToUse;
    }

}
