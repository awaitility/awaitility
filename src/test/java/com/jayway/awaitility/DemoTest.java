package com.jayway.awaitility;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.awaitility.synchronizer.ConditionFactory.callTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.Callable;

import org.junit.Test;

public class DemoTest {
    
    @Test
    public void testUsingCallable() throws Exception {
        final CounterService service = new CounterServiceImpl();
        service.run();
        await().until(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return service.getCount()==1;
            }
        });
    }

    @Test
    public void testUsingCallTo() throws Exception {
        final CounterService service = new CounterServiceImpl();
        service.run();
        await().until(callTo(service).getCount(), is(equalTo(1)));
    }

    @Test
    public void testUsingGreaterThan() throws Exception {
        final CounterService service = new CounterServiceImpl();
        service.run();
        await().until(callTo(service).getCount(), greaterThan(2));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCrash() throws Exception {
        final CounterService service = new CounterServiceImpl(new IllegalArgumentException());
        service.run();
        await().until(callTo(service).getCount(), is(equalTo(1)));
    }
}

interface CounterService extends Runnable {
    int getCount();
}

class CounterServiceImpl implements CounterService {
    private volatile int count=0;
    private final RuntimeException exception;

    public CounterServiceImpl() {
        this.exception = null;
    }

    public CounterServiceImpl(RuntimeException exception) {
        this.exception = exception;
    }
    public void run() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    for (int indx=0; indx<5; indx++) {
                        Thread.sleep(1000);
                        if (exception != null) {
                            throw exception;
                        }
                        count += 1;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
    public int getCount() {
        return count;
    }
}