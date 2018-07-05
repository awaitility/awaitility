@file:JvmName("AwaitilityKt")

/**
 * Contains Awaitility extension functions
 */
package org.awaitility.kotlin

import org.awaitility.Awaitility
import org.awaitility.Duration
import org.awaitility.core.ConditionFactory
import org.awaitility.pollinterval.PollInterval
import org.hamcrest.Matcher
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

/**
 * This is typically the starting point of the Kotlin "DSL". Allows you to write `await` instead of `await()`. For example:
 *
 * ```
 * await until { myRepository.count() == 2 }
 * ```
 * @since 3.1.2
 */
val await: ConditionFactory
    /**
     * @return A new [ConditionFactory] instance
     * @see [Awaitility.await]
     */
    get() = Awaitility.await()

/**
 * An intermediary data type that stores the [ConditionFactory] and [fn] for later use.
 * Note that this data class should never be instantiated or used directly. Instead use the
 * [untilCallTo] extension function.
 *
 * @param factory The condition factory
 * @param fn The function to call in order to extract the value used by the predicate
 * @see untilCallTo
 */
data class AwaitilityKtUntilFunCondition<T> internal constructor(internal val factory: ConditionFactory, internal val fn: () -> T?)

/**
 * Infix function which is what allows us to write the predicate on right-hand side of [matches] without using a dot.
 *
 * @param pred The predicate that determines whether or not the condition is fulfilled.
 */
infix fun <T> AwaitilityKtUntilFunCondition<T>.matches(pred: (T?) -> Boolean) = factory.until(fn, pred)

/**
 * An extension function to `ConditionFactory` that allows you do write conditions such as:
 *
 * ```
 * await untilCallTo { myRepository.count() } matches { count -> count == 1 }
 * ```
 *
 * Note that [await] is a getter that needs to imported from `org.awaitility.kotlin`.
 *
 * @param fn A function that returns the value that will be evaluated by the predicate in [matches].
 * @since 3.1.1
 */
infix fun <T> ConditionFactory.untilCallTo(fn: () -> T?) = AwaitilityKtUntilFunCondition(this, fn)

/**
 * An extension function to `ConditionFactory` that allows you do write conditions such as:
 *
 * ```
 * await until { myRepository.count() == 1 }
 * ```
 *
 * Note that [await] is a getter that needs to imported from `org.awaitility.kotlin`.
 *
 * @param fn The function to evaluate
 * @since 3.1.2
 */
infix fun ConditionFactory.until(fn: () -> Boolean) = until(fn)

/**
 * An extension function to `ConditionFactory` that allows you do write conditions such as:
 *
 * ```
 * await withPollInterval ONE_HUNDRED_MILLISECONDS ignoreException IllegalArgumentException::class untilAsserted  {
 *     assertThat(fakeRepository.value).isEqualTo(1)
 *}
 * ```
 *
 * I.e. you can use [untilAsserted] to integrate Awaitility with an assertion library of choice.
 * Note that [await] is a getter that needs to imported from `org.awaitility.kotlin`
 *
 * @param fn A function that returns the value that will be evaluated by the predicate in [matches].
 * @return the condition factory
 * @since 3.1.2
 */
infix fun ConditionFactory.untilAsserted(fn: () -> Unit): Unit = untilAsserted(fn)

/**
 * Await at most `timeout` before throwing a timeout exception.
 *
 * @param duration the duration
 * @return the condition factory
 * @since 3.1.2
 */
infix fun ConditionFactory.atMost(duration: Duration): ConditionFactory = atMost(duration)

/**
 * Condition has to be evaluated not earlier than `timeout` before throwing a timeout exception.
 *
 * @param timeout the timeout
 * @return the condition factory
 * @since 3.1.2
 */
infix fun ConditionFactory.atLeast(timeout: Duration): ConditionFactory = atLeast(timeout)

/**
 * Start building a named await statement. This is useful is cases when you
 * have several awaits in your test and you need to tell them apart. If a
 * named await timeout's the <code>alias</code> will be displayed indicating
 * which await statement that failed.
 *
 * @param alias the alias that will be shown if the await timeouts.
 * @return the condition factory
 * @since 3.1.2
 */
infix fun ConditionFactory.withAlias(alias: String): ConditionFactory = alias(alias)

/**
 * Specify the delay that will be used before Awaitility starts polling for
 * the result the first time. If you don't specify a poll delay explicitly
 * it'll be the same as the poll interval.
 *
 * @param pollDelay the poll delay
 * @return the condition factory
 * @since 3.1.2
 */
infix fun ConditionFactory.withPollDelay(pollDelay: Duration): ConditionFactory = pollDelay(pollDelay)

/**
 * Specify the polling interval Awaitility will use for this await
 * statement. This means the frequency in which the condition is checked for
 * completion.
 *
 * @param pollInterval the poll interval
 * @return the condition factory
 * @since 3.1.2
 * @see [ConditionFactory.pollInterval]
 */
infix fun ConditionFactory.withPollInterval(pollInterval: Duration): ConditionFactory = pollInterval(pollInterval)

/**
 * Specify the polling interval Awaitility will use for this await
 * statement. For example [org.awaitility.pollinterval.FibonacciPollInterval.fibonacci].
 *
 * @param pollInterval the poll interval
 * @return the condition factory
 * @since 3.1.2
 */
infix fun ConditionFactory.withPollInterval(pollInterval: PollInterval): ConditionFactory = pollInterval(pollInterval)

/**
 * Instruct Awaitility to ignore exceptions instance of the supplied exceptionType type.
 * Exceptions will be treated as evaluating to <code>false</code>.
 * This is useful in situations where the evaluated conditions may temporarily throw exceptions.
 *
 * @param exceptionType The exception type (hierarchy) to ignore
 * @return the condition factory
 * @since 3.1.2
 */
infix fun ConditionFactory.ignoreExceptionsInstanceOf(exceptionType: KClass<out Throwable>): ConditionFactory = ignoreExceptionsInstanceOf(exceptionType.javaObjectType)

/**
 * Instruct Awaitility to ignore a specific exception and <i>no</i> subclasses of this exception.
 * Exceptions will be treated as evaluating to <code>false</code>.
 * This is useful in situations where the evaluated conditions may temporarily throw exceptions.
 *
 * @param exceptionType The exception type to ignore
 * @return the condition factory
 * @since 3.1.2
 */
infix fun ConditionFactory.ignoreException(exceptionType: KClass<out Throwable>): ConditionFactory = ignoreException(exceptionType.javaObjectType)

/**
 * Instruct Awaitility to ignore exceptions that occur during evaluation and matches the supplied Hamcrest matcher.
 * Exceptions will be treated as evaluating to `false`. This is useful in situations where the evaluated
 * conditions may temporarily throw exceptions.
 *
 * @param matcher The Hamcrest matcher
 * @return the condition factory.
 * @since 3.1.2
 */
infix fun ConditionFactory.ignoreExceptionsMatching(matcher: Matcher<in Throwable>): ConditionFactory = ignoreExceptionsMatching(matcher)

/**
 * Instruct Awaitility to ignore exceptions that occur during evaluation and matches the supplied Hamcrest matcher.
 * Exceptions will be treated as evaluating to `false`. This is useful in situations where the evaluated
 * conditions may temporarily throw exceptions.
 *
 * @param matcher The predicate
 * @return the condition factory.
 * @since 3.1.2
 */
infix fun ConditionFactory.ignoreExceptionsMatching(matcher: (Throwable) -> Boolean): ConditionFactory = ignoreExceptionsMatching(matcher)

/**
 * Specify the executor service whose threads will be used to evaluate the poll condition in Awaitility.
 * Note that the executor service must be shutdown manually!
 *
 * This is an advanced feature and it should only be used sparingly.
 *
 * @param executorService The executor service that Awaitility will use when polling condition evaluations
 * @return the condition factory
 * @since 3.1.2
 */
infix fun ConditionFactory.pollExecutorService(executorService: ExecutorService): ConditionFactory = pollExecutorService(executorService)

/**
 * Specify a thread supplier whose thread will be used to evaluate the poll condition in Awaitility.
 * The supplier will be called only once and the thread it returns will be reused during all condition evaluations.
 * This is an advanced feature and it should only be used sparingly.
 *
 * @param threadSupplier A supplier of the thread that Awaitility will use when polling
 * @return the condition factory
 * @since 3.1.2
 */
infix fun ConditionFactory.pollThread(threadSupplier: (Runnable) -> Thread): ConditionFactory = pollThread(threadSupplier)

/**
 * Await until a Atomic boolean becomes true.
 *
 * @param atomicBoolean the atomic variable
 * @return the condition factory
 * @since 3.1.2
 */
infix fun ConditionFactory.untilTrue(atomicBoolean: AtomicBoolean) = untilTrue(atomicBoolean)

/**
 * Await until a Atomic boolean becomes false.
 *
 * @param atomicBoolean the atomic variable
 * @return the condition factory
 * @since 3.1.2
 */
infix fun ConditionFactory.untilFalse(atomicBoolean: AtomicBoolean) = untilFalse(atomicBoolean)