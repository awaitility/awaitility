/*
 * Copyright 2014 the original author or authors.
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

import org.awaitility.Duration;
import org.hamcrest.Matcher;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Handler for {@link Condition} implementations that calls {@link ConditionEvaluationListener} with condition evaluation result and message.
 * It also serves as stop watch for elapsed time.
 */
class ConditionEvaluationHandler<T> {

    private final Matcher<? super T> matcher;
    private final ConditionSettings settings;
    private final StopWatch watch;

    ConditionEvaluationHandler(Matcher<? super T> matcher, ConditionSettings settings) {
        this.matcher = matcher;
        this.settings = settings;
        watch = new StopWatch();
    }

    @SuppressWarnings("unchecked")
    void handleConditionResultMismatch(String mismatchMessage, T currentConditionValue, Duration pollInterval) {
        ConditionEvaluationListener<T> listener = settings.getConditionEvaluationListener();
        if (listener == null) {
            return;
        }

        long elapsedTimeInMS = watch.getElapsedTimeInMS();
        long remainingTimeInMS = getRemainingTimeInMS(elapsedTimeInMS, settings.getMaxWaitTime());
        try {
            listener.conditionEvaluated(new EvaluatedCondition<T>(mismatchMessage, matcher, currentConditionValue, elapsedTimeInMS,
                    remainingTimeInMS, false, settings.getAlias(), pollInterval));
        } catch (ClassCastException e) {
            throwClassCastExceptionBecauseConditionEvaluationListenerCouldntBeApplied(e, listener);
        }
    }

    @SuppressWarnings("unchecked")
    void handleConditionResultMatch(String matchMessage, T currentConditionValue, Duration pollInterval) {
        ConditionEvaluationListener<T> listener = settings.getConditionEvaluationListener();
        if (listener == null) {
            return;
        }
        long elapsedTimeInMS = watch.getElapsedTimeInMS();
        long remainingTimeInMS = getRemainingTimeInMS(elapsedTimeInMS, settings.getMaxWaitTime());
        try {
            listener.conditionEvaluated(new EvaluatedCondition<T>(matchMessage, matcher, currentConditionValue, elapsedTimeInMS,
                    remainingTimeInMS, true, settings.getAlias(), pollInterval));
        } catch (ClassCastException e) {
            throwClassCastExceptionBecauseConditionEvaluationListenerCouldntBeApplied(e, listener);
        }
    }

    private long getRemainingTimeInMS(long elapsedTimeInMS, Duration maxWaitTime) {
        return maxWaitTime.equals(Duration.FOREVER) ?
                Long.MAX_VALUE : maxWaitTime.getValueInMS() - elapsedTimeInMS;
    }

    private void throwClassCastExceptionBecauseConditionEvaluationListenerCouldntBeApplied(ClassCastException e, ConditionEvaluationListener listener) {
        throw new ClassCastException("Cannot apply condition evaluation listener " + listener.getClass().getName() + " because " + e.getMessage());
    }

    public void start() {
        watch.start();
    }

    private static class StopWatch {
        private long startTime;

        public void start() {
            this.startTime = System.nanoTime();
        }

        long getElapsedTimeInMS() {
            return NANOSECONDS.toMillis(System.nanoTime() - startTime);
        }
    }
}
