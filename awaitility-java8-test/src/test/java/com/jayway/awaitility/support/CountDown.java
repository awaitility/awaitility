package com.jayway.awaitility.support;

import org.junit.Ignore;

import java.util.concurrent.Callable;

@Ignore("Not a test")
public class CountDown implements Callable<Integer> {

    private int countDown;

    public CountDown(int countDown) {
        this.countDown = countDown;
    }

    public Integer call() throws Exception {
        return countDown--;
    }

    public Integer get() {
        return countDown;
    }
}