package org.awaitility.constraint;

import org.awaitility.Duration;

public class IntervalWaitConstraint extends AtMostWaitConstraint {

    private final Duration atLeastConstraint;

    private static IntervalWaitConstraint between(Duration notBeforeThan, Duration notLaterThan) {
        return new IntervalWaitConstraint(notBeforeThan, notLaterThan);
    }

    IntervalWaitConstraint(Duration atLeastConstraint, Duration atMostDuration) {
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
