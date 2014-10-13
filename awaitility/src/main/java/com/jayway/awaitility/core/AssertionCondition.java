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
package com.jayway.awaitility.core;

import java.util.concurrent.Callable;

/**
 * Condition implementation which takes an executable assertion which should throw {@link java.lang.AssertionError} on assertion failure.
 *
 * @since 1.6.0
 * @author Marcin Zajączkowski, 2014-03-28
 */
public class AssertionCondition implements Condition<Void> {

    private final ConditionAwaiter conditionAwaiter;

    private String lastExceptionMessage;
    private final ConditionEvaluationHandler<String> conditionEvaluationHandler;

    /**
     * <p>Constructor for AssertionCondition.</p>
     *
     * @param supplier a {@link java.lang.Runnable} object.
     * @param settings a {@link com.jayway.awaitility.core.ConditionSettings} object.
     */
    public AssertionCondition(final Runnable supplier, final ConditionSettings settings) {
        if (supplier == null) {
            throw new IllegalArgumentException("You must specify a supplier (was null).");
        }

        conditionEvaluationHandler = new ConditionEvaluationHandler<String>(null, settings);

        final Callable<Boolean> callable = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                try {
                    supplier.run();
                    conditionEvaluationHandler.handleConditionResultMatch(getMatchMessage(supplier), lastExceptionMessage);
                    return true;
                } catch (AssertionError e) {
                    lastExceptionMessage = e.getMessage();
                    conditionEvaluationHandler.handleConditionResultMatch(getMismatchMessage(supplier), lastExceptionMessage);
                    return false;
                }
            }
        };
        conditionAwaiter = new ConditionAwaiter(callable, settings) {
            @Override
            protected String getTimeoutMessage() {
                return supplier.getClass().getName() + " " + lastExceptionMessage;
            }
        };
    }


    private String getMatchMessage(Runnable supplier) {
        return supplier.getClass().getName() + " passed";
    }

    private String getMismatchMessage(Runnable supplier) {
        return supplier.getClass().getName() + " " + lastExceptionMessage;
    }

    /**
     * <p>await.</p>
     *
     * @return a {@link java.lang.Void} object.
     */
    public Void await() {
        conditionEvaluationHandler.start();
        conditionAwaiter.await();
        return null;
    }
}
