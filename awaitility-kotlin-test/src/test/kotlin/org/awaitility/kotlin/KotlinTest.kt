/*
 * Copyright 2017 the original author or authors.
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

import org.awaitility.Awaitility.await
import org.awaitility.classes.Asynch
import org.awaitility.classes.FakeRepository
import org.awaitility.classes.FakeRepositoryImpl
import org.junit.Before
import org.junit.Test


class KotlinTest {
    lateinit var asynch: Asynch
    lateinit var fakeRepository: FakeRepository

    @Before fun setup() {
        fakeRepository = FakeRepositoryImpl()
        asynch = Asynch(fakeRepository)
    }

    @Test fun testPath() {
        Asynch(fakeRepository).perform()
        await().until { fakeRepository.value == 1 }
    }
}