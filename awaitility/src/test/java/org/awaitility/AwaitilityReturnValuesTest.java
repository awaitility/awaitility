/*
 * Copyright 2016 the original author or authors.
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
package org.awaitility;

import org.awaitility.classes.Asynch;
import org.awaitility.classes.FakeRepository;
import org.awaitility.classes.FakeRepositoryImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.Callable;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.fieldIn;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;

public class AwaitilityReturnValuesTest {

    private FakeRepository fakeRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        fakeRepository = new FakeRepositoryImpl();
        Awaitility.reset();
    }

    @Test(timeout = 2000)
    public void returnsResultAfterSupplier() throws Exception {
        new Asynch(fakeRepository).perform();
        int value = await().until(new Callable<Integer>() {
            public Integer call() throws Exception {
                return fakeRepository.getValue();
            }
        }, greaterThan(0));
        assertEquals(1, value);
    }

    @Test(timeout = 2000)
    public void returnsResultAfterFieldInSupplier() throws Exception {
        new Asynch(fakeRepository).perform();
        int value = await().until(fieldIn(fakeRepository).ofType(int.class).andWithName("value"), equalTo(1));
        assertEquals(1, value);
    }
}
