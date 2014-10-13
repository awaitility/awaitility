package com.jayway.awaitility.core;

import com.jayway.awaitility.Duration;
import org.hamcrest.Matcher;

/**
 * Handler for {@link Condition} implementations that calls {@link ConditionEvaluationListener} with condition evaluation result and message.
 * It also serves as stop watch for elapsed time.
 */
public class ConditionEvaluationHandler<T> {

    private final Matcher<? super T> matcher;
    private final ConditionSettings settings;
    private final StopWatch watch;

    public ConditionEvaluationHandler(Matcher<? super T> matcher, ConditionSettings settings) {
        this.matcher = matcher;
        this.settings = settings;
        watch = new StopWatch();
    }

    @SuppressWarnings("unchecked")
    public void handleConditionResultMismatch(String mismatchMessage, T currentConditionValue) {
        ConditionEvaluationListener<T> listener = settings.getConditionEvaluationListener();
        if (listener == null) {
            return;
        }

        long elapsedTimeInMS = watch.getElapsedTimeInMS();
        long remainingTimeInMS = getRemainingTimeInMS(elapsedTimeInMS, settings.getMaxWaitTime());
        try {
            listener.conditionEvaluated(new EvaluatedCondition<T>(mismatchMessage, matcher, currentConditionValue, elapsedTimeInMS, remainingTimeInMS, false));
        } catch (ClassCastException e) {
            throwClassCastExceptionBecauseIntermediaryResultHandlerCouldntBeApplied(e, listener);
        }
    }

    @SuppressWarnings("unchecked")
    public void handleConditionResultMatch(String matchMessage, T currentConditionValue) {
        ConditionEvaluationListener<T> listener = settings.getConditionEvaluationListener();
        if (listener == null) {
            return;
        }
        long elapsedTimeInMS = watch.getElapsedTimeInMS();
        long remainingTimeInMS = getRemainingTimeInMS(elapsedTimeInMS, settings.getMaxWaitTime());
        try {
            listener.conditionEvaluated(new EvaluatedCondition<T>(matchMessage, matcher, currentConditionValue, elapsedTimeInMS, remainingTimeInMS, true));
        } catch (ClassCastException e) {
            throwClassCastExceptionBecauseIntermediaryResultHandlerCouldntBeApplied(e, listener);
        }
    }

    private long getRemainingTimeInMS(long elapsedTimeInMS, Duration maxWaitTime) {
        return maxWaitTime.equals(Duration.FOREVER) ?
                Long.MAX_VALUE : maxWaitTime.getValueInMS() - elapsedTimeInMS;
    }

    private void throwClassCastExceptionBecauseIntermediaryResultHandlerCouldntBeApplied(ClassCastException e, ConditionEvaluationListener listener) {
        throw new ClassCastException("Cannot apply condition evaluation listener " + listener.getClass().getName() + " because " + e.getMessage());
    }

    public void start() {
        watch.start();
    }

    private static class StopWatch {
        private long startTime;

        public void start() {
            this.startTime = System.currentTimeMillis();
        }

        public long getElapsedTimeInMS() {
            return System.currentTimeMillis() - startTime;
        }
    }
}
