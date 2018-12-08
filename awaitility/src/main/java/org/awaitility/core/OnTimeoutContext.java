/*
 * Copyright 2015 the original author or authors.
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

import java.util.Optional;
import org.awaitility.Duration;
import org.awaitility.pollinterval.PollInterval;

public class OnTimeoutContext {

    private final String alias;
    private final Duration maxWaitTime;
    private final PollInterval pollInterval;
    private final Duration pollDelay;
    private final String timeoutMessage;
    private final Throwable cause;
    private final Duration evaluationDuration;

    public OnTimeoutContext(String alias, Duration maxWaitTime, PollInterval pollInterval, Duration pollDelay, String timeoutMessage,
        Throwable cause, Duration evaluationDuration) {
        this.alias = alias;
        this.maxWaitTime = maxWaitTime;
        this.pollInterval = pollInterval;
        this.pollDelay = pollDelay;
        this.timeoutMessage = timeoutMessage;
        this.cause = cause;
        this.evaluationDuration = evaluationDuration;
    }

    public Optional<String> getAlias() {
        return Optional.ofNullable(alias);
    }

    public Duration getMaxWaitTime() {
        return maxWaitTime;
    }

    public PollInterval getPollInterval() {
        return pollInterval;
    }

    public Duration getPollDelay() {
        return pollDelay;
    }

    public String getTimeoutMessage() {
        return timeoutMessage;
    }

    public Optional<Throwable> getCause() {
        return Optional.ofNullable(cause);
    }

    public Duration getEvaluationDuration() {
        return evaluationDuration;
    }
}
