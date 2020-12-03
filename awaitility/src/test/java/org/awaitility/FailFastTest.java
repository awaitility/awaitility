package org.awaitility;

import org.awaitility.core.TerminalFailureException;
import org.junit.Test;

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

    @Test(expected = TerminalFailureException.class)
    public void fail_fast() {
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
                .failFast(failFastCondition, () -> new Exception("System crash")).until(() ->
                        ai.get(),
                equalTo(-1) // will never be true
        );
    }
}
