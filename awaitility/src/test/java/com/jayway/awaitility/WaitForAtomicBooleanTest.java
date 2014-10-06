package com.jayway.awaitility;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.awaitility.Duration.FIVE_SECONDS;
import static org.hamcrest.CoreMatchers.equalTo;

public class WaitForAtomicBooleanTest {
    private AtomicBoolean wasAdded;
    private AtomicBoolean wasAddedWithDefaultValue;

    @Before
    public void setup() {
        wasAdded = new AtomicBoolean(false);
        wasAddedWithDefaultValue = new AtomicBoolean();
    }

    @Test(timeout = 2000L)
    public void atomicBooleanExample() throws Exception {
        new WasAddedModifier().start();

        await().atMost(FIVE_SECONDS).until(wasAdded(), equalTo(true));
    }

    @Test(timeout = 2000L)
    public void atomicBooleanWithUntilTrueWhenBooleanUsesDefaultValue() throws Exception {
        new WasAddedWithDefaultValue().start();

        await().atMost(FIVE_SECONDS).untilTrue(wasAddedWithDefaultValue);
    }

    @Test(timeout = 2000L)
    public void atomicBooleanWithUntilTrue() throws Exception {
        new WasAddedModifier().start();

        await().atMost(FIVE_SECONDS).untilTrue(wasAdded);
    }

    @Test(timeout = 2000L)
    public void atomicBooleanWithUntilFalse() throws Exception {
        wasAdded.set(true);
        new WasAddedModifier().start();

        await().atMost(FIVE_SECONDS).untilFalse(wasAdded);
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
            wasAdded.set(!wasAdded.get());
        }
    }

    private class WasAddedWithDefaultValue extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            wasAddedWithDefaultValue.set(true);
        }
    }
}
