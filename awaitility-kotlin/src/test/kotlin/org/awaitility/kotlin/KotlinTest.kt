/*
 * Copyright 2019 the original author or authors.
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

package org.awaitility.kotlin

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.awaitility.Awaitility
import org.awaitility.Awaitility.await
import org.awaitility.Durations.*
import org.awaitility.classes.Asynch
import org.awaitility.classes.FakeRepository
import org.awaitility.classes.FakeRepositoryImpl
import org.awaitility.core.ConditionEvaluationListener
import org.awaitility.core.ConditionTimeoutException
import org.awaitility.core.TerminalFailureException
import org.awaitility.pollinterval.FibonacciPollInterval.fibonacci
import org.hamcrest.Matchers.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.Thread.sleep
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class KotlinTest {

    @Rule
    @JvmField
    val exception: ExpectedException = ExpectedException.none()

    private lateinit var asynch: Asynch
    private lateinit var fakeRepository: FakeRepository

    @Before
    fun setup() {
        fakeRepository = FakeRepositoryImpl()
        asynch = Asynch(fakeRepository)
    }

    @Test
    fun booleanCondition() {
        Asynch(fakeRepository).perform()
        await().until { fakeRepository.value == 1 }
    }

    @Test
    fun forever() {
        Asynch(fakeRepository).perform()
        await.forever until { fakeRepository.value == 1 }
    }

    @Test
    fun assertionCondition() {
        Asynch(fakeRepository).perform()
        await().untilAsserted { assertEquals(1, fakeRepository.value) }
    }

    @Test
    fun assertionConditionFailsWithANiceErrorMessage() {
        exception.expect(ConditionTimeoutException::class.java)
        exception.expectMessage(startsWith("Assertion condition defined"))

        Asynch(fakeRepository).perform()
        await().atMost(1, SECONDS).untilAsserted { assertEquals(2, fakeRepository.value) }
    }

    @Test
    fun booleanConditionFailsWithANiceErrorMessage() {
        exception.expect(ConditionTimeoutException::class.java)
        exception.expectMessage(allOf(startsWith("Condition"), endsWith("was not fulfilled within 1 second.")))

        Asynch(fakeRepository).perform()
        await().atMost(1, SECONDS).until { fakeRepository.value == 2 }
    }

    @Test
    fun untilCallToExtensionFn() {
        Asynch(fakeRepository).perform()

        await().untilCallTo { fakeRepository.value } matches { it == 1 }
    }

    @Test
    fun simpleAwaitUntilWithKotlin() {
        Asynch(fakeRepository).perform()

        await until { fakeRepository.value == 1 }
    }

    @Test
    fun usingLotsOfMethodsInDsl() {
        Asynch(fakeRepository).perform()

        await withAlias "Kotlin Test" ignoreExceptionsInstanceOf
                IllegalArgumentException::class withPollDelay ONE_HUNDRED_MILLISECONDS withPollInterval
                fibonacci().with().offset(1).and().unit(MILLISECONDS) atLeast TWO_HUNDRED_MILLISECONDS atMost
                ONE_MINUTE untilCallTo { fakeRepository.value } matches { it == 1 }

    }

    @Test
    fun untilAsserted() {
        Asynch(fakeRepository).perform()

        await withPollInterval ONE_HUNDRED_MILLISECONDS ignoreException IllegalArgumentException::class untilAsserted {
            assertThat(fakeRepository.value).isEqualTo(1)
        }
    }

    @Test
    fun untilAssertedWithConsumerMatcher() {
        Asynch(fakeRepository).perform()

        await.untilAsserted(fakeRepository::getValue) {
            assertThat(it).isEqualTo(1)
        }
    }

    @Test
    fun untilAtomicAssertedWhenReferenceIsNullByDefault() {
        val atomicReference = AtomicReference<String>()
        Thread {
            sleep(500)
            atomicReference.set("world")
        }.start()

        await.untilAtomic(atomicReference) { string ->
            assertThat(string).isEqualTo("world")
        }
    }

    @Test
    fun untilAssertedWithConditionEvaluationListener() {
        var value = false
        Asynch(fakeRepository).perform()

        await withPollInterval ONE_HUNDRED_MILLISECONDS ignoreException IllegalArgumentException::class conditionEvaluationListener ConditionEvaluationListener<Unit> { value = true } untilAsserted {
            assertThat(fakeRepository.value).isEqualTo(1)
        }

        assertThat(value).isTrue
    }

    @Test
    fun untilCallToExtensionFnHasADecentErrorMessage() {
        Asynch(fakeRepository).perform()

        val throwable = catchThrowable {
            await() atMost Duration.ofSeconds(1) untilCallTo { fakeRepository.value } matches { it == 2 }
        }

        assertThat(throwable).isExactlyInstanceOf(ConditionTimeoutException::class.java).hasMessageEndingWith("expected the predicate to return <true> but it returned <false> for input of <1> within 1 second.")
    }

    @Test
    fun untilNotNull() {
        val fakeObjectRepository = FakeGenericRepository<Data?>(null)
        AsynchObject(fakeObjectRepository, Data("Hello")).perform()

        val data = await untilNotNull { fakeObjectRepository.data }
        assertThat(data.state).isEqualTo("Hello") // No need for "data?.value" since we know it's not null!
    }

    @Test
    fun untilNull() {
        val fakeObjectRepository = FakeGenericRepository<Data?>(Data("Hello"))
        AsynchObject(fakeObjectRepository, null).perform()

        await untilNull { fakeObjectRepository.data }
    }

    @Test
    fun hasWithNullableType() {
        val fakeObjectRepository = FakeGenericRepository<Data?>(null)
        AsynchObject(fakeObjectRepository, Data("Hello")).perform()

        val data = await untilCallTo { fakeObjectRepository.data } has {
            state == "Hello"
        }
        assertThat(data.state).isEqualTo("Hello")
    }

    @Test
    fun hasWithNonNullableType() {
        val fakeObjectRepository = FakeGenericRepository(Data("Before"))
        AsynchObject(fakeObjectRepository, Data("After")).perform()

        val data: Data = await untilCallTo { fakeObjectRepository.data } has {
            state == "After"
        }
        assertThat(data.state).isEqualTo("After")
    }

    @Test
    fun atMostWithKotlinDuration() {
        await() atMost 1.seconds untilNull { null }
    }

    @Test
    fun atLeastWithKotlinDuration() {
        await() atLeast 1.seconds untilNull {
            Thread.sleep((1.seconds + 500.milliseconds).inWholeMilliseconds)
            null
        }
    }

    @Test
    fun withPollDelayAndWithPollIntervalWithKotlinDuration() {
        val ref = AtomicReference("value")

        Thread() {
            Thread.sleep((1.seconds + 500.milliseconds).inWholeMilliseconds)
            ref.set(null)
        }.start()

        val count = AtomicInteger()
        await() withPollDelay 1.seconds withPollInterval 100.milliseconds untilNull {
            count.incrementAndGet()
            ref.get()
        }
        // Mathematically we have 100ms in 500ms 5 times,
        // invocation count should be 5 times -> safe assertion interval of invocation is [4, 6] times
        assertThat(count).hasValueBetween(4, 6)
    }

    @Test(timeout = 2000)
    fun awaitDuringTimeOnCondition() {
        val duration = measureTimeMillis {
            await() during ONE_SECOND until { true }
        }

        assertThat(duration).isGreaterThan(1000)
    }

    @Test(timeout = 2000)
    fun awaitDuringTimeOnConditionWithKotlinDuration() {
        val duration = measureTimeMillis {
            await() during 1.seconds until { true }
        }

        assertThat(duration).isGreaterThan(1000)
    }

    @Test
    fun failFastThrowsTerminalFailureExceptionWhenConditionIsTrue() {
        val counter = AtomicInteger(0)
        val thrown = catchThrowable {
            await atMost Duration.ofSeconds(5) failFast { counter.incrementAndGet() >= 3 } until { false }
        }
        assertThat(thrown).isInstanceOf(TerminalFailureException::class.java)
    }

    @Test
    fun failFastWithReasonThrowsTerminalFailureExceptionWithTheReason() {
        val counter = AtomicInteger(0)
        val thrown = catchThrowable {
            (await atMost Duration.ofSeconds(5)).failFastWithReason("System crash") { counter.incrementAndGet() >= 3 } until { false }
        }
        assertThat(thrown).isInstanceOf(TerminalFailureException::class.java).hasMessage("System crash")
    }

    @Test
    fun failFastAssertedThrowsTerminalFailureException() {
        val counter = AtomicInteger(0)
        val thrown = catchThrowable {
            await atMost Duration.ofSeconds(5) failFastAsserted {
                assertThat(counter.incrementAndGet()).isLessThan(3)
            } until { false }
        }
        assertThat(thrown).isInstanceOf(TerminalFailureException::class.java)
    }

    @Test
    fun failFastAssertedWithReasonThrowsTerminalFailureExceptionWithTheReason() {
        val counter = AtomicInteger(0)
        val thrown = catchThrowable {
            (await atMost Duration.ofSeconds(5)).failFastAsserted("Workflow failed") {
                assertThat(counter.incrementAndGet()).isLessThan(3)
            } until { false }
        }
        assertThat(thrown).isInstanceOf(TerminalFailureException::class.java).hasMessage("Workflow failed")
    }

    @Test
    fun failFastDoesNotAffectAnAwaitThatSucceeds() {
        Asynch(fakeRepository).perform()
        await atMost Duration.ofSeconds(5) failFast { false } until { fakeRepository.value == 1 }
    }

    @Test(timeout = 2000)
    fun failFastWithReasonSupplierFromKotlin() {
        val evaluations = AtomicInteger()

        // failFastWithReason takes a lambda as reason; failFast(String, ..) is unaffected
        val lazyReason = catchThrowable {
            await().atMost(5, SECONDS).pollInterval(Duration.ofMillis(10))
                .failFastWithReason({ "Gave up after ${evaluations.get()} evaluations" }, Callable { evaluations.incrementAndGet() >= 3 })
                .until { false }
        }
        val staticReason = catchThrowable {
            await().atMost(5, SECONDS).pollInterval(Duration.ofMillis(10))
                .failFast("static reason", Callable { true })
                .until { false }
        }

        assertThat(lazyReason).isInstanceOf(TerminalFailureException::class.java).hasMessage("Gave up after 3 evaluations")
        assertThat(staticReason).isInstanceOf(TerminalFailureException::class.java).hasMessage("static reason")
    }

    @Test(timeout = 2000)
    fun untilThrownReturnsTheExceptionThrownOnTheThirdCall() {
        val calls = AtomicInteger()

        val e = await.atMost(5.seconds).withPollInterval(10.milliseconds).untilThrown<IOException> {
            if (calls.incrementAndGet() == 3) throw FileNotFoundException("third")
            calls.get()
        }

        assertThat(e).isInstanceOf(FileNotFoundException::class.java).hasMessage("third")
        assertThat(calls).hasValue(3)
    }

    @Test(timeout = 2000)
    fun untilThrownAcceptsAFunctionThatReturnsUnit() {
        val calls = AtomicInteger()

        val e = await.atMost(5.seconds).withPollInterval(10.milliseconds).untilThrown<IllegalStateException> {
            check(calls.incrementAndGet() < 3) { "third" }
        }

        assertThat(e).hasMessage("third")
    }

    @Test(timeout = 2000)
    fun untilThrownExactlyDoesNotMatchASubclass() {
        val e = await.atMost(5.seconds).withPollInterval(10.milliseconds) ignoreException FileNotFoundException::class
        val calls = AtomicInteger()

        val actual = e.untilThrownExactly<IOException> {
            if (calls.incrementAndGet() < 3) throw FileNotFoundException("subclass")
            throw IOException("exact")
        }

        assertThat(actual.javaClass).isEqualTo(IOException::class.java)
        assertThat(calls).hasValue(3)
    }

    @Test(timeout = 2000)
    fun untilThrownTimesOutWithAMessageThatNamesTheExpectedException() {
        val thrown = catchThrowable {
            await.atMost(300.milliseconds).withPollInterval(10.milliseconds).untilThrown<IOException> { 42 }
        }

        assertThat(thrown).isInstanceOf(ConditionTimeoutException::class.java)
            .hasMessageContaining("was expected to throw java.io.IOException but it returned 42 within 300 milliseconds.")
    }

    @Test
    fun withTimeoutMessageAppendsLazilyEvaluatedTextToTheTimeoutMessage() {
        val value = AtomicInteger(1)
        var calls = 0

        val throwable = catchThrowable {
            await withAlias "Kotlin Test" atMost 200.milliseconds withTimeoutMessage {
                calls++
                "value was ${value.get()}"
            } untilCallTo { value.get() } matches { it == 2 }
        }

        assertThat(calls).isEqualTo(1)
        assertThat(throwable).isExactlyInstanceOf(ConditionTimeoutException::class.java)
            .hasMessageStartingWith("Condition with alias 'Kotlin Test' didn't complete within 200 milliseconds because ")
            .hasMessageEndingWith(".\nvalue was 1")
    }

    @Test
    fun withTimeoutMessageIsNotCalledWhenTheConditionIsFulfilled() {
        var calls = 0

        await atMost 2.seconds withTimeoutMessage { calls++; "never shown" } until { true }

        assertThat(calls).isEqualTo(0)
    }

    @Test(timeout = 2000)
    fun onTimeoutIsCalledOnceWhenTheAwaitTimesOutAndSurvivesLaterLogging() {
        val descriptions = mutableListOf<String>()
        val logged = mutableListOf<String>()

        val throwable = catchThrowable {
            await atMost 300.milliseconds onTimeout { descriptions.add(it.description) } logging { logged.add(it) } until { false }
        }

        assertThat(throwable).isExactlyInstanceOf(ConditionTimeoutException::class.java)
        assertThat(descriptions).containsExactly(throwable.message)
        assertThat(logged).isNotEmpty
    }

    @Test(timeout = 2000)
    fun onTimeoutIsNotCalledWhenTheConditionIsFulfilled() {
        var called = false

        await atMost 1.seconds onTimeout { called = true } until { true }

        assertThat(called).isFalse
    }

    @Test(timeout = 2000)
    fun awaitDuringExtendsTheDefaultTimeout() {
        Awaitility.setDefaultTimeout(500.milliseconds.toJavaDuration())
        try {
            await() during 500.milliseconds until { true }
        } finally {
            Awaitility.reset()
        }
    }

    @Test(timeout = 2000)
    fun awaitDuringFailsAtOnceWhenTheUserSetAtMostIsTooShort() {
        var throwable: Throwable? = null
        val duration = measureTimeMillis {
            throwable = catchThrowable { await() atMost 300.milliseconds during 1.seconds until { true } }
        }

        assertThat(throwable).isInstanceOf(ConditionTimeoutException::class.java).hasMessageContaining("can never be fulfilled")
        assertThat(duration).isLessThan(500)
    }

    @Test(timeout = 2000)
    fun untilOrTimeoutReturnsSuccessfulResult() {
        Asynch(fakeRepository).perform()

        val result = await atMost 2.seconds untilOrTimeout { fakeRepository.value == 1 }

        assertThat(result.isSuccess).isTrue()
        assertThat(result.value).isTrue()
    }

    @Test(timeout = 2000)
    fun untilOrTimeoutReturnsFailedResultInsteadOfThrowing() {
        val result = await atMost 300.milliseconds untilOrTimeout { fakeRepository.value == 2 }

        assertThat(result.isSuccess).isFalse()
        assertThat(result.value).isFalse()
        assertThat(result.cause).isExactlyInstanceOf(ConditionTimeoutException::class.java)
            .hasMessageEndingWith("was not fulfilled within 300 milliseconds.")
    }

    @Test(timeout = 2000)
    fun untilAssertedOrTimeoutReturnsFailedResultInsteadOfThrowing() {
        val result = await atMost 300.milliseconds untilAssertedOrTimeout {
            assertThat(fakeRepository.value).isEqualTo(2)
        }

        assertThat(result.isSuccess).isFalse()
        assertThat(result.value).isNull()
        assertThat(result.cause).hasMessageStartingWith("Assertion condition defined")
    }

    @Test(timeout = 2000)
    fun untilOrTimeoutWithSupplierAndPredicateCanBeCalledFromKotlin() {
        Asynch(fakeRepository).perform()

        val result = await.atMost(2.seconds.toJavaDuration()).untilOrTimeout({ fakeRepository.value }) { it == 1 }

        assertThat(result.value).isEqualTo(1)
    }
}

class AsynchObject<T>(private val repository: FakeGenericRepository<T>, private val changeTo: T) {

    fun perform() {
        val thread = Thread(Runnable {
            try {
                Thread.sleep(600)
                repository.data = changeTo
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        })
        thread.start()
    }
}


data class Data(var state: String)
data class FakeGenericRepository<T>(var data: T)