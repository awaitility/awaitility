/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.awaitility.core;

import org.awaitility.constraint.WaitConstraint;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** A minimum wait based on an absolute instant, resolved when evaluation starts. */
final class InstantWaitConstraint implements WaitConstraint {
    private final WaitConstraint delegate;
    private final Instant instant;

    InstantWaitConstraint(WaitConstraint delegate, Instant instant) {
        this.delegate = Objects.requireNonNull(delegate, "delegate").withMinWaitTime(Duration.ZERO);
        this.instant = Objects.requireNonNull(instant, "instant");
    }

    @Override
    public Duration getMaxWaitTime() {
        return delegate.getMaxWaitTime();
    }

    @Override
    public Duration getMinWaitTime() {
        Duration remaining = Duration.between(Instant.now(), instant);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    @Override
    public Duration getHoldPredicateTime() {
        return delegate.getHoldPredicateTime();
    }

    @Override
    public WaitConstraint withMinWaitTime(Duration minWaitTime) {
        return delegate.withMinWaitTime(minWaitTime);
    }

    @Override
    public WaitConstraint withMaxWaitTime(Duration maxWaitTime) {
        return new InstantWaitConstraint(delegate.withMaxWaitTime(maxWaitTime), instant);
    }

    @Override
    public WaitConstraint withHoldPredicateTime(Duration holdConditionTime) {
        return new InstantWaitConstraint(delegate.withHoldPredicateTime(holdConditionTime), instant);
    }
}
