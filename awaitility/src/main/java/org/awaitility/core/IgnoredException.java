package org.awaitility.core;

public class IgnoredException {
    private final Throwable trace;
    private final long elapsedTimeInMS;
    private final long remainingTimeInMS;
    private final String alias;

    public IgnoredException(Throwable trace, long elapsedTimeInMS, long remainingTimeInMS, String alias) {
        this.trace = trace;
        this.elapsedTimeInMS = elapsedTimeInMS;
        this.remainingTimeInMS = remainingTimeInMS;
        this.alias = alias;
    }

    public Throwable getTrace() {
        return trace;
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
