package com.jayway.awaitility.constraint;

import com.jayway.awaitility.Duration;

public class AtMostWaitConstraint implements WaitConstraint {

    private final Duration atMostDuration;

    public static AtMostWaitConstraint FOREVER = new AtMostWaitConstraint(Duration.FOREVER);

    public static AtMostWaitConstraint TEN_SECONDS = new AtMostWaitConstraint(Duration.TEN_SECONDS);

    public AtMostWaitConstraint(Duration atMostDuration) {
        this.atMostDuration = atMostDuration;
    }

    public Duration getMaxWaitTime() {
        return atMostDuration;
    }

    public Duration getMinWaitTime() {
        return Duration.ZERO;
    }

    public WaitConstraint withMinWaitTime(Duration minWaitTime) {
        return new IntervalWaitConstraint(minWaitTime, atMostDuration);
    }

    public WaitConstraint withMaxWaitTime(Duration maxWaitTime) {
        return new AtMostWaitConstraint(maxWaitTime);
    }
}
