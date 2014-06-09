package com.jayway.awaitility.core;

import org.hamcrest.Matcher;

/**
 * Contains properties of the condition at its current stage.
 *
 * @param <T> The condition evaluation result value type.
 */
public class EvaluatedCondition<T> {
    private final String description;
    private final Matcher<? super T> matcher;
    private final T currentConditionValue;
    private final long elapsedTimeInMS;
    private final long remainingTimeInMS;
    private final boolean conditionIsFulfilled;

    /**
     * @param description           Descriptive message of the Hamcrest matcher.
     * @param matcher               The Hamcrest matcher used in the condition
     * @param currentConditionValue The current value of the condition.
     * @param elapsedTimeInMS       elapsed time in milliseconds.
     * @param remainingTimeInMS     remaining time to wait in milliseconds; <code>Long.MAX_VALUE</code>, if no timeout defined, i.e., running forever.
     * @param isConditionSatisfied  <code>true</code> if the condition is satisfied (i.e. hamcrest matcher matches the value), <code>false</code> otherwise (i.e. an intermediate value).
     */
    EvaluatedCondition(String description, Matcher<? super T> matcher, T currentConditionValue, long elapsedTimeInMS, long remainingTimeInMS,
                       boolean isConditionSatisfied) {
        this.description = description;
        this.matcher = matcher;
        this.currentConditionValue = currentConditionValue;
        this.elapsedTimeInMS = elapsedTimeInMS;
        this.remainingTimeInMS = remainingTimeInMS;
        this.conditionIsFulfilled = isConditionSatisfied;
    }

    /**
     * @return Descriptive message of the Hamcrest matcher.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The Hamcrest matcher used in the condition
     */
    public Matcher<? super T> getMatcher() {
        return matcher;
    }

    /**
     * @return The current value of the condition.
     */
    public T getValue() {
        return currentConditionValue;
    }

    /**
     * @return Elapsed time in milliseconds.
     */
    public long getElapsedTimeInMS() {
        return elapsedTimeInMS;
    }

    /**
     * @return Remaining time to wait in milliseconds or <code>Long.MAX_VALUE</code> if no timeout defined, i.e., running forever.
     */
    public long getRemainingTimeInMS() {
        return remainingTimeInMS;
    }

    /**
     * @return <code>true</code> if the condition doesn't have a timeout, <code>false</code> otherwise.
     */
    public boolean isConditionRunningForever() {
        return getRemainingTimeInMS() == Long.MAX_VALUE;
    }

    /**
     * @return <code>true</code> if the condition is satisfied (i.e. hamcrest matcher matches the value), <code>false</code> otherwise (i.e. an intermediate value).
     */
    public boolean isSatisfied() {
        return conditionIsFulfilled;
    }
}
