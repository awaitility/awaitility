/*
 * Copyright 2026 the original author or authors.
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

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import static org.awaitility.core.LambdaErrorMessageGenerator.generateLambdaErrorMessagePrefix;
import static org.awaitility.core.LambdaErrorMessageGenerator.isLambdaClass;
import static org.awaitility.spi.Timeout.timeout_message;

/**
 * A {@link Condition} implementation that evaluates a {@link Callable} against a {@link Predicate}
 * without requiring a {@link Matcher} on the classpath.
 *
 * @param <T> the type returned by the supplier
 */
class PredicateCondition<T> implements Condition<T> {

    private final ConditionAwaiter conditionAwaiter;
    private final ConditionEvaluationHandler<T> conditionEvaluationHandler;
    private volatile T lastResult;

    PredicateCondition(final Callable<T> supplier, final Predicate<? super T> predicate, final ConditionSettings settings) {
        if (supplier == null) {
            throw new IllegalArgumentException("You must specify a supplier (was null).");
        }
        if (predicate == null) {
            throw new IllegalArgumentException("You must specify a predicate (was null).");
        }

        conditionEvaluationHandler = new ConditionEvaluationHandler<>(null, settings);
        final ConditionEvaluator callable = new ConditionEvaluator() {
            @Override
            public ConditionEvaluationResult eval(Duration pollInterval) throws Exception {
                lastResult = supplier.call();
                boolean matches = predicate.test(lastResult);
                if (matches) {
                    conditionEvaluationHandler.handleConditionResultMatch(
                            getMatchMessage(supplier), lastResult, pollInterval);
                } else {
                    conditionEvaluationHandler.handleConditionResultMismatch(
                            getMismatchMessage(supplier), lastResult, pollInterval);
                }
                return new ConditionEvaluationResult(matches);
            }
        };

        conditionAwaiter = new ConditionAwaiter(callable, settings) {
            @SuppressWarnings("rawtypes")
            @Override
            protected String getTimeoutMessage() {
                if (timeout_message != null) {
                    return timeout_message;
                }
                return getMismatchMessage(supplier);
            }
        };
    }

    @Override
    public T await() {
        conditionAwaiter.await(conditionEvaluationHandler);
        return lastResult;
    }

    private String getMatchMessage(Callable<T> supplier) {
        return String.format("%s returned a value matching the predicate: <%s>",
                getCallableDescription(supplier), String.valueOf(lastResult));
    }

    private String getMismatchMessage(Callable<T> supplier) {
        return String.format("%s returned a value not matching the predicate: <%s>",
                getCallableDescription(supplier), String.valueOf(lastResult));
    }

    @SuppressWarnings("rawtypes")
    private String getCallableDescription(Callable<T> supplier) {
        final Class<? extends Callable> supplierClass = supplier.getClass();
        Method enclosingMethod = supplierClass.getEnclosingMethod();
        if (supplierClass.isAnonymousClass() && enclosingMethod != null) {
            return enclosingMethod.getDeclaringClass().getName() + "." + enclosingMethod.getName() + " Callable";
        } else if (isLambdaClass(supplierClass)) {
            return generateLambdaErrorMessagePrefix(supplierClass, true);
        } else {
            return supplierClass.getName();
        }
    }
}
