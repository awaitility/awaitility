/*
 * Copyright 2010 the original author or authors.
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
package com.jayway.awaitility.core;

import org.hamcrest.Matcher;

/**
 * Handles each intermediary mismatch and a final match (if any) of Hamcrest-based condition.
 *
 * @param <T> The expected return type of the condition
 */
public interface IntermediaryResultHandler<T> {

    /**
     * Handles an intermediary mismatch of a Hamcrest matcher.
     *
     * @param mismatchMessage       Descriptive message returned when Hamcrest matcher fails.
     * @param matcher               The Hamcrest matcher used in the condition
     * @param currentConditionValue The current value of the condition.
     * @param elapsedTimeInMS       elapsed time in milliseconds.
     * @param remainingTimeInMS     remaining time to wait in milliseconds; <code>Long.MAX_VALUE</code>, if no timeout defined, i.e., running forever.
     */
    void handleMismatch(String mismatchMessage, Matcher<? super T> matcher, T currentConditionValue, long elapsedTimeInMS, long remainingTimeInMS);

    /**
     * Handles a final match of a Hamcrest matcher.
     *
     * @param matchMessage      Descriptive message returned when Hamcrest matcher succeeds.
     * @param matcher           The Hamcrest matcher used in the condition
     * @param conditionEndValue The value returned by the condition when it matched.
     * @param elapsedTimeInMS   elapsed time in milliseconds.
     * @param remainingTimeInMS remaining time to wait in milliseconds; <code>Long.MAX_VALUE</code>, if no timeout defined, i.e., running forever.
     */
    void handleMatch(String matchMessage, Matcher<? super T> matcher, T conditionEndValue, long elapsedTimeInMS, long remainingTimeInMS);
}
