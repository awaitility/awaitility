package com.jayway.awaitility.core;

/**
 * This interface replaces {@link Runnable} in cases when
 * execution of {@link #run()} method may throw exception.
 *
 * Useful for capturing lambdas that throw exceptions.
 */
public interface ThrowingRunnable {

    void run() throws Exception;
}
