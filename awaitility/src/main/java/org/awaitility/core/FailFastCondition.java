package org.awaitility.core;

import java.util.concurrent.Callable;

public class FailFastCondition {

    private final Callable<Boolean> failFastCondition;
    private final Callable<Exception> failureReason;

    public FailFastCondition(final Callable<Boolean> failFastCondition, final Callable<Exception> failureReason) {
        this.failFastCondition = failFastCondition;
        this.failureReason = failureReason;
    }

    public Callable<Boolean> getFailFastCondition() {
        return this.failFastCondition;
    }

    public Callable<Exception> getFailureReason() {
        return this.failureReason;
    }
}
