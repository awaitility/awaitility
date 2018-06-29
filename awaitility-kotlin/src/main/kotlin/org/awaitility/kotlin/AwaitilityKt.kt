@file:JvmName("AwaitilityKt")

/**
 * Contains Awaitility extension functions
 */
package org.awaitility.kotlin

import org.awaitility.core.ConditionFactory

/**
 * An intermediary data type that stores the [ConditionFactory] and [fn] for later use.
 * Note that this data class should never be instantiated or used directly. Instead use the
 * [untilCallTo] extension function.
 *
 * @param factory The condition factory
 * @param fn The function to call in order to extract the value used by the predicate
 * @see untilCallTo
 */
data class AwaitilityKtUntilFunCondition<T> internal constructor(val factory: ConditionFactory, val fn: () -> T?)

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
 * await().untilCallTo { myRepository.count() } matches { count -> count == 1 }
 * ```
 *
 * Note that the reason why we can't name this function [untilCallTo] is because it clashes with
 * `org.awaitility.core.ConditionFactory#until(java.util.concurrent.Callable<java.lang.Boolean>)`.
 *
 * @param fn A function that returns the value that will be evaluated by the predicate in [matches].
 * @since 3.1.1
 */
fun <T> ConditionFactory.untilCallTo(fn: () -> T?) = AwaitilityKtUntilFunCondition(this, fn)