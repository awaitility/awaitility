package com.jayway.awaitility;

import com.jayway.awaitility.core.IntermediaryResultHandler;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.jayway.awaitility.Awaitility.handleIntermediaryResultsWith;
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
                .handleIntermediaryResultsWith(new IntermediaryResultHandler() {
                    public void handle(String mismatchMessage, long elapsedTimeInMS, Long remainingTimeInMS) {
                        throw new RuntimeException();
                    }
                })
                .until(new CountDown(10), is(equalTo(0)));
    }

    @Test
    public void settingDefaultHandlerWillImpactAllAwaitStatements() {

        final CountDown globalCountDown = new CountDown(20);

        IntermediaryResultHandler defaultIntermediaryResultHandler = new IntermediaryResultHandler() {
            public void handle(String mismatchMessage, long elapsedTimeInMS, Long remainingTimeInMS) {
                try {
                    globalCountDown.call();
                } catch (Exception e) {
                }
            }
        };
        Awaitility.handleIntermediaryResultsWith(defaultIntermediaryResultHandler);

        with().until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(15)));

        with().until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(10)));
    }

    @Test
    public void defaultHandlerCanBeDisabledPerAwaitStatement() {

        final CountDown globalCountDown = new CountDown(20);

        IntermediaryResultHandler defaultIntermediaryResultHandler = new IntermediaryResultHandler() {
            public void handle(String mismatchMessage, long elapsedTimeInMS, Long remainingTimeInMS) {
                try {
                    globalCountDown.call();
                } catch (Exception e) {
                }
            }
        };
        handleIntermediaryResultsWith(defaultIntermediaryResultHandler);

        with().until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(15)));

        with()
                .handleIntermediaryResultsWith(null)
                .until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(15)));

        with().until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(10)));
    }

    @Test
    public void afterAwaitilityResetNoDefaultHandlerIsSet() {
        final CountDown globalCountDown = new CountDown(20);

        IntermediaryResultHandler defaultIntermediaryResultHandler = new IntermediaryResultHandler() {
            public void handle(String mismatchMessage, long elapsedTimeInMS, Long remainingTimeInMS) {
                try {
                    globalCountDown.call();
                } catch (Exception e) {
                }
            }
        };
        handleIntermediaryResultsWith(defaultIntermediaryResultHandler);

        with().until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(15)));

        Awaitility.reset();

        with()
                .until(new CountDown(5), is(equalTo(0)));
        assertThat(globalCountDown.get(), is(equalTo(15)));
    }

    @Test
    public void intermediaryResultsCanBeLoggedOnAWay() {
        with()
                .handleIntermediaryResultsWith(new IntermediaryResultHandler() {
                    public void handle(String mismatchMessage, long elapsedTimeInMS, Long remainingTimeInMS) {
                        System.out.printf("%s (elapsed time %ds, remaining time %ds)\n", mismatchMessage, elapsedTimeInMS / 1000, remainingTimeInMS / 1000);
                    }
                })
                .pollInterval(Duration.ONE_SECOND)
                .atMost(Duration.TEN_SECONDS)
                .until(new CountDown(5), is(equalTo(0)));
    }

    @Test
    public void intermediaryResultsCanBeBuffered() {
        final List<String> buffer = new ArrayList<String>();
        with()
                .handleIntermediaryResultsWith(new IntermediaryResultHandler() {
                    public void handle(String mismatchMessage, long elapsedTimeInMS, Long remainingTimeInMS) {
                        String msg = String.format("%s (elapsed time %ds, remaining time %ds)\n", mismatchMessage, elapsedTimeInMS / 1000, remainingTimeInMS / 1000);
                        buffer.add(msg);
                    }
                })
                .until(new CountDown(5), is(equalTo(0)));

        assertThat(buffer.size(), is(equalTo(5)));
    }

    @Test
    public void expectedMismatchMessage() {
        final ValueHolder<String> lastMismatchMessage = new ValueHolder<String>();
        with()
                .handleIntermediaryResultsWith(new IntermediaryResultHandler() {
                    public void handle(String mismatchMessage, long elapsedTimeInMS, Long remainingTimeInMS) {
                        lastMismatchMessage.value = mismatchMessage;
                    }
                })
                .until(new CountDown(10), is(equalTo(5)));

        String expectedMismatchMessage = String.format("%s expected <5> but was <6>", CountDown.class.getName());
        assertThat(lastMismatchMessage.value, is(equalTo(expectedMismatchMessage)));

    }

    @Test
    public void awaitingForeverShouldReturnNullRemainingTime() {
        final Set<Long> remainingTimes = new HashSet<Long>();
        final Set<Long> elapsedTimes = new HashSet<Long>();
        with()
                .handleIntermediaryResultsWith(new IntermediaryResultHandler() {
                    public void handle(String mismatchMessage, long elapsedTimeInMS, Long remainingTimeInMS) {
                        remainingTimes.add(remainingTimeInMS);
                        elapsedTimes.add(elapsedTimeInMS);
                    }
                })
                .forever()
                .until(new CountDown(10), is(equalTo(5)));

        assertThat(remainingTimes, everyItem(is(equalTo((Long) null))));
        assertThat(elapsedTimes, everyItem(is(not(equalTo((Long) null)))));
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
