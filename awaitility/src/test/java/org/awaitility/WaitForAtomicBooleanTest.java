/*
 * Copyright 2015 the original author or authors.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.FIVE_SECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;

class WaitForAtomicBooleanTest {
    private AtomicBoolean wasAdded;
    private AtomicBoolean wasAddedWithDefaultValue;

    @BeforeEach
    void setup() {
        wasAdded = new AtomicBoolean(false);
        wasAddedWithDefaultValue = new AtomicBoolean();
    }

    @Test
    @Timeout(value = 2000L, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void atomicBooleanExample() {
        new WasAddedModifier().start();

        await().atMost(FIVE_SECONDS).until(wasAdded(), equalTo(true));
    }

    @Test
    @Timeout(value = 2000L, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void atomicBooleanWithUntilTrueWhenBooleanUsesDefaultValue() {
        new WasAddedWithDefaultValue().start();

        await().atMost(FIVE_SECONDS).untilTrue(wasAddedWithDefaultValue);
    }

    @Test
    @Timeout(value = 2000L, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void atomicBooleanWithUntilTrue() {
        new WasAddedModifier().start();

        await().atMost(FIVE_SECONDS).untilTrue(wasAdded);
    }

    @Test
    @Timeout(value = 2000L, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void atomicBooleanWithUntilFalse() {
        wasAdded.set(true);
        new WasAddedModifier().start();

        await().atMost(FIVE_SECONDS).untilFalse(wasAdded);
    }

    private Callable<Boolean> wasAdded() {
        return () -> wasAdded.get();
    }

    private class WasAddedModifier extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            wasAdded.set(!wasAdded.get());
        }
    }

    private class WasAddedWithDefaultValue extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            wasAddedWithDefaultValue.set(true);
        }
    }
}
