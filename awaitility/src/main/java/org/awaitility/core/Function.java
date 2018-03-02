package org.awaitility.core;

/**
 * A function (because Awaitility doesn't require Java 8)
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
public interface Function<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param input The input
     * @return The result
     */
    R apply(T input);
}