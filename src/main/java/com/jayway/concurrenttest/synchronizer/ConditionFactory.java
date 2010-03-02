package com.jayway.concurrenttest.synchronizer;

import java.util.concurrent.Callable;

import org.hamcrest.Matcher;

import com.jayway.concurrenttest.synchronizer.ConditionOptions.MethodCaller;

public class ConditionFactory {
    private final Duration timeout;
    private final Duration pollInterval;
    private final boolean catchUncaughtExceptions;

    public ConditionFactory(Duration timeout, Duration pollInterval, boolean catchUncaughtExceptions) {
        if (pollInterval == null) {
            throw new IllegalArgumentException("pollInterval cannot be null");
        }
        if (timeout == null) {
            throw new IllegalArgumentException("timeout cannot be null");
        }
        this.timeout = timeout;
        this.pollInterval = pollInterval;
        this.catchUncaughtExceptions = catchUncaughtExceptions;
    }

    public ConditionFactory andTimeout(Duration timeout) {
        return new ConditionFactory(timeout, pollInterval, catchUncaughtExceptions);
    }

    public ConditionFactory waitAtMost(Duration timeout) {
        return new ConditionFactory(timeout, pollInterval, catchUncaughtExceptions);
    }

    public ConditionFactory andPollInterval(Duration pollInterval) {
        return new ConditionFactory(timeout, pollInterval, catchUncaughtExceptions);
    }

    public ConditionFactory andCatchUncaughtExceptions() {
        return new ConditionFactory(timeout, pollInterval, true);
    }

    public void await(Callable<Boolean> conditionEvaluator) throws Exception {
        AwaitConditionImpl condition = new AwaitConditionImpl(timeout, conditionEvaluator, pollInterval);
        if (catchUncaughtExceptions) {
            condition.andCatchAllUncaughtExceptions();
        }
        condition.await();
    }
    
    
    public <T> void until(T ignore, final Matcher<T> matcher) throws Exception {
        await(ConditionOptions.until(ignore, matcher));
    }

    public <T> void until(final Callable<T> supplier, final Matcher<T> matcher) throws Exception {
        await(ConditionOptions.until(supplier, matcher));
    }
}
