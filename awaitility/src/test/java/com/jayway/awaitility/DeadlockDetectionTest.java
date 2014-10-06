package com.jayway.awaitility;

import com.jayway.awaitility.core.ConditionTimeoutException;
import com.jayway.awaitility.core.DeadlockException;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DeadlockDetectionTest {

    @Test(timeout = 2000L)
    public void deadlockTest() throws Exception {
        final Object lock1 = new Object();
        final Object lock2 = new Object();

        final AtomicBoolean locked = new AtomicBoolean();

        Thread t1 = new Thread() {
            public void run() {
                synchronized(lock1) {
                    try{
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {}

                    synchronized(lock2) {
                        locked.set(true);
                    }
                }
            }
        };

        Thread t2 = new Thread(){
            public void run(){
                synchronized(lock2) {
                    try{
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {}

                    synchronized(lock1) {
                        locked.set(true);
                    }
                }
            }
        };

        t1.start();
        t2.start();

        try {
            await().atMost(Duration.ONE_SECOND).untilTrue(locked);
            fail("ConditionTimeoutException expected.");

        } catch (ConditionTimeoutException e) {
            DeadlockException cause = (DeadlockException) e.getCause();
            assertTrue(cause instanceof DeadlockException);
            assertEquals(2, cause.getThreadInfos().length);
        }
    }
}
