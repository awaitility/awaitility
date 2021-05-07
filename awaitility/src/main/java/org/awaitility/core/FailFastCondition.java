package org.awaitility.core;

import java.util.concurrent.Callable;

public class FailFastCondition {
    private static final String DEFAULT_FAILURE_REASON = "Fail fast condition triggered";

    private final Callable<Boolean> failFastCondition;
    private final String failFastFailureReason;

    public FailFastCondition(String failFastFailureReason, Callable<Boolean> failFastCondition) {
        this.failFastCondition = failFastCondition;
        this.failFastFailureReason = failFastFailureReason == null ? DEFAULT_FAILURE_REASON : failFastFailureReason;
    }

    public Callable<Boolean> getFailFastCondition() {
        return this.failFastCondition;
    }

    public String getFailFastFailureReason() {
        return this.failFastFailureReason;
    }
}