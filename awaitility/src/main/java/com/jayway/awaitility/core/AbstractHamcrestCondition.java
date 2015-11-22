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
    private final ConditionEvaluationHandler<T> conditionEvaluationHandler;

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

        conditionEvaluationHandler = new ConditionEvaluationHandler<T>(matcher, settings);
        final ConditionEvaluator callable = new ConditionEvaluator() {
            public boolean eval(Duration pollInterval) throws Exception{
                lastResult = supplier.call();
                boolean matches = matcher.matches(lastResult);
                if (matches) {
                    conditionEvaluationHandler.handleConditionResultMatch(getMatchMessage(supplier, matcher), lastResult, pollInterval);
                } else {
                    conditionEvaluationHandler.handleConditionResultMismatch(getMismatchMessage(supplier, matcher), lastResult, pollInterval);
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


    private String getMatchMessage(Callable<T> supplier, Matcher<? super T> matcher) {
        return String.format("%s reached its end value of %s", getCallableDescription(supplier), HamcrestToStringFilter.filter(matcher));
    }

    private String getMismatchMessage(Callable<T> supplier, Matcher<? super T> matcher) {
        Description mismatchDescription = new StringDescription();
        matcher.describeMismatch(lastResult, mismatchDescription);
        return String.format("%s expected %s but %s", getCallableDescription(supplier), HamcrestToStringFilter.filter(matcher), mismatchDescription);
    }

    /**
     * <p>await.</p>
     *
     * @return a T object.
     */
    public T await() {
        conditionAwaiter.await(conditionEvaluationHandler);
        return lastResult;
    }

    abstract String getCallableDescription(final Callable<T> supplier);
}
