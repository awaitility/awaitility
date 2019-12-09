package org.awaitility.core;

public class IgnoredException {
    private final Throwable throwable;
    private final long elapsedTimeInMS;
    private final long remainingTimeInMS;
    private final String alias;

    public IgnoredException(Throwable throwable, long elapsedTimeInMS, long remainingTimeInMS, String alias) {
        this.throwable = throwable;
        this.elapsedTimeInMS = elapsedTimeInMS;
        this.remainingTimeInMS = remainingTimeInMS;
        this.alias = alias;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public long getElapsedTimeInMS() {
        return elapsedTimeInMS;
    }

    public long getRemainingTimeInMS() {
        return remainingTimeInMS;
    }

    public String getAlias() {
        return alias;
    }
}
