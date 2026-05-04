/*
 * Copyright 2022 the original author or authors.
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

import org.awaitility.core.TerminalFailureException;
import org.awaitility.core.ThrowingRunnable;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Fast failure on terminal status #178
 * <p>
 * Issue https://github.com/awaitility/awaitility/issues/178
 * <p>
 * PR https://github.com/awaitility/awaitility/pull/193
 */
class FailFastTest {

    @AfterEach
    void reset_awaitility_after_each_test() {
        Awaitility.reset();
    }

    @Test
    void fail_fast_throws_terminal_failure_exception_with_the_supplied_failure_reason() {
        Throwable exception = assertThrows(TerminalFailureException.class, () -> {

            AtomicInteger ai = new AtomicInteger(0);

            new Thread(() -> {
                while (true) {
                    ai.incrementAndGet();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();

            // pretend that 10 is a terminal failure
            Callable<Boolean> failFastCondition = () -> ai.get() >= 10;

            await().timeout(Duration.ofSeconds(5))
                    .failFast("System crash", failFastCondition)
                    .until(ai::get, equalTo(-1)); // will never be true
        });
        assertThat(exception.getMessage(), equalTo("System crash"));
    }

    @Test
    void fail_fast_throws_terminal_failure_exception_with_the_default_failure_reason_when_no_failure_reason_is_specified() {
        Throwable exception = assertThrows(TerminalFailureException.class, () -> {

            AtomicInteger ai = new AtomicInteger(0);

            new Thread(() -> {
                while (true) {
                    ai.incrementAndGet();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();

            // pretend that 10 is a terminal failure
            Callable<Boolean> failFastCondition = () -> ai.get() >= 10;

            await().timeout(Duration.ofSeconds(5))
                    .failFast(failFastCondition)
                    .until(ai::get, equalTo(-1)); // will never be true
        });
        assertThat(exception.getMessage(), equalTo("Fail fast condition triggered"));
    }

    @Test
    void statically_configured_fail_fast_condition_without_failure_reason() {
        Throwable exception = assertThrows(TerminalFailureException.class, () -> {

            AtomicInteger ai = new AtomicInteger(0);

            new Thread(() -> {
                while (true) {
                    ai.incrementAndGet();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();

            // pretend that 10 is a terminal failure
            Callable<Boolean> failFastCondition = () -> ai.get() >= 10;
            Awaitility.setDefaultFailFastCondition(failFastCondition);

            await().timeout(Duration.ofSeconds(5))
                    .until(ai::get, equalTo(-1));// will never be true
        });
        assertThat(exception.getMessage(), equalTo("Fail fast condition triggered"));
    }

    @Test
    void statically_configured_fail_fast_condition_with_failure_reason() {
        Throwable exception = assertThrows(TerminalFailureException.class, () -> {

            AtomicInteger ai = new AtomicInteger(0);

            new Thread(() -> {
                while (true) {
                    ai.incrementAndGet();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();

            // pretend that 10 is a terminal failure
            Callable<Boolean> failFastCondition = () -> ai.get() >= 10;
            Awaitility.setDefaultFailFastCondition("System crash", failFastCondition);

            await().timeout(Duration.ofSeconds(5))
                    .until(ai::get, equalTo(-1)); // will never be true
        });
        assertThat(exception.getMessage(), equalTo("System crash"));
    }

    @Test
    void fail_fast_throws_terminal_failure_exception_without_reason_when_using_assertions_without_explicit_reason() {
        Throwable exception = assertThrows(TerminalFailureException.class, () -> {

            AtomicInteger ai = new AtomicInteger(0);

            new Thread(() -> {
                while (true) {
                    ai.incrementAndGet();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();

            // pretend that 10 is a terminal failure
            ThrowingRunnable failFastAssertion = () -> assertThat(ai.get(), greaterThan(10));

            await().timeout(Duration.ofSeconds(5))
                    .failFast(failFastAssertion)
                    .until(ai::get, equalTo(-1)); // will never be true
        });
        assertThat(exception.getMessage(), containsString("was less than <10>"));
    }

    @Test
    void fail_fast_throws_terminal_failure_exception_without_reason_when_using_default_assertions_without_explicit_reason() {
        Throwable exception = assertThrows(TerminalFailureException.class, () -> {

            AtomicInteger ai = new AtomicInteger(0);

            new Thread(() -> {
                while (true) {
                    ai.incrementAndGet();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();

            // pretend that 10 is a terminal failure
            ThrowingRunnable failFastAssertion = () -> assertThat(ai.get(), greaterThan(10));
            Awaitility.setDefaultFailFastCondition(failFastAssertion);

            await().timeout(Duration.ofSeconds(5))
                    .until(ai::get, equalTo(-1)); // will never be true
        });
        assertThat(exception.getMessage(), containsString("was less than <10>"));
    }

    @Test
    void fail_fast_throws_terminal_failure_exception_without_reason_when_using_assertions_with_explicit_reason() {
        Throwable exception = assertThrows(TerminalFailureException.class, () -> {

            AtomicInteger ai = new AtomicInteger(0);

            new Thread(() -> {
                while (true) {
                    ai.incrementAndGet();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();

            // pretend that 10 is a terminal failure
            ThrowingRunnable failFastAssertion = () -> assertThat(ai.get(), greaterThan(10));

            await().timeout(Duration.ofSeconds(5))
                    .failFast("fail fast reason", failFastAssertion)
                    .until(ai::get, equalTo(-1)); // will never be true
        });
        assertThat(exception.getMessage(), containsString("fail fast reason"));
        assertThat(exception.getCause(), instanceOf(AssertionError.class));
    }

    @Test
    void fail_fast_throws_terminal_failure_exception_without_reason_when_using_default_assertions_with_explicit_reason() {
        Throwable exception = assertThrows(TerminalFailureException.class, () -> {

            AtomicInteger ai = new AtomicInteger(0);

            new Thread(() -> {
                while (true) {
                    ai.incrementAndGet();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();

            // pretend that 10 is a terminal failure
            ThrowingRunnable failFastAssertion = () -> assertThat(ai.get(), greaterThan(10));
            Awaitility.setDefaultFailFastCondition("fail fast reason", failFastAssertion);

            await().timeout(Duration.ofSeconds(5))
                    .until(ai::get, equalTo(-1)); // will never be true
        });
        assertThat(exception.getMessage(), containsString("fail fast reason"));
        assertThat(exception.getCause(), instanceOf(AssertionError.class));
    }
}