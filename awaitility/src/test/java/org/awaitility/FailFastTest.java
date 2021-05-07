package org.awaitility;

import org.awaitility.core.TerminalFailureException;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

/**
 * Fast failure on terminal status #178
 * <p>
 * Issue https://github.com/awaitility/awaitility/issues/178
 * <p>
 * PR https://github.com/awaitility/awaitility/pull/193
 */
public class FailFastTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @After
    public void reset_awaitility_after_each_test() {
        Awaitility.reset();
    }

    @Test
    public void fail_fast_throws_terminal_failure_exception_with_the_supplied_failure_reason() {
        exception.expect(TerminalFailureException.class);
        exception.expectMessage(equalTo("System crash"));

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
    }
    
    @Test
    public void fail_fast_throws_terminal_failure_exception_with_the_default_failure_reason_when_no_failure_reason_is_specified() {
        exception.expect(TerminalFailureException.class);
        exception.expectMessage(equalTo("Fail fast condition triggered"));

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
    }

    @Test
    public void statically_configured_fail_fast_condition_without_failure_reason() {
        exception.expect(TerminalFailureException.class);
        exception.expectMessage(equalTo("Fail fast condition triggered"));

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
                .until(ai::get, equalTo(-1)); // will never be true
    }

    @Test
    public void statically_configured_fail_fast_condition_with_failure_reason() {
        exception.expect(TerminalFailureException.class);
        exception.expectMessage(equalTo("System crash"));

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
    }
}