/*
 * Copyright 2019 the original author or authors.
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

public class TimeoutEvent {

    private final String description;
    private final long elapsedTimeInMS;
    private final long remainingTimeInMS;
    private final boolean conditionIsFulfilled;
    private final String alias;

    public TimeoutEvent(String description, long elapsedTimeInMS, long remainingTimeInMS, boolean conditionIsFulfilled, String alias) {
        this.description = description;
        this.elapsedTimeInMS = elapsedTimeInMS;
        this.remainingTimeInMS = remainingTimeInMS;
        this.conditionIsFulfilled = conditionIsFulfilled;
        this.alias = alias;
    }

    public String getDescription() {
        return description;
    }

    public long getElapsedTimeInMS() {
        return elapsedTimeInMS;
    }

    public long getRemainingTimeInMS() {
        return remainingTimeInMS;
    }

    public boolean isConditionIsFulfilled() {
        return conditionIsFulfilled;
    }

    public String getAlias() {
        return alias;
    }
}
