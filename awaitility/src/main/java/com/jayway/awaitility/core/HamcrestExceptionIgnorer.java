package com.jayway.awaitility.core;

import org.hamcrest.Matcher;

public class HamcrestExceptionIgnorer implements ExceptionIgnorer {

    private final Matcher<? super Exception> matcher;

    public HamcrestExceptionIgnorer(Matcher<? super Exception> matcher) {
        if (matcher == null) {
            throw new IllegalArgumentException("matcher cannot be null");
        }
        this.matcher = matcher;
    }

    public boolean shouldIgnoreException(Exception exception) {
        return matcher.matches(exception);
    }
}
