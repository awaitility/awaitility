package com.jayway.awaitility.core;

public class PredicateExceptionIgnorer implements ExceptionIgnorer {

    private final Predicate<Exception> predicate;

    public PredicateExceptionIgnorer(Predicate<Exception> predicate) {
        if (predicate == null) {
            throw new IllegalArgumentException("predicate cannot be null");
        }
        this.predicate = predicate;
    }

    public boolean shouldIgnoreException(Exception exception) {
        return predicate.matches(exception);
    }
}
