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

import com.jayway.awaitility.constraint.AtMostWaitConstraint;
import com.jayway.awaitility.constraint.WaitConstraint;
import com.jayway.awaitility.core.*;
import com.jayway.awaitility.pollinterval.FixedPollInterval;
import com.jayway.awaitility.pollinterval.PollInterval;
import org.hamcrest.Matcher;

import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Duration.ONE_HUNDRED_MILLISECONDS;

/**
 * Awaitility is a small Java DSL for synchronizing (waiting for) asynchronous
 * operations. It makes it easy to test asynchronous code. Examples:
 * <p>&nbsp;</p>
 * Wait at most 5 seconds until customer status has been updated:
 * <p/>
 * <pre>
 * await().atMost(5, SECONDS).until(customerStatusHasUpdated());
 * </pre>
 * <p/>
 * Wait forever until the call to <code>orderService.orderCount()</code> is
 * greater than 3.
 * <p/>
 * <pre>
 * await().forever().untilCall(to(orderService).orderCount(), greaterThan(3));
 * </pre>
 * <p/>
 * Wait 300 milliseconds until field in object <code>myObject</code> with name
 * <code>fieldName</code> and of type <code>int.class</code> is equal to 4.
 * <p/>
 * <pre>
 * await().atMost(300, MILLISECONDS).until(fieldIn(orderService).withName("fieldName").andOfType(int.class), equalTo(3));
 * </pre>
 * <p/>
 * Advanced usage: Use a poll interval of 100 milliseconds with an initial delay
 * of 20 milliseconds until customer status is equal to "REGISTERED". This
 * example also uses a named await by specifying an alias
 * ("customer registration"). This makes it easy to find out which await
 * statement that failed if you have multiple awaits in the same test.
 * <p/>
 * <pre>
 * with().pollInterval(ONE_HUNDERED_MILLISECONDS).and().with().pollDelay(20, MILLISECONDS).await("customer registration")
 *         .until(customerStatus(), equalTo(REGISTERED));
 * </pre>
 * <p/>
 * You can also specify a default timeout, poll interval and poll delay using:
 * <p/>
 * <pre>
 * Awaitility.setDefaultTimeout(..)
 * Awaitility.setDefaultPollInterval(..)
 * Awaitility.setDefaultPollDelay(..)
 * </pre>
 * <p/>
 * You can also reset to the default values using {@link com.jayway.awaitility.Awaitility#reset()}.
 * In order to use Awaitility effectively it's recommended to statically import
 * the following methods from the Awaitility framework:
 * <p>&nbsp;</p>
 * <ul>
 * <li>com.jayway.awaitility.Awaitlity.*</li>
 * </ul>
 * It may also be useful to import these methods:
 * <ul>
 * <li>com.jayway.awaitility.Duration.*</li>
 * <li>java.util.concurrent.TimeUnit.*</li>
 * <li>org.hamcrest.Matchers.*</li>
 * <li>org.junit.Assert.*</li>
 * </ul>
 * <p>&nbsp;</p>
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
 * <p>&nbsp;</p>
 * Note that since Awaitility uses polling to verify that a condition matches
 * it's not intended to use it for precise performance testing.
 * <p>&nbsp;</p>
 * <b>IMPORTANT:</b> Awaitility does nothing to ensure thread safety or thread
 * synchronization! This is your responsibility! Make sure your code is correctly
 * synchronized or that you are using thread safe data structures such as volatile
 * fields or classes such as AtomicInteger and ConcurrentHashMap.
 */
public class Awaitility {

    private static final Duration DEFAULT_POLL_DELAY = null;

    private static final PollInterval DEFAULT_POLL_INTERVAL = new FixedPollInterval(ONE_HUNDRED_MILLISECONDS);

    /**
     * The default poll interval (fixed 100 ms).
     */
    private static volatile PollInterval defaultPollInterval = DEFAULT_POLL_INTERVAL;

    /**
     * The default wait constraint (10 seconds).
     */
    private static volatile WaitConstraint defaultWaitConstraint = AtMostWaitConstraint.TEN_SECONDS;

    /**
     * The default poll delay
     */
    private static volatile Duration defaultPollDelay = DEFAULT_POLL_DELAY;

    /**
     * Catch all uncaught exceptions by default?
     */
    private static volatile boolean defaultCatchUncaughtExceptions = true;

    /**
     * Ignore caught exceptions by default?
     */
    private static volatile ExceptionIgnorer defaultExceptionIgnorer = new PredicateExceptionIgnorer(new Predicate<Exception>() {
        public boolean matches(Exception e) {
            return false;
        }
    });

    /**
     * Default listener of condition evaluation results.
     */
    private static volatile ConditionEvaluationListener defaultConditionEvaluationListener = null;

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
     * Instruct Awaitility to ignore caught or uncaught exceptions during condition evaluation.
     * Exceptions will be treated as evaluating to <code>false</code>. Your test will not fail
     * upon an exception, unless it times out.
     */
    public static void ignoreExceptionsByDefault() {
        defaultExceptionIgnorer = new PredicateExceptionIgnorer(new Predicate<Exception>() {
            public boolean matches(Exception e) {
                return true;
            }
        });
    }

    /**
     * Instruct Awaitility to ignore caught exception of the given type during condition evaluation.
     * Exceptions will be treated as evaluating to <code>false</code>. Your test will not fail
     * upon an exception matching the supplied exception type, unless it times out.
     */
    public static void ignoreExceptionByDefault(final Class<? extends Exception> exceptionType) {
        defaultExceptionIgnorer = new PredicateExceptionIgnorer(new Predicate<Exception>() {
            public boolean matches(Exception e) {
                return e.getClass().equals(exceptionType);
            }
        });
    }

    /**
     * Instruct Awaitility to ignore caught exceptions matching the given <code>predicate</code> during condition evaluation.
     * Exceptions will be treated as evaluating to <code>false</code>. Your test will not fail
     * upon an exception matching the supplied predicate, unless it times out.
     */
    public static void ignoreExceptionsByDefaultMatching(Predicate<Exception> predicate) {
        defaultExceptionIgnorer = new PredicateExceptionIgnorer(predicate);
    }

    /**
     * Instruct Awaitility to ignore caught exceptions matching the supplied <code>matcher</code> during condition evaluation.
     * Exceptions will be treated as evaluating to <code>false</code>. Your test will not fail
     * upon an exception matching the supplied exception type, unless it times out.
     */
    public static void ignoreExceptionsByDefaultMatching(Matcher<? super Exception> matcher) {
        defaultExceptionIgnorer = new HamcrestExceptionIgnorer(matcher);
    }

    /**
     * Reset the timeout, poll interval, poll delay, uncaught exception handling
     * to their default values:
     * <p>&nbsp;</p>
     * <ul>
     * <li>timeout - 10 seconds</li>
     * <li>poll interval - 100 milliseconds</li>
     * <li>poll delay - 100 milliseconds</li>
     * <li>Catch all uncaught exceptions - true</li>
     * <li>Do not ignore caught exceptions</li>
     * <li>Don't handle condition evaluation results</li>
     * </ul>
     */
    public static void reset() {
        defaultPollInterval = DEFAULT_POLL_INTERVAL;
        defaultPollDelay = DEFAULT_POLL_DELAY;
        defaultWaitConstraint = AtMostWaitConstraint.TEN_SECONDS;
        defaultCatchUncaughtExceptions = true;
        defaultConditionEvaluationListener = null;
        defaultExceptionIgnorer = new PredicateExceptionIgnorer(new Predicate<Exception>() {
            public boolean matches(Exception e) {
                return false;
            }
        });
        Thread.setDefaultUncaughtExceptionHandler(null);
        MethodCallRecorder.reset();
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
     * @param alias the alias that will be shown if the await timeouts.
     * @return the condition factory
     */
    public static ConditionFactory await(String alias) {
        return new ConditionFactory(alias, defaultWaitConstraint, defaultPollInterval, defaultPollDelay,
                defaultCatchUncaughtExceptions, defaultExceptionIgnorer, defaultConditionEvaluationListener);
    }

    /**
     * Catching uncaught exceptions in other threads. This will make the await
     * statement fail even if exceptions occur in other threads. This is the
     * default behavior.
     *
     * @return the condition factory
     */
    public static ConditionFactory catchUncaughtExceptions() {
        return new ConditionFactory(defaultWaitConstraint, defaultPollInterval, defaultPollDelay, true, defaultExceptionIgnorer);
    }

    /**
     * Don't catch uncaught exceptions in other threads. This will <i>not</i>
     * make the await statement fail if exceptions occur in other threads.
     *
     * @return the condition factory
     */
    public static ConditionFactory dontCatchUncaughtExceptions() {
        return new ConditionFactory(defaultWaitConstraint, defaultPollInterval, defaultPollDelay, false, defaultExceptionIgnorer);
    }

    /**
     * Start constructing an await statement with some settings. E.g.
     * <p>
     * <pre>
     * with().pollInterval(20, MILLISECONDS).await().until(somethingHappens());
     * </pre>
     *
     * @return the condition factory
     */
    public static ConditionFactory with() {
        return new ConditionFactory(defaultWaitConstraint, defaultPollInterval, defaultPollDelay,
                defaultCatchUncaughtExceptions, defaultExceptionIgnorer, defaultConditionEvaluationListener);
    }

    /**
     * Start constructing an await statement given some settings. E.g.
     * <p>
     * <pre>
     * given().pollInterval(20, MILLISECONDS).then().await().until(somethingHappens());
     * </pre>
     *
     * @return the condition factory
     */
    public static ConditionFactory given() {
        return new ConditionFactory(defaultWaitConstraint, defaultPollInterval, defaultPollDelay,
                defaultCatchUncaughtExceptions, defaultExceptionIgnorer);
    }

    /**
     * An alternative to using {@link #await()} if you want to specify a timeout
     * directly.
     *
     * @param timeout the timeout
     * @return the condition factory
     */
    public static ConditionFactory waitAtMost(Duration timeout) {
        return new ConditionFactory(defaultWaitConstraint.withMaxWaitTime(timeout), defaultPollInterval, defaultPollDelay,
                defaultCatchUncaughtExceptions, defaultExceptionIgnorer);
    }

    /**
     * An alternative to using {@link #await()} if you want to specify a timeout
     * directly.
     *
     * @param value the value
     * @param unit  the unit
     * @return the condition factory
     */
    public static ConditionFactory waitAtMost(long value, TimeUnit unit) {
        return new ConditionFactory(defaultWaitConstraint.withMaxWaitTime(new Duration(value, unit)), defaultPollInterval,
                defaultPollDelay, defaultCatchUncaughtExceptions, defaultExceptionIgnorer);
    }

    /**
     * Sets the default poll interval that all await statements will use.
     *
     * @param pollInterval the poll interval
     * @param unit         the unit
     */
    public static void setDefaultPollInterval(long pollInterval, TimeUnit unit) {
        defaultPollInterval = new FixedPollInterval(new Duration(pollInterval, unit));
    }

    /**
     * Sets the default poll delay all await statements will use.
     *
     * @param pollDelay the poll delay
     * @param unit      the unit
     */
    public static void setDefaultPollDelay(long pollDelay, TimeUnit unit) {
        defaultPollDelay = new Duration(pollDelay, unit);
    }

    /**
     * Sets the default timeout all await statements will use.
     *
     * @param timeout the timeout
     * @param unit    the unit
     */
    public static void setDefaultTimeout(long timeout, TimeUnit unit) {
        defaultWaitConstraint = defaultWaitConstraint.withMaxWaitTime(new Duration(timeout, unit));
    }

    /**
     * Sets the default poll interval that all await statements will use.
     *
     * @param pollInterval the new default poll interval
     */
    public static void setDefaultPollInterval(Duration pollInterval) {
        if (pollInterval == null) {
            throw new IllegalArgumentException("You must specify a poll interval (was null).");
        }
        defaultPollInterval = new FixedPollInterval(pollInterval);
    }

    /**
     * Sets the default poll interval that all await statements will use.
     *
     * @param pollInterval the new default poll interval
     */
    public static void setDefaultPollInterval(PollInterval pollInterval) {
        if (pollInterval == null) {
            throw new IllegalArgumentException("You must specify a poll interval (was null).");
        }
        defaultPollInterval = pollInterval;
    }

    /**
     * Sets the default poll delay that all await statements will use.
     *
     * @param pollDelay the new default poll delay
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
     * @param defaultTimeout the new default timeout
     */
    public static void setDefaultTimeout(Duration defaultTimeout) {
        if (defaultTimeout == null) {
            throw new IllegalArgumentException("You must specify a default timeout (was null).");
        }
        defaultWaitConstraint = defaultWaitConstraint.withMaxWaitTime(defaultTimeout);
    }

    /**
     * Sets the default condition evaluation listener that all await statements will use.
     *
     * @param defaultConditionEvaluationListener handles condition evaluation each time evaluation of a condition occurs. Works only with Hamcrest matcher-based conditions.
     */
    public static void setDefaultConditionEvaluationListener(ConditionEvaluationListener defaultConditionEvaluationListener) {
        Awaitility.defaultConditionEvaluationListener = defaultConditionEvaluationListener;
    }

    /**
     * Await until a specific method invocation returns something. E.g.
     * <p>
     * <pre>
     * await().untilCall(to(service).getCount(), greaterThan(2));
     * </pre>
     * <p>
     * Here we tell Awaitility to wait until the <code>service.getCount()</code>
     * method returns a value that is greater than 2.
     *
     * @param <S>    The type of the service.
     * @param object the object that contains the method of interest.
     * @return A proxy of the service
     */
    @SuppressWarnings("unchecked")
    public static <S> S to(S object) {
        return (S) MethodCallRecorder.createProxy(object);
    }

    /**
     * Await until a {@link ThrowingRunnable} supplier execution passes (ends without throwing an exception).
     * <p>&nbsp;</p>
     * This method is intended to benefit from lambda expressions introduced in Java 8. It allows to use standard AssertJ/FEST Assert assertions
     * (by the way also standard JUnit/TestNG assertions) to test asynchronous calls and systems.
     * It accepts {@link ThrowingRunnable} interface instead of plain {@link Runnable} to allow passing lambda expressions that throw exceptions
     * in their bodies, e.g.:
     * <pre>
     * await().until(matches(() -> {
     *      methodThatHasThrowsInItsDeclaration();
     * }));
     * </pre>
     * when using {@link Runnable} user would have to write something like:
     * <pre>
     * await().until(matches(() -> {
     *      try {
     *          methodThatHasThrowsInItsDeclaration();
     *      } catch(Exception e) {
     *          throw new RuntimeException(e);
     *      }
     * }));
     * </pre>
     * <p>&nbsp;</p>
     * {@link java.lang.AssertionError} instances thrown by the supplier are treated as an assertion failure and proper error message is propagated on timeout.
     * Other exceptions are rethrown immediately as an execution errors.
     *
     * @param throwingRunnable the supplier that is responsible for executing the assertion and throwing AssertionError on failure.
     * @throws com.jayway.awaitility.core.ConditionTimeoutException If condition was not fulfilled within the given time period.
     * @since 1.7.0
     */
    public static Runnable matches(final ThrowingRunnable throwingRunnable) {
        return new Runnable() {
            public void run() {
                try {
                    throwingRunnable.run();
                } catch (Throwable e) {
                    CheckedExceptionRethrower.safeRethrow(e);
                }
            }
        };
    }

    /**
     * Await until an instance field matches something. E.g.
     * <p>
     * <pre>
     * await().until(fieldIn(service).ofType(int.class).andWithName("fieldName"), greaterThan(2));
     * </pre>
     * <p>
     * Here Awaitility waits until a field with name <code>fieldName</code> and of the <code>int.class</code>
     * in object <code>service</code> is greater than 2.
     * <p>
     * Note that the field must be thread-safe in order to guarantee correct behavior.
     *
     * @param object The object that contains the field.
     * @return A field supplier builder which lets you specify the parameters needed to find the field.
     */
    public static FieldSupplierBuilder fieldIn(Object object) {
        return new FieldSupplierBuilder(object);
    }

    /**
     * Await until a static field matches something. E.g.
     * <p>
     * <pre>
     * await().until(fieldIn(Service.class).ofType(int.class).andWithName("fieldName"), greaterThan(2));
     * </pre>
     * <p>
     * Here Awaitility waits until a static field with name <code>fieldName</code> and of the
     * <code>int.class</code> in object <code>service</code> is greater than 2.
     * <p>
     * Note that the field must be thread-safe in order to guarantee correct behavior.
     *
     * @param clazz The class that contains the static field.
     * @return A field supplier builder which lets you specify the parameters needed to find the field.
     */
    public static FieldSupplierBuilder fieldIn(Class<?> clazz) {
        return new FieldSupplierBuilder(clazz);
    }
}
