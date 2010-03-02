package com.jayway.concurrenttest.synchronizer;

import java.util.concurrent.Callable;

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
}
