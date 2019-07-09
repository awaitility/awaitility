package org.awaitility.constraint;


import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class AtMostWaitConstraint implements WaitConstraint {

    private final Duration atMostDuration;

    public static final AtMostWaitConstraint FOREVER = new AtMostWaitConstraint(ChronoUnit.FOREVER.getDuration());

    public static final AtMostWaitConstraint TEN_SECONDS = new AtMostWaitConstraint(Duration.ofSeconds(10));

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
