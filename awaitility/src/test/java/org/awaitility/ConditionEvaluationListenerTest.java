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

import org.awaitility.core.ConditionEvaluationListener;
import org.awaitility.core.EvaluatedCondition;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.awaitility.Awaitility.setDefaultConditionEvaluationListener;
import static org.awaitility.Awaitility.with;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ConditionEvaluationListenerTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @After
    public void tearDown() {
        Awaitility.reset();
    }

    @Test(expected = RuntimeException.class)
    public void listenerExceptionsAreNotCaught() {
        with()
                .catchUncaughtExceptions()
                .conditionEvaluationListener(new ConditionEvaluationListener<Integer>() {
                    public void conditionEvaluated(EvaluatedCondition<Integer> condition) {
                        throw new RuntimeException();
                    }
                })
                .until(new CountDown(10), is(equalTo(0)));
    }

    @Test(timeout = 2000)
    public void settingDefaultHandlerWillImpactAllAwaitStatements() {

        final CountDown globalCountDown = new CountDown(20);

        ConditionEvaluationListener defaultConditionEvaluationListener = new ConditionEvaluationListener<Integer>() {
            public void conditionEvaluated(EvaluatedCondition<Integer> condition) {
                try {
                    if (!condition.isSatisfied()) {
                        globalCountDown.call();
                    }
                } catch (Exception ignored) {
                }
            }
        };
        Awaitility.setDefaultConditionEvaluationListener(defaultConditionEvaluationListener);

        with().until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(15)));

        with().until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(10)));
    }

    @Test(timeout = 2000)
    public void defaultHandlerCanBeDisabledPerAwaitStatement() {

        final CountDown globalCountDown = new CountDown(20);

        ConditionEvaluationListener defaultConditionEvaluationListener = new ConditionEvaluationListener<Integer>() {
            public void conditionEvaluated(EvaluatedCondition<Integer> condition) {
                try {
                    if (!condition.isSatisfied()) {
                        globalCountDown.call();
                    }
                } catch (Exception ignored) {
                }
            }
        };
        setDefaultConditionEvaluationListener(defaultConditionEvaluationListener);

        with().until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(15)));

        with()
                .conditionEvaluationListener(null)
                .until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(15)));

        with().until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(10)));
    }

    @Test(timeout = 2000)
    public void afterAwaitilityResetNoDefaultHandlerIsSet() {
        final CountDown globalCountDown = new CountDown(20);

        ConditionEvaluationListener defaultConditionEvaluationListener = new ConditionEvaluationListener<Integer>() {
            public void conditionEvaluated(EvaluatedCondition<Integer> condition) {
                try {
                    if (!condition.isSatisfied()) {
                        globalCountDown.call();
                    }
                } catch (Exception ignored) {
                }
            }
        };
        setDefaultConditionEvaluationListener(defaultConditionEvaluationListener);

        with().until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(15)));

        Awaitility.reset();

        with().until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(15)));
    }

    @Test(timeout = 10000)
    public void conditionResultsCanBeLoggedToSystemOut() {
        with()
                .conditionEvaluationListener(new ConditionEvaluationListener<Integer>() {
                    public void conditionEvaluated(EvaluatedCondition<Integer> condition) {
                        if (condition.isSatisfied()) {
                            System.out.printf("%s (in %ds)%n", condition.getDescription(), condition.getElapsedTimeInMS() / 1000);
                        } else {
                            System.out.printf("%s (elapsed time %ds, remaining time %ds)%n", condition.getDescription(), condition.getElapsedTimeInMS() / 1000, condition.getRemainingTimeInMS() / 1000);
                        }
                    }
                })
                .pollInterval(Duration.ONE_SECOND)
                .atMost(Duration.TEN_SECONDS)
                .until(new CountDown(5), is(equalTo(0)));
    }

    @Test(timeout = 2000)
    public void conditionResultsCanBeBuffered() {
        final List<String> buffer = new ArrayList<String>();
        with()
                .conditionEvaluationListener(new ConditionEvaluationListener<Integer>() {
                    public void conditionEvaluated(EvaluatedCondition<Integer> condition) {
                        String msg = String.format("%s (elapsed time %ds, remaining time %ds)%n", condition.getDescription(), condition.getElapsedTimeInMS() / 1000, condition.getRemainingTimeInMS() / 1000);
                        buffer.add(msg);
                    }
                })
                .until(new CountDown(5), is(equalTo(0)));

        assertThat(buffer.size(), is(equalTo(5 + 1)));
    }


    @Test(timeout = 2000)
    public void expectedMismatchMessageForComplexMatchers() {
        final ValueHolder<String> lastMismatchMessage = new ValueHolder<String>();
        with()
                .conditionEvaluationListener(new ConditionEvaluationListener<CountDownBean>() {
                    public void conditionEvaluated(EvaluatedCondition<CountDownBean> condition) {
                        if (!condition.isSatisfied()) {
                            lastMismatchMessage.value = condition.getDescription();
                        }
                    }
                })
                .until(new CountDownProvider(new CountDownBean(10, 20)), samePropertyValuesAs(new CountDownBean(10, 10)));

        String expectedMismatchMessage = String.format("%s expected same property values as CountDownBean [countDown: <10>, secondCountDown: <10>] but secondCountDown was <11>", CountDownProvider.class.getName());
        assertThat(lastMismatchMessage.value, is(equalTo(expectedMismatchMessage)));

    }

    @Test(timeout = 2000)
    public void expectedMismatchMessage() {
        final ValueHolder<String> lastMismatchMessage = new ValueHolder<String>();
        with()
                .conditionEvaluationListener(new ConditionEvaluationListener<Integer>() {
                    public void conditionEvaluated(EvaluatedCondition<Integer> condition) {
                        if (!condition.isSatisfied()) {
                            lastMismatchMessage.value = condition.getDescription();
                        }
                    }
                })
                .until(new CountDown(10), is(equalTo(5)));

        String expectedMismatchMessage = String.format("%s expected <5> but was <6>", CountDown.class.getName());
        assertThat(lastMismatchMessage.value, is(equalTo(expectedMismatchMessage)));

    }

    @Test(timeout = 2000)
    public void expectedMatchMessage() {
        final ValueHolder<String> lastMatchMessage = new ValueHolder<String>();
        with()
                .conditionEvaluationListener(new ConditionEvaluationListener<Integer>() {
                    public void conditionEvaluated(EvaluatedCondition<Integer> condition) {
                        lastMatchMessage.value = condition.getDescription();
                    }
                })
                .until(new CountDown(10), is(equalTo(5)));

        String expectedMatchMessage = String.format("%s reached its end value of <5>", CountDown.class.getName());
        assertThat(lastMatchMessage.value, is(equalTo(expectedMatchMessage)));
    }

    @Test(timeout = 2000)
    public void awaitingForeverReturnsLongMaxValueAsRemainingTime() {
        final Set<Long> remainingTimes = new HashSet<Long>();
        final Set<Long> elapsedTimes = new HashSet<Long>();
        with()
                .conditionEvaluationListener(new ConditionEvaluationListener<Integer>() {
                    public void conditionEvaluated(EvaluatedCondition<Integer> condition) {
                        remainingTimes.add(condition.getRemainingTimeInMS());
                        elapsedTimes.add(condition.getElapsedTimeInMS());
                    }
                })
                .forever()
                .until(new CountDown(10), is(equalTo(5)));

        assertThat(remainingTimes, everyItem(is(Long.MAX_VALUE)));
        assertThat(elapsedTimes, everyItem(is(not(Long.MAX_VALUE))));
    }

    private static class CountDown implements Callable<Integer> {

        private int countDown;

        private CountDown(int countDown) {
            this.countDown = countDown;
        }

        public Integer call() throws Exception {
            return countDown--;
        }

        public Integer get() {
            return countDown;
        }
    }

    public static class CountDownBean {

        private int countDown;
        private int secondCountDown;

        private CountDownBean(int countDown, int secondCountDown) {
            this.countDown = countDown;
            this.secondCountDown = secondCountDown;
        }

        public int getCountDown() {
            return countDown;
        }

        public void setCountDown(int countDown) {
            this.countDown = countDown;
        }

        public int getSecondCountDown() {
            return secondCountDown;
        }

        public void setSecondCountDown(int secondCountDown) {
            this.secondCountDown = secondCountDown;
        }
    }

    private static class CountDownProvider implements Callable<CountDownBean> {


        private final CountDownBean countDown;


        private CountDownProvider(CountDownBean countDown) {
            this.countDown = countDown;
        }


        public CountDownBean call() throws Exception {
            countDown.setSecondCountDown(countDown.getSecondCountDown() - 1);
            return get();
        }

        public CountDownBean get() {
            return countDown;
        }
    }


    private static class ValueHolder<T> {
        T value;
    }
}
