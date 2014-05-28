package com.jayway.awaitility;

import com.jayway.awaitility.core.IntermediaryResultHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.jayway.awaitility.Awaitility.setDefaultIntermediaryResultHandler;
import static com.jayway.awaitility.Awaitility.with;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class IntermediaryResultHandlerTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();


    @Before
    public void setUp() {
        Awaitility.reset();
    }

    @Test(expected = RuntimeException.class)
    public void handlerExceptionsAreNotCaught() {
        with()
                .catchUncaughtExceptions()
                .intermediaryResultHandler(new IntermediaryResultHandler<Integer>() {
                    public void handleMismatch(String mismatchMessage, Integer value, long elapsedTimeInMS, long remainingTimeInMS) {
                        throw new RuntimeException();
                    }
                    public void handleMatch(String matchMessage, Integer value, long elapsedTimeInMS, long remainingTimeInMS) {
                        throw new RuntimeException();
                    }
                })
                .until(new CountDown(10), is(equalTo(0)));
    }

    @Test (timeout = 2000)
    public void settingDefaultHandlerWillImpactAllAwaitStatements() {

        final CountDown globalCountDown = new CountDown(20);

        IntermediaryResultHandler defaultIntermediaryResultHandler = new IntermediaryResultHandler<Integer>() {
            public void handleMismatch(String mismatchMessage, Integer value, long elapsedTimeInMS, long remainingTimeInMS) {
                try {
                    globalCountDown.call();
                } catch (Exception e) {
                }
            }

            public void handleMatch(String matchMessage, Integer value, long elapsedTimeInMS, long remainingTimeInMS) { }
        };
        Awaitility.setDefaultIntermediaryResultHandler(defaultIntermediaryResultHandler);

        with().until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(15)));

        with().until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(10)));
    }

    @Test(timeout = 2000)
    public void defaultHandlerCanBeDisabledPerAwaitStatement() {

        final CountDown globalCountDown = new CountDown(20);

        IntermediaryResultHandler defaultIntermediaryResultHandler = new IntermediaryResultHandler<Integer>() {
            public void handleMismatch(String mismatchMessage, Integer value, long elapsedTimeInMS, long remainingTimeInMS) {
                try {
                    globalCountDown.call();
                } catch (Exception e) {
                }
            }

            public void handleMatch(String matchMessage, Integer value, long elapsedTimeInMS, long remainingTimeInMS) { }
        };
        setDefaultIntermediaryResultHandler(defaultIntermediaryResultHandler);

        with().until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(15)));

        with()
                .intermediaryResultHandler(null)
                .until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(15)));

        with().until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(10)));
    }

    @Test(timeout = 2000)
    public void afterAwaitilityResetNoDefaultHandlerIsSet() {
        final CountDown globalCountDown = new CountDown(20);

        IntermediaryResultHandler defaultIntermediaryResultHandler = new IntermediaryResultHandler<Integer>() {
            public void handleMismatch(String mismatchMessage, Integer value, long elapsedTimeInMS, long remainingTimeInMS) {
                try {
                    globalCountDown.call();
                } catch (Exception e) {
                }
            }

            public void handleMatch(String matchMessage, Integer value, long elapsedTimeInMS, long remainingTimeInMS) { }
        };
        setDefaultIntermediaryResultHandler(defaultIntermediaryResultHandler);

        with().until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(15)));

        Awaitility.reset();

        with()
                .until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(15)));
    }

    @Test(timeout = 10000)
    public void intermediaryResultsCanBeLoggedToSystemOut() {
        with()
                .intermediaryResultHandler(new IntermediaryResultHandler<Integer>() {
                    public void handleMismatch(String mismatchMessage, Integer value, long elapsedTimeInMS, long remainingTimeInMS) {
                        System.out.printf("%s (elapsed time %ds, remaining time %ds)\n", mismatchMessage, elapsedTimeInMS / 1000, remainingTimeInMS / 1000);
                    }

                    public void handleMatch(String matchMessage, Integer value, long elapsedTimeInMS, long remainingTimeInMS) {
                        System.out.printf("%s (in %ds)\n", matchMessage, elapsedTimeInMS / 1000);
                    }
                })
                .pollInterval(Duration.ONE_SECOND)
                .atMost(Duration.TEN_SECONDS)
                .until(new CountDown(5), is(equalTo(0)));
    }

    @Test(timeout = 2000)
    public void intermediaryResultsCanBeBuffered() {
        final List<String> buffer = new ArrayList<String>();
        with()
                .intermediaryResultHandler(new IntermediaryResultHandler<Integer>() {
                    public void handleMismatch(String mismatchMessage, Integer value, long elapsedTimeInMS, long remainingTimeInMS) {
                        String msg = String.format("%s (elapsed time %ds, remaining time %ds)\n", mismatchMessage, elapsedTimeInMS / 1000, remainingTimeInMS / 1000);
                        buffer.add(msg);
                    }

                    public void handleMatch(String matchMessage, Integer value, long elapsedTimeInMS, long remainingTimeInMS) {
                        String msg = String.format("%s (in %ds)\n", matchMessage, elapsedTimeInMS / 1000);
                        buffer.add(msg);
                    }
                })
                .until(new CountDown(5), is(equalTo(0)));

        assertThat(buffer.size(), is(equalTo(5+1)));
    }


    @Test(timeout = 2000)
    public void expectedMismatchMessageForComplexMatchers() {
        final ValueHolder<String> lastMismatchMessage = new ValueHolder<String>();
        with()
                .intermediaryResultHandler(new IntermediaryResultHandler<CountDownBean>() {
                    public void handleMismatch(String mismatchMessage, CountDownBean value, long elapsedTimeInMS, long remainingTimeInMS) {
                        lastMismatchMessage.value = mismatchMessage;
                    }

                    public void handleMatch(String matchMessage, CountDownBean value, long elapsedTimeInMS, long remainingTimeInMS) {
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
                .intermediaryResultHandler(new IntermediaryResultHandler<Integer>() {
                    public void handleMismatch(String mismatchMessage, Integer value, long elapsedTimeInMS, long remainingTimeInMS) {
                        lastMismatchMessage.value = mismatchMessage;
                    }

                    public void handleMatch(String matchMessage, Integer value, long elapsedTimeInMS, long remainingTimeInMS) {
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
                .intermediaryResultHandler(new IntermediaryResultHandler<Integer>() {
                    public void handleMismatch(String mismatchMessage, Integer value, long elapsedTimeInMS, long remainingTimeInMS) {
                        lastMatchMessage.value = mismatchMessage;
                    }

                    public void handleMatch(String matchMessage, Integer value, long elapsedTimeInMS, long remainingTimeInMS) {
                        lastMatchMessage.value = matchMessage;
                    }
                })
                .until(new CountDown(10), is(equalTo(5)));

        String expectedMatchMessage = String.format("%s reached its end value of <5>", CountDown.class.getName());
        assertThat(lastMatchMessage.value, is(equalTo(expectedMatchMessage)));

    }

    @Test(timeout = 2000)
    public void throwsClassCastExceptionWhenIntermediaryResultHandlerTypeIsInvalid() {
        exception.expect(ClassCastException.class);
        exception.expectMessage(allOf(startsWith("Cannot apply intermediary result handler com.jayway.awaitility.IntermediaryResultHandlerTest"),
                    endsWith(" because java.lang.Integer cannot be cast to java.lang.String")));
        with().intermediaryResultHandler(new IntermediaryResultHandler<String>() {
                    public void handleMismatch(String mismatchMessage, String value, long elapsedTimeInMS, long remainingTimeInMS) {
                    }

                    public void handleMatch(String matchMessage, String value, long elapsedTimeInMS, long remainingTimeInMS) {
                    }
                })
                .until(new CountDown(10), is(equalTo(5)));
    }

    @Test(timeout = 2000)
    public void throwsClassCastExceptionWhenIntermediaryResultHandlerTypeIsInvalidAndResultMatches() {
        exception.expect(ClassCastException.class);
        exception.expectMessage(allOf(startsWith("Cannot apply intermediary result handler com.jayway.awaitility.IntermediaryResultHandlerTest"),
                    endsWith(" because java.lang.Integer cannot be cast to java.lang.String")));
        with().intermediaryResultHandler(new IntermediaryResultHandler<String>() {
                    public void handleMismatch(String mismatchMessage, String value, long elapsedTimeInMS, long remainingTimeInMS) {
                    }

                    public void handleMatch(String matchMessage, String value, long elapsedTimeInMS, long remainingTimeInMS) {
                    }
                })
                .until(new CountDown(5), is(equalTo(5)));
    }

    @Test(timeout = 2000)
    public void awaitingForeverReturnsLongMaxValueAsRemainingTime() {
        final Set<Long> remainingTimes = new HashSet<Long>();
        final Set<Long> elapsedTimes = new HashSet<Long>();
        with()
                .intermediaryResultHandler(new IntermediaryResultHandler<Integer>() {
                    public void handleMismatch(String mismatchMessage, Integer value, long elapsedTimeInMS, long remainingTimeInMS) {
                        remainingTimes.add(remainingTimeInMS);
                        elapsedTimes.add(elapsedTimeInMS);
                    }

                    public void handleMatch(String matchMessage, Integer value, long elapsedTimeInMS, long remainingTimeInMS) {
                        remainingTimes.add(remainingTimeInMS);
                        elapsedTimes.add(elapsedTimeInMS);
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
            this.countDown = countDown; this.secondCountDown = secondCountDown;
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
            countDown.setSecondCountDown(countDown.getSecondCountDown()-1);
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
