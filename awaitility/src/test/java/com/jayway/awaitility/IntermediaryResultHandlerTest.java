package com.jayway.awaitility;

import com.jayway.awaitility.core.IntermediaryResultHandler;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.jayway.awaitility.Awaitility.setDefaultIntermediaryResultHandler;
import static com.jayway.awaitility.Awaitility.with;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class IntermediaryResultHandlerTest {

    @Before
    public void setUp() {
        Awaitility.reset();
    }

    @Test(expected = RuntimeException.class)
    public void handlerExceptionsAreNotCaught() {
        with()
                .catchUncaughtExceptions()
                .intermediaryResultHandler(new IntermediaryResultHandler() {
                    public void handle(String mismatchMessage, long elapsedTimeInMS, long remainingTimeInMS) {
                        throw new RuntimeException();
                    }
                })
                .until(new CountDown(10), is(equalTo(0)));
    }

    @Test (timeout = 2000)
    public void settingDefaultHandlerWillImpactAllAwaitStatements() {

        final CountDown globalCountDown = new CountDown(20);

        IntermediaryResultHandler defaultIntermediaryResultHandler = new IntermediaryResultHandler() {
            public void handle(String mismatchMessage, long elapsedTimeInMS, long remainingTimeInMS) {
                try {
                    globalCountDown.call();
                } catch (Exception e) {
                }
            }
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

        IntermediaryResultHandler defaultIntermediaryResultHandler = new IntermediaryResultHandler() {
            public void handle(String mismatchMessage, long elapsedTimeInMS, long remainingTimeInMS) {
                try {
                    globalCountDown.call();
                } catch (Exception e) {
                }
            }
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

        IntermediaryResultHandler defaultIntermediaryResultHandler = new IntermediaryResultHandler() {
            public void handle(String mismatchMessage, long elapsedTimeInMS, long remainingTimeInMS) {
                try {
                    globalCountDown.call();
                } catch (Exception e) {
                }
            }
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
                .intermediaryResultHandler(new IntermediaryResultHandler() {
                    public void handle(String mismatchMessage, long elapsedTimeInMS, long remainingTimeInMS) {
                        System.out.printf("%s (elapsed time %ds, remaining time %ds)\n", mismatchMessage, elapsedTimeInMS / 1000, remainingTimeInMS / 1000);
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
                .intermediaryResultHandler(new IntermediaryResultHandler() {
                    public void handle(String mismatchMessage, long elapsedTimeInMS, long remainingTimeInMS) {
                        String msg = String.format("%s (elapsed time %ds, remaining time %ds)\n", mismatchMessage, elapsedTimeInMS / 1000, remainingTimeInMS / 1000);
                        buffer.add(msg);
                    }
                })
                .until(new CountDown(5), is(equalTo(0)));

        assertThat(buffer.size(), is(equalTo(5)));
    }

    @Test(timeout = 2000)
    public void expectedMismatchMessage() {
        final ValueHolder<String> lastMismatchMessage = new ValueHolder<String>();
        with()
                .intermediaryResultHandler(new IntermediaryResultHandler() {
                    public void handle(String mismatchMessage, long elapsedTimeInMS, long remainingTimeInMS) {
                        lastMismatchMessage.value = mismatchMessage;
                    }
                })
                .until(new CountDown(10), is(equalTo(5)));

        String expectedMismatchMessage = String.format("%s expected <5> but was <6>", CountDown.class.getName());
        assertThat(lastMismatchMessage.value, is(equalTo(expectedMismatchMessage)));

    }

    @Test(timeout = 2000)
    public void awaitingForeverReturnsLongMaxValueAsRemainingTime() {
        final Set<Long> remainingTimes = new HashSet<Long>();
        final Set<Long> elapsedTimes = new HashSet<Long>();
        with()
                .intermediaryResultHandler(new IntermediaryResultHandler() {
                    public void handle(String mismatchMessage, long elapsedTimeInMS, long remainingTimeInMS) {
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

    private static class ValueHolder<T> {
        T value;
    }
}
