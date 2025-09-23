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

import org.awaitility.core.ConditionTimeoutException;
import org.awaitility.core.ThrowingRunnable;
import org.awaitility.support.CountDown;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.with;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;

class ConditionEvaluationListenerJava8Test {

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void expectedMatchMessageForAssertionConditionsWhenUsingLambdasWithoutAlias() {
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        CountDown countDown = new CountDown(10);
        with()
                .conditionEvaluationListener(condition -> {
                    try {
                        countDown.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    lastMatchMessage.set(condition.getDescription());
                })
                .untilAsserted(() -> assertEquals(5, (int) countDown.get()));

        String expectedMatchMessage = String.format("%s reached its end value", CountDown.class.getName());
        assertThat(lastMatchMessage.get(), allOf(startsWith("Assertion condition defined as a lambda expression"), endsWith(expectedMatchMessage)));
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void expectedMatchMessageForAssertionConditionsWhenUsingLambdasWithAlias() {
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        CountDown countDown = new CountDown(10);
        with()
                .conditionEvaluationListener(condition -> {
                    try {
                        countDown.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    lastMatchMessage.set(condition.getDescription());
                }).await("my alias")
                .untilAsserted(() -> assertEquals(5, (int) countDown.get()));

        String expectedMatchMessage = String.format("%s reached its end value", CountDown.class.getName());
        assertThat(lastMatchMessage.get(), allOf(startsWith("Assertion condition with alias my alias defined as a lambda expression"), endsWith(expectedMatchMessage)));
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void expectedMismatchMessageForAssertionConditionsWhenUsingLambdasWithoutAlias() {
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        CountDown countDown = new CountDown(10);
        assertThrows(ConditionTimeoutException.class, () -> 
            with()
                    .conditionEvaluationListener(condition -> {
                        try {
                            countDown.call();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        lastMatchMessage.set(condition.getDescription());
                    }).await().atMost(150, MILLISECONDS).untilAsserted(() -> assertEquals(-1, (int) countDown.get()))
        );
        assertThat(lastMatchMessage.get(), allOf(startsWith("Assertion condition defined as a lambda expression in"), containsString("expected: <-1> but was: <")));
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void expectedMismatchMessageForAssertionConditionsWhenUsingLambdasWithAlias() {
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        CountDown countDown = new CountDown(10);
        assertThrows(ConditionTimeoutException.class, () ->
                with().conditionEvaluationListener(condition -> {
                try {
                    countDown.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                lastMatchMessage.set(condition.getDescription());
            }).await("my alias").atMost(150, MILLISECONDS).untilAsserted(() -> assertEquals(-1, (int) countDown.get()))
        );
        assertThat(lastMatchMessage.get(), startsWith("Assertion condition with alias my alias defined as a lambda expression"));
    }

    @SuppressWarnings("Convert2Lambda")
    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void expectedMatchMessageForAssertionConditionsWhenNotUsingLambdasWithoutAlias(TestInfo info) {
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        CountDown countDown = new CountDown(10);
        with().conditionEvaluationListener(condition -> {
            try {
                countDown.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            lastMatchMessage.set(condition.getDescription());
        })
        .untilAsserted(new ThrowingRunnable() {
            @Override
            public void run() {
                assertEquals(5, (int) countDown.get());
            }
        });

        assertThat(lastMatchMessage.get(), allOf(startsWith("Assertion condition defined in"), containsString(info.getTestMethod().get().getName()), endsWith("reached its end value")));
    }

    @SuppressWarnings("Convert2Lambda")
    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void expectedMatchMessageForAssertionConditionsWhenNotUsingLambdasWithAlias(TestInfo info) {
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        CountDown countDown = new CountDown(10);
        with()
                .conditionEvaluationListener(condition -> {
                    try {
                        countDown.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    lastMatchMessage.set(condition.getDescription());
                }).await("my alias")
                .untilAsserted(new ThrowingRunnable() {
                    @Override
                    public void run() {
                        assertEquals(5, (int) countDown.get());
                    }
                });

        assertThat(lastMatchMessage.get(), allOf(startsWith("Assertion condition with alias my alias defined in"), containsString(info.getTestMethod().get().getName()), endsWith("reached its end value")));
    }

    @SuppressWarnings("Convert2Lambda")
    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void expectedMismatchMessageForAssertionConditionsWhenNotUsingLambdasWithoutAlias(TestInfo info) {
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        CountDown countDown = new CountDown(10);
        assertThrows(ConditionTimeoutException.class, () ->
            with()
                    .conditionEvaluationListener(condition -> {
                        lastMatchMessage.set(condition.getDescription());
                        try {
                            countDown.call();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).await().atMost(150, MILLISECONDS).untilAsserted(new ThrowingRunnable() {
                @Override
                public void run() {
                    assertEquals(-1, (int) countDown.get());
                }
            })
        );

        assertThat(lastMatchMessage.get(), allOf(startsWith("Assertion condition defined in"), containsString(info.getTestMethod().get().getName()), containsString("expected:")));
    }

    @SuppressWarnings("Convert2Lambda")
    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void expectedMismatchMessageForAssertionConditionsWhenNotUsingLambdasWithAlias(TestInfo info) {
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        CountDown countDown = new CountDown(10);
        assertThrows(ConditionTimeoutException.class, () ->
            with()
                    .conditionEvaluationListener(condition -> {
                        lastMatchMessage.set(condition.getDescription());
                        try {
                            countDown.call();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).await("my alias").atMost(150, MILLISECONDS)
                    .untilAsserted(new ThrowingRunnable() {
                        @Override
                        public void run() {
                            assertEquals(5, (int) countDown.get());
                        }
                    })
        );
        assertThat(lastMatchMessage.get(), allOf(startsWith("Assertion condition with alias my alias defined in"), containsString(info.getTestMethod().get().getName()), containsString("expected:")));
    }

    // Callable<Boolean> tests

    @SuppressWarnings("Convert2Lambda")
    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void expectedMatchMessageForCallableConditionsWithoutAliasWhenNotUsingLambda(TestInfo info) {
        final CountDown countDown = new CountDown(10);
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        with()
                .conditionEvaluationListener(condition -> lastMatchMessage.set(condition.getDescription()))
                .until(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return countDown.call() == 5;
                    }
                });

        assertThat(lastMatchMessage.get(), allOf(startsWith("Callable condition defined in"), containsString(info.getTestMethod().get().getName()), endsWith("returned true")));
    }

    @SuppressWarnings("Convert2Lambda")
    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void expectedMatchMessageForCallableConditionsWithAliasWhenNotUsingLambda(TestInfo info) {
        final CountDown countDown = new CountDown(10);
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        with()
                .conditionEvaluationListener(condition -> lastMatchMessage.set(condition.getDescription()))
                .await("my alias")
                .until(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return countDown.call() == 5;
                    }
                });

        assertThat(lastMatchMessage.get(), allOf(startsWith("Callable condition with alias my alias defined in"), containsString(info.getTestMethod().get().getName()), endsWith("returned true")));
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void expectedMatchMessageForCallableConditionsWithoutAliasWhenUsingLambda() {
        final CountDown countDown = new CountDown(10);
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        with()
                .conditionEvaluationListener(condition -> lastMatchMessage.set(condition.getDescription()))
                .until(() -> countDown.call() == 5);

        assertThat(lastMatchMessage.get(), allOf(startsWith("Condition defined as a lambda expression in"), containsString(getClass().getName()), endsWith("returned true")));
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void expectedMatchMessageForCallableConditionsWithAliasWhenUsingLambda() {
        final CountDown countDown = new CountDown(10);
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        with()
                .conditionEvaluationListener(condition -> lastMatchMessage.set(condition.getDescription()))
                .await("my alias")
                .until(() -> countDown.call() == 5);

        assertThat(lastMatchMessage.get(), allOf(startsWith("Condition with alias my alias defined as a lambda expression in "), containsString(getClass().getName()), endsWith("returned true")));
    }

    // Callable<Boolean> mismatch tests

    @SuppressWarnings("Convert2Lambda")
    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void expectedMismatchMessageForCallableConditionsWithoutAliasWhenNotUsingLambda(TestInfo info) {
        final CountDown countDown = new CountDown(10);
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        assertThrows(ConditionTimeoutException.class, () ->
            with()
                    .conditionEvaluationListener(condition -> lastMatchMessage.set(condition.getDescription()))
                    .await().atMost(150, MILLISECONDS)
                    .until(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return countDown.call() == -1;
                        }
                    })
        );
        assertThat(lastMatchMessage.get(), allOf(startsWith("Callable condition defined in"), containsString(info.getTestMethod().get().getName()), endsWith("returned false")));
    }

    @SuppressWarnings("Convert2Lambda")
    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void expectedMismatchMessageForCallableConditionsWithAliasWhenNotUsingLambda(TestInfo info) {
        final CountDown countDown = new CountDown(10);
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        assertThrows(ConditionTimeoutException.class, () ->
            with()
                    .conditionEvaluationListener(condition -> lastMatchMessage.set(condition.getDescription()))
                    .await("my alias").atMost(150, MILLISECONDS)
                    .until(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return countDown.call() == 5;
                        }
                    })
        );
        assertThat(lastMatchMessage.get(), allOf(startsWith("Callable condition with alias my alias defined in"), containsString(info.getTestMethod().get().getName()), endsWith("returned false")));
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void expectedMismatchMessageForCallableConditionsWithoutAliasWhenUsingLambda() {
        final CountDown countDown = new CountDown(10);
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        assertThrows(ConditionTimeoutException.class, () ->
            with()
                    .conditionEvaluationListener(condition -> lastMatchMessage.set(condition.getDescription()))
                    .await().atMost(150, MILLISECONDS)
                    .until(() -> countDown.call() == 5)
            );
        assertThat(lastMatchMessage.get(), allOf(startsWith("Condition defined as a lambda expression in"), containsString(getClass().getName()), endsWith("returned false")));
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void expectedMismatchMessageForCallableConditionsWithAliasWhenUsingLambda() {
        final CountDown countDown = new CountDown(10);
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        assertThrows(ConditionTimeoutException.class, () ->
            with()
                    .conditionEvaluationListener(condition -> lastMatchMessage.set(condition.getDescription()))
                    .await("my alias").atMost(150, MILLISECONDS)
                    .until(() -> countDown.call() == 5)
        );
        assertThat(lastMatchMessage.get(), allOf(startsWith("Condition with alias my alias defined as a lambda expression in "), containsString(getClass().getName()), endsWith("returned false")));
    }

    // Callable<Boolean> value test
    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void conditionOfCallableBooleanHasBooleanValuesInConditionEvaluationListener() {
        final CountDown countDown = new CountDown(10);
        final List<Boolean> results = new ArrayList<>();
        with()
                .conditionEvaluationListener(condition -> results.add((Boolean) condition.getValue()))
                .until(() -> countDown.call() == 5);

        assertThat(results.get(results.size() - 1), is(true));
        assertThat(results.subList(0, results.size() - 1), allOf(hasItem(false), not(hasItem(true))));
    }
}
