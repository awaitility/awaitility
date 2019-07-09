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
import org.awaitility.Awaitility.await
import org.awaitility.Durations.*
import org.awaitility.classes.Asynch
import org.awaitility.classes.FakeRepository
import org.awaitility.classes.FakeRepositoryImpl
import org.awaitility.core.ConditionTimeoutException
import org.awaitility.pollinterval.FibonacciPollInterval.fibonacci
import org.hamcrest.Matchers.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.time.Duration
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

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
    fun assertionCondition() {
        Asynch(fakeRepository).perform()
        await().untilAsserted { assertEquals(1, fakeRepository.value) }
    }

    @Test
    fun assertionConditionFailsWithANiceErrorMessage() {
        exception.expect(ConditionTimeoutException::class.java)
        exception.expectMessage(startsWith("Assertion condition defined in"))

        Asynch(fakeRepository).perform()
        await().atMost(1, SECONDS).untilAsserted { assertEquals(2, fakeRepository.value) }
    }

    @Test
    fun booleanConditionFailsWithANiceErrorMessage() {
        exception.expect(ConditionTimeoutException::class.java)
        exception.expectMessage(allOf(startsWith("Condition"), endsWith("was not fulfilled within 1 seconds.")))

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
    fun untilCallToExtensionFnHasADecentErrorMessage() {
        Asynch(fakeRepository).perform()

        val throwable = catchThrowable {
            await() atMost Duration.ofSeconds(1) untilCallTo { fakeRepository.value } matches { it == 2 }
        }

        assertThat(throwable).isExactlyInstanceOf(ConditionTimeoutException::class.java).hasMessageEndingWith("expected the predicate to return <true> but it returned <false> for input of <1> within 1 seconds.")
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

        val data : Data = await untilCallTo { fakeObjectRepository.data } has {
            state == "After"
        }
        assertThat(data.state).isEqualTo("After")
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