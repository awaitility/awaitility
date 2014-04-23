package com.jayway.awaitility.core;

/**
 * A runtime exception thrown by Awaitility when a condition was not fulfilled within the specified threshold.
 *
 */
public class ConditionTimeoutException extends RuntimeException {

    /**
     * <p>Constructor for ConditionTimeoutException.</p>
     *
     * @param message A description of why the timeout occurred.
     */
    public ConditionTimeoutException(String message) {
        super(message);
    }
}
