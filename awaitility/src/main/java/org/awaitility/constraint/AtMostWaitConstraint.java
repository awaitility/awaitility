package org.awaitility.constraint;

import org.awaitility.Duration;

public class AtMostWaitConstraint implements WaitConstraint {

    private final Duration atMostDuration;

    public static final AtMostWaitConstraint FOREVER = new AtMostWaitConstraint(Duration.FOREVER);

    public static final AtMostWaitConstraint TEN_SECONDS = new AtMostWaitConstraint(Duration.TEN_SECONDS);

    AtMostWaitConstraint(Duration atMostDuration) {
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
