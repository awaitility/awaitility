package com.jayway.awaitility;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.awaitility.Duration.FIVE_SECONDS;
import static org.hamcrest.CoreMatchers.equalTo;

public class WaitForAtomicBooleanTest {
    private AtomicBoolean wasAdded = new AtomicBoolean(false);

    @Test(timeout = 2000L)
    public void atomicBooleanExample() throws Exception {
        new WasAddedModifier().start();

        await().atMost(FIVE_SECONDS).until(wasAdded(), equalTo(true));
    }

    private Callable<Boolean> wasAdded() {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return wasAdded.get();
            }
        };
    }

    private class WasAddedModifier extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            wasAdded.set(true);
        }
    }
}
