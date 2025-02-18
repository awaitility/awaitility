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
package org.awaitility;

import org.awaitility.classes.Asynch;
import org.awaitility.classes.FakeRepository;
import org.awaitility.core.ConditionTimeoutException;
import org.awaitility.core.JavaVersionDetector;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

public class UsingAtomicTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        Awaitility.reset();
    }

    @Test(timeout = 2000)
    public void usingAtomicInteger() {
        AtomicInteger atomic = new AtomicInteger(0);
        new Asynch(new FakeRepositoryWithAtomicInteger(atomic)).perform();
        await().untilAtomic(atomic, equalTo(1));
    }

    @Test(timeout = 2000)
    public void usingAtomicIntegerAndTimeout() {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage("expected <1> but was <0> within 200 milliseconds.");
        AtomicInteger atomic = new AtomicInteger(0);
        await().atMost(200, MILLISECONDS).untilAtomic(atomic, equalTo(1));
    }

    @Test(timeout = 2000)
    public void usingAtomicBoolean() {
        AtomicBoolean atomic = new AtomicBoolean(false);
        new Asynch(new FakeRepositoryWithAtomicBoolean(atomic)).perform();
        await().untilAtomic(atomic, equalTo(true));
    }

    @Test(timeout = 2000)
    public void usingAtomicBooleanAndTimeout() {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage("expected <true> but was <false> within 200 milliseconds.");
        AtomicBoolean atomic = new AtomicBoolean(false);
        await().atMost(200, MILLISECONDS).untilAtomic(atomic, equalTo(true));
    }

    @Test(timeout = 2000)
    public void usingAtomicLong() {
        AtomicLong atomic = new AtomicLong(0);
        new Asynch(new FakeRepositoryWithAtomicLong(atomic)).perform();
        await().untilAtomic(atomic, equalTo(1L));
    }

    @Test(timeout = 2000)
    public void usingAtomicLongAndTimeout() {
        exception.expect(ConditionTimeoutException.class);
        String message = JavaVersionDetector.getJavaMajorVersion() < 17 ?
                "Lambda expression in org.awaitility.core.ConditionFactory that uses java.util.concurrent.atomic.AtomicLong: expected <1L> but was <0L> within 200 milliseconds."
                : "Lambda expression in org.awaitility.core.ConditionFactory expected <1L> but was <0L> within 200 milliseconds.";
        exception.expectMessage(message);
        AtomicLong atomic = new AtomicLong(0);
        await().atMost(200, MILLISECONDS).untilAtomic(atomic, equalTo(1L));
    }

    @Test(timeout = 2000)
    public void usingAtomicReference() {
        AtomicReference<String> atomic = new AtomicReference<>("0");
        new Asynch(new FakeRepositoryWithAtomicReference(atomic)).perform();
        await().untilAtomic(atomic, equalTo("1"));
    }

    @Test(timeout = 2000)
    public void usingAtomicReferenceAndTimeout() {
        exception.expect(ConditionTimeoutException.class);
        String message = JavaVersionDetector.getJavaMajorVersion() < 17 ?
                "Lambda expression in org.awaitility.core.ConditionFactory that uses java.util.concurrent.atomic.AtomicReference: expected \"1\" but was \"0\" within 200 milliseconds."
                : "Lambda expression in org.awaitility.core.ConditionFactory expected \"1\" but was \"0\" within 200 milliseconds.";

        exception.expectMessage(message);
        AtomicReference<String> atomic = new AtomicReference<>("0");
        await().atMost(200, MILLISECONDS).untilAtomic(atomic, equalTo("1"));
    }
}

class FakeRepositoryWithAtomicInteger implements FakeRepository {

    private final AtomicInteger integer;

    public FakeRepositoryWithAtomicInteger(AtomicInteger integer) {
        this.integer = integer;
    }

    public int getValue() {
        return integer.get();
    }

    public void setValue(int value) {
        integer.set(value);
    }

}

class FakeRepositoryWithAtomicLong implements FakeRepository {

    private final AtomicLong atomic;

    public FakeRepositoryWithAtomicLong(AtomicLong atomic) {
        this.atomic = atomic;
    }

    public int getValue() {
        return (int) atomic.get();
    }

    public void setValue(int value) {
        atomic.set(value);
    }

}

class FakeRepositoryWithAtomicBoolean implements FakeRepository {

    private final AtomicBoolean atomic;

    public FakeRepositoryWithAtomicBoolean(AtomicBoolean atomic) {
        this.atomic = atomic;
    }

    public int getValue() {
        return atomic.get() ? 1 : 0;
    }

    public void setValue(int value) {
        atomic.set(value > 0);
    }

}

class FakeRepositoryWithAtomicReference implements FakeRepository {

    private final AtomicReference<String> atomic;

    public FakeRepositoryWithAtomicReference(AtomicReference<String> atomic) {
        this.atomic = atomic;
    }

    public int getValue() {
        return Integer.parseInt(atomic.get());
    }

    public void setValue(int value) {
        atomic.set("" + value);
    }
}