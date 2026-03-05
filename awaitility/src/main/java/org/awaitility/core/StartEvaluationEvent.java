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

public class StartEvaluationEvent<T> {
    private final String description;
    private final Object matcher;
    private final long elapsedTimeInMS;
    private final long remainingTimeInMS;
    private final String alias;

    /**
     * @param description           description message of the event
     * @param matcher               The matcher used in the condition (may be {@code null} for non-matcher conditions)
     * @param elapsedTimeInMS       elapsed time in milliseconds.
     * @param remainingTimeInMS     remaining time to wait in milliseconds; <code>Long.MAX_VALUE</code>, if no timeout defined, i.e., running forever.
     */
    StartEvaluationEvent(String description, Object matcher, long elapsedTimeInMS, long remainingTimeInMS,
                         String alias) {
        this.description = description;
        this.matcher = matcher;
        this.elapsedTimeInMS = elapsedTimeInMS;
        this.remainingTimeInMS = remainingTimeInMS;
        this.alias = alias;
    }

    public String getDescription() {
        return description;
    }

    @SuppressWarnings("unchecked")
    public Matcher<? super T> getMatcher() {
        return (Matcher<? super T>) matcher;
    }

    public long getElapsedTimeInMS() {
        return elapsedTimeInMS;
    }

    public long getRemainingTimeInMS() {
        return remainingTimeInMS;
    }

    public String getAlias() {
        return alias;
    }
}
