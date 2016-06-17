package org.awaitility.constraint;

import org.awaitility.Duration;

/**
 * Interface that defines the wait strategy.
 * Max wait time stands for upper bound of condition evaluation duration. Min wait time stands for lower bound condition
 * evaluation duration. In case operation is executed before getMinWaitTime the exception is thrown indicating that
 * condition shouldn't be executed earlier than specified amount of time. Also operation is not allowed to be
 * executed longer than getMaxWaitTime.
 */
public interface WaitConstraint {

    Duration getMaxWaitTime();

    Duration getMinWaitTime();

    WaitConstraint withMinWaitTime(Duration minWaitTime);

    WaitConstraint withMaxWaitTime(Duration maxWaitTime);
}
