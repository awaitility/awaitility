package com.jayway.awaitility.constraint;

import com.jayway.awaitility.Duration;

public class IntervalWaitConstraint extends AtMostWaitConstraint {

    private final Duration atLeastConstraint;

    public static IntervalWaitConstraint between(Duration notBeforeThan, Duration notLaterThan) {
        return new IntervalWaitConstraint(notBeforeThan, notLaterThan);
    }

    public IntervalWaitConstraint(Duration atLeastConstraint, Duration atMostDuration) {
        super(atMostDuration);
        this.atLeastConstraint = atLeastConstraint;
    }

    @Override
    public Duration getMinWaitTime() {
        return atLeastConstraint;
    }

    @Override
    public WaitConstraint withMaxWaitTime(Duration maxWaitTime) {
        return between(atLeastConstraint, maxWaitTime);
    }
}
