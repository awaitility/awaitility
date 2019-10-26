package org.awaitility.constraint;

import java.time.Duration;

public class HoldsPredicateWaitConstraint extends IntervalWaitConstraint {
  private final Duration holdConditionTime;

  HoldsPredicateWaitConstraint(Duration atLeastConstraint, Duration atMostDuration, Duration holdConditionTime) {
    super(atLeastConstraint, atMostDuration);
    this.holdConditionTime = holdConditionTime;
  }

  @Override
  public Duration getHoldPredicateTime() {
    return holdConditionTime;
  }

  @Override
  public WaitConstraint withHoldPredicateTime(Duration holdConditionTime) {
    return new HoldsPredicateWaitConstraint(getMinWaitTime(), getMaxWaitTime(), holdConditionTime);
  }
}
