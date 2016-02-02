package com.jayway.awaitility.core;

import org.hamcrest.Matcher;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

public class AggregatingCallable<T> implements Callable<List<T>> {

    private final List<T> collectedResults = new CopyOnWriteArrayList<T>();
    private final Callable<T> callable;
    private final Matcher<T> matcher;

    public AggregatingCallable(Callable<T> callable, Matcher<T> matcher) {
        this.callable = callable;
        this.matcher = matcher;
    }

    public List<T> call() throws Exception {
        T result = callable.call();
        if (matcher.matches(result)) {
            collectedResults.add(result);
        }
        return collectedResults;
    }
}
