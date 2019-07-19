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

package org.awaitility;

import org.junit.Test;

import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

public class AwaitilityAdderTest {

    @Test(timeout = 2000)
    public void awaitilityCanWaitForLongAdders() {
        // Given
        LongAdder accumulator = new LongAdder();

        // When
        new Thread(() -> {
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            accumulator.add(5);
        }).start();

        // Then
        await().untilAdder(accumulator, equalTo(5L));
    }

    @Test(timeout = 2000)
    public void awaitilityCanWaitForDoubleAdders() {
        // Given
        DoubleAdder accumulator = new DoubleAdder();

        // When
        new Thread(() -> {
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            accumulator.add(5.5d);
        }).start();

        // Then
        await().untilAdder(accumulator, equalTo(5.5d));
    }
}