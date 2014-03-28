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
 * Condition implementation which takes an executable assertion which should throw {@link AssertionError} on assertion failure.
 *
 * @since 1.6.0
 *
 * @author Marcin Zajączkowski, 2014-03-28
 */
public class AssertionCondition implements Condition<Void> {

    private final ConditionAwaiter conditionAwaiter;

    private String lastExceptionMessage;

    public AssertionCondition(final Runnable supplier, ConditionSettings settings) {
        if (supplier == null) {
            throw new IllegalArgumentException("You must specify a supplier (was null).");
        }
        Callable<Boolean> callable = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                try {
                    supplier.run();
                    return true;
                } catch (AssertionError e) {
                    lastExceptionMessage = e.getMessage();
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

    public Void await() {
        conditionAwaiter.await();
        return null;
    }
}
