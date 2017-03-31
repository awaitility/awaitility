package org.awaitility.core;

/**
 * A bi-function (because Awaitility doesn't use Java 8)
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <R> the type of the result of the function
 */
public interface BiFunction<T, U, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param arg1 The first argument
     * @param arg2 The second argument
     * @return The result
     */
    R apply(T arg1, U arg2);
}