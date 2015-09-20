package com.jayway.awaitility.core;

/**
 * @param <T> the type of the input to the function
 */
public interface Predicate<T> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    boolean matches(T t);

}
