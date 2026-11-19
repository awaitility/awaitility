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
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.core.LambdaErrorMessageGenerator.generateLambdaErrorMessagePrefix;
import static org.awaitility.core.LambdaErrorMessageGenerator.isLambdaClass;

/**
 * Condition that is fulfilled when the polled code throws an exception of an expected type.
 * The caught exception is the result of the condition. Internal to Awaitility.
 * <p>
 * Outcomes of one evaluation:
 * <ul>
 *     <li>throws a matching exception: the condition is fulfilled and the exception is returned</li>
 *     <li>returns normally: the condition is not fulfilled yet, keep polling</li>
 *     <li>throws a non-matching exception: rethrown, so that the ignore exceptions settings decide whether
 *     to keep polling (exception ignored) or fail immediately (not ignored), the same as for {@code until(..)}</li>
 * </ul>
 *
 * @param <T> the expected exception type
 */
class UntilThrownCondition<T extends Throwable> implements Condition<T> {

    private final ConditionAwaiter conditionAwaiter;
    private final ConditionEvaluationHandler<Object> conditionEvaluationHandler;
    private final AtomicReference<Throwable> caught = new AtomicReference<>();
    private final AtomicReference<String> lastOutcome = new AtomicReference<>();

    /**
     * @param code                  the code to poll
     * @param returnsValue          <code>true</code> if <code>code</code> returns a value worth reporting (a {@link Callable}),
     *                              <code>false</code> if it wraps a {@link ThrowingRunnable}
     * @param descriptionSource     the object (typically the lambda) used to describe the condition in messages
     * @param expectedExceptionType the expected exception type
     * @param exactMatch            <code>true</code> to require the exact class, <code>false</code> to also accept subclasses
     * @param settings              the condition settings
     */
    UntilThrownCondition(final Callable<?> code, final boolean returnsValue, final Object descriptionSource,
                         final Class<T> expectedExceptionType, final boolean exactMatch, final ConditionSettings settings) {
        if (code == null || descriptionSource == null) {
            throw new IllegalArgumentException("You must specify the code that is expected to throw (was null).");
        }
        if (expectedExceptionType == null) {
            throw new IllegalArgumentException("You must specify the expected exception type (was null).");
        }
        final String description = describe(descriptionSource, settings.getAlias());
        // The alias is added by ConditionAwaiter to the timeout message, so leave it out here
        final String timeoutDescription = describe(descriptionSource, null);
        final String expectation = "expected to throw " + (exactMatch ? "exactly " : "") + expectedExceptionType.getName();

        conditionEvaluationHandler = new ConditionEvaluationHandler<>(null, settings);

        final ConditionEvaluator evaluator = pollInterval -> {
            final Object value;
            try {
                value = code.call();
            } catch (Throwable throwable) {
                final String outcome = "it threw " + throwable;
                lastOutcome.set(outcome);
                if (matches(throwable, expectedExceptionType, exactMatch)) {
                    caught.set(throwable);
                    conditionEvaluationHandler.handleConditionResultMatch(description + " " + expectation + " and " + outcome, throwable, pollInterval);
                    return new ConditionEvaluationResult(true);
                }
                // Not the expected exception: let the ignore exceptions settings decide (see ConditionAwaiter.ConditionPoller)
                return CheckedExceptionRethrower.safeRethrow(throwable);
            }
            final String outcome = returnsValue ? "it returned " + value : "it returned normally";
            lastOutcome.set(outcome);
            conditionEvaluationHandler.handleConditionResultMismatch(description + " " + expectation + " but " + outcome, value, pollInterval);
            return new ConditionEvaluationResult(false);
        };

        conditionAwaiter = new ConditionAwaiter(evaluator, settings) {
            @Override
            protected String getTimeoutMessage() {
                String outcome = lastOutcome.get();
                return timeoutDescription + " was " + expectation + " but " + (outcome == null ? "it never completed an evaluation" : outcome);
            }
        };
    }

    private static boolean matches(Throwable throwable, Class<? extends Throwable> expectedExceptionType, boolean exactMatch) {
        return exactMatch ? throwable.getClass().equals(expectedExceptionType) : expectedExceptionType.isInstance(throwable);
    }

    private static String describe(Object descriptionSource, String alias) {
        Class<?> type = descriptionSource.getClass();
        String aliasDescription = alias == null ? "" : " with alias " + alias;
        if (isLambdaClass(type)) {
            String lambdaDescription = generateLambdaErrorMessagePrefix(type, false);
            return alias == null ? "Condition with " + lambdaDescription : "Condition" + aliasDescription + " using " + lambdaDescription;
        }
        return "Condition" + aliasDescription + " " + type.getName() + generateMethodDescription(type);
    }

    private static String generateMethodDescription(Class<?> type) {
        Method enclosingMethod = null;
        try {
            enclosingMethod = type.getEnclosingMethod();
        } catch (Error ignored) {
            // A java.lang.InternalError can be thrown for some generated classes (e.g. in Groovy)
        }
        return enclosingMethod == null ? "" : " defined in " + enclosingMethod;
    }

    /**
     * Await until the expected exception is thrown.
     *
     * @return the caught exception
     */
    @SuppressWarnings("unchecked")
    @Override
    public T await() {
        conditionAwaiter.await(conditionEvaluationHandler);
        return (T) caught.get();
    }
}
