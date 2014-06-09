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

import com.jayway.awaitility.Duration;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import java.util.concurrent.Callable;

abstract class AbstractHamcrestCondition<T> implements Condition<T> {

    private ConditionAwaiter conditionAwaiter;

    private T lastResult;
    private final StopWatch watch;

    /**
     * <p>Constructor for AbstractHamcrestCondition.</p>
     *
     * @param supplier a {@link java.util.concurrent.Callable} object.
     * @param matcher  a {@link org.hamcrest.Matcher} object.
     * @param settings a {@link com.jayway.awaitility.core.ConditionSettings} object.
     */
    public AbstractHamcrestCondition(final Callable<T> supplier, final Matcher<? super T> matcher, final ConditionSettings settings) {
        if (supplier == null) {
            throw new IllegalArgumentException("You must specify a supplier (was null).");
        }
        if (matcher == null) {
            throw new IllegalArgumentException("You must specify a matcher (was null).");
        }

        watch = new StopWatch();
        final Callable<Boolean> callable = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                lastResult = supplier.call();
                boolean matches = matcher.matches(lastResult);
                if (!matches) {
                    handleConditionResultMismatch(supplier, matcher, settings, watch, lastResult);
                } else {
                    handleConditionResultMatch(supplier, matcher, settings, watch, lastResult);
                }
                return matches;

            }
        };
        conditionAwaiter = new ConditionAwaiter(callable, settings) {
            @Override
            protected String getTimeoutMessage() {
                // Use "return getMismatchMessage(supplier, matcher);" to more descriptive message
                return String.format("%s expected %s but was <%s>", getCallableDescription(supplier), HamcrestToStringFilter.filter(matcher), lastResult);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private void handleConditionResultMatch(Callable<T> supplier, Matcher<? super T> matcher, ConditionSettings settings, StopWatch watch, T lastResult) {
        ConditionEvaluationListener<T> listener = settings.getConditionEvaluationListener();
        if (listener != null) {
            long elapsedTimeInMS = watch.getElapsedTimeInMS();
            long remainingTimeInMS = getRemainingTimeInMS(elapsedTimeInMS, settings.getMaxWaitTime());
            try {
                listener.conditionEvaluated(new EvaluatedCondition<T>(getMatchMessage(supplier, matcher), matcher, lastResult, elapsedTimeInMS, remainingTimeInMS, true));
            } catch (ClassCastException e) {
                throwClassCastExceptionBecauseIntermediaryResultHandlerCouldntBeApplied(e, listener);
            }
        }
    }

    private long getRemainingTimeInMS(long elapsedTimeInMS, Duration maxWaitTime) {
        return maxWaitTime.equals(Duration.FOREVER) ?
                Long.MAX_VALUE : maxWaitTime.getValueInMS() - elapsedTimeInMS;
    }

    private String getMatchMessage(Callable<T> supplier, Matcher<? super T> matcher) {
        return String.format("%s reached its end value of %s", getCallableDescription(supplier), HamcrestToStringFilter.filter(matcher));
    }

    @SuppressWarnings("unchecked")
    private void handleConditionResultMismatch(Callable<T> supplier, Matcher<? super T> matcher, ConditionSettings settings, StopWatch watch, T lastResult) {
        ConditionEvaluationListener<T> listener = settings.getConditionEvaluationListener();
        if (listener != null) {
            long elapsedTimeInMS = watch.getElapsedTimeInMS();
            long remainingTimeInMS = getRemainingTimeInMS(elapsedTimeInMS, settings.getMaxWaitTime());
            try {
                listener.conditionEvaluated(new EvaluatedCondition<T>(getMismatchMessage(supplier, matcher), matcher, lastResult, elapsedTimeInMS, remainingTimeInMS, false));
            } catch (ClassCastException e) {
                throwClassCastExceptionBecauseIntermediaryResultHandlerCouldntBeApplied(e, listener);
            }
        }
    }

    private void throwClassCastExceptionBecauseIntermediaryResultHandlerCouldntBeApplied(ClassCastException e, ConditionEvaluationListener listener) {
        throw new ClassCastException("Cannot apply condition evaluation listener " + listener.getClass().getName() + " because " + e.getMessage());
    }

    private String getMismatchMessage(Callable<T> supplier, Matcher<? super T> matcher) {
        Description mismatchDescription = new StringDescription();
        matcher.describeMismatch(lastResult, mismatchDescription);
        return String.format("%s expected %s but %s", getCallableDescription(supplier), HamcrestToStringFilter.filter(matcher), mismatchDescription);
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

    /**
     * <p>await.</p>
     *
     * @return a T object.
     */
    public T await() {
        watch.start();
        conditionAwaiter.await();
        return lastResult;
    }

    abstract String getCallableDescription(final Callable<T> supplier);
}
