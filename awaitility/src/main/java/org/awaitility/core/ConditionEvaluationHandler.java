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

import org.hamcrest.Matcher;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

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

    void handleConditionResultMismatch(String mismatchMessage, T currentConditionValue, Duration pollInterval) {
        doWithListener(listener -> {
            long elapsedTimeInMS = watch.getElapsedTimeInMS();
            long remainingTimeInMS = getRemainingTimeInMS(elapsedTimeInMS, settings.getMaxWaitTime());
            try {
                listener.conditionEvaluated(new EvaluatedCondition<>(mismatchMessage, matcher, currentConditionValue, elapsedTimeInMS,
                        remainingTimeInMS, false, settings.getAlias(), pollInterval));
            } catch (ClassCastException e) {
                throwClassCastExceptionBecauseConditionEvaluationListenerCouldNotBeApplied(e, listener);
            }
        });
    }

    void handleConditionResultMatch(String matchMessage, T currentConditionValue, Duration pollInterval) {
        doWithListener(listener -> {
            long elapsedTimeInMS = watch.getElapsedTimeInMS();
            long remainingTimeInMS = getRemainingTimeInMS(elapsedTimeInMS, settings.getMaxWaitTime());
            try {
                listener.conditionEvaluated(new EvaluatedCondition<>(matchMessage, matcher, currentConditionValue, elapsedTimeInMS,
                        remainingTimeInMS, true, settings.getAlias(), pollInterval));
            } catch (ClassCastException e) {
                throwClassCastExceptionBecauseConditionEvaluationListenerCouldNotBeApplied(e, listener);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void doWithListener(Consumer<ConditionEvaluationListener<T>> consumer) {
        ConditionEvaluationListener<T> listener = settings.getConditionEvaluationListener();
        if (listener == null) {
            return;
        }
        consumer.accept(listener);
    }

    private long getRemainingTimeInMS(long elapsedTimeInMS, Duration maxWaitTime) {
        return maxWaitTime == null || ChronoUnit.FOREVER.getDuration().equals(maxWaitTime) ? Long.MAX_VALUE : maxWaitTime.toMillis() - elapsedTimeInMS;
    }

    private void throwClassCastExceptionBecauseConditionEvaluationListenerCouldNotBeApplied(ClassCastException e, ConditionEvaluationListener listener) {
        throw new ClassCastException("Cannot apply condition evaluation listener " + listener.getClass().getName() + " because " + e.getMessage());
    }

    public void start() {

        ConditionEvaluationListener<T> listener = settings.getConditionEvaluationListener();
        if (listener != null) {
            long elapsedTimeInMS = 0L;
            long remainingTimeInMS = getRemainingTimeInMS(0, settings.getMaxWaitTime());

            listener.beforeEvaluation(new StartEvaluationEvent<>("Starting evaluation", matcher, elapsedTimeInMS,
                    remainingTimeInMS, settings.getAlias()));
        }
        watch.start();
    }

    public void handleTimeout(String message, boolean isConditionSatisfied) {
        ConditionEvaluationListener<T> listener = settings.getConditionEvaluationListener();
        if (listener != null) {
            long elapsedTimeInMS = watch.getElapsedTimeInMS();
            long remainingTimeInMS = getRemainingTimeInMS(elapsedTimeInMS, settings.getMaxWaitTime());
            listener.onTimeout(new TimeoutEvent(message, elapsedTimeInMS, remainingTimeInMS,
                    isConditionSatisfied, settings.getAlias()));
        }
    }

    public void handleIgnoredException(Throwable throwable) {
        ConditionEvaluationListener<T> listener = settings.getConditionEvaluationListener();
        if (listener != null) {
            long elapsedTimeInMS = watch.getElapsedTimeInMS();
            long remainingTimeInMS = getRemainingTimeInMS(elapsedTimeInMS, settings.getMaxWaitTime());
            listener.exceptionIgnored(new IgnoredException(throwable, elapsedTimeInMS, remainingTimeInMS, settings.getAlias()));
        }
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
