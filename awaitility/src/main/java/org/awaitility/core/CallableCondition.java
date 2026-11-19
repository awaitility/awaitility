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
package org.awaitility.core;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.Callable;

import static org.awaitility.core.LambdaErrorMessageGenerator.generateLambdaErrorMessagePrefix;
import static org.awaitility.core.LambdaErrorMessageGenerator.isLambdaClass;
import static org.awaitility.spi.Timeout.timeout_message;

class CallableCondition implements Condition<Void> {

    private final ConditionAwaiter conditionAwaiter;

    private final ConditionEvaluationHandler<Object> conditionEvaluationHandler;

    private final ConditionEvaluationWrapper conditionEvaluationWrapper;

    /**
     * The last value returned by the condition, captured when the timeout message is generated.
     */
    private volatile Boolean lastValueAtTimeout;
    private volatile boolean timedOut;

    /**
     * <p>Constructor for CallableCondition.</p>
     *
     * @param matcher  a {@link java.util.concurrent.Callable} object.
     * @param settings a {@link org.awaitility.core.ConditionSettings} object.
     */
    CallableCondition(final Callable<Boolean> matcher, ConditionSettings settings) {
        conditionEvaluationHandler = new ConditionEvaluationHandler<Object>(null, settings);
        conditionEvaluationWrapper = new ConditionEvaluationWrapper(matcher, settings, conditionEvaluationHandler);
        conditionAwaiter = new ConditionAwaiter(conditionEvaluationWrapper, settings) {
            @SuppressWarnings("rawtypes")
            @Override
            protected String getTimeoutMessage() {
                lastValueAtTimeout = conditionEvaluationWrapper.lastValue;
                timedOut = true;
                if (timeout_message != null) {
                    return timeout_message;
                }
                final String timeoutMessage;
                if (matcher == null) {
                    timeoutMessage = "";
                } else {
                    final Class<? extends Callable> type = matcher.getClass();
                    final Method enclosingMethod = type.getEnclosingMethod();
                    if (type.isAnonymousClass() && enclosingMethod != null) {
                        timeoutMessage = String.format("Condition returned by method \"%s\" in class %s was not fulfilled",
                                enclosingMethod.getName(), enclosingMethod.getDeclaringClass().getName());
                    } else {
                        final String message;
                        if (isLambdaClass(type)) {
                            message = "with " + generateLambdaErrorMessagePrefix(type, false);
                        } else {
                            message = type.getName();
                        }
                        timeoutMessage = String.format("Condition %s was not fulfilled", message);
                    }
                }
                return timeoutMessage;
            }
        };
    }

    /**
     * <p>await</p>
     *
     * @return a {@link java.lang.Void} object.
     */
    public Void await() {
        conditionAwaiter.await(conditionEvaluationHandler);
        return null;
    }

    /**
     * Like {@link #await()} but returns a failed {@link AwaitResult} instead of throwing a {@link ConditionTimeoutException}.
     *
     * @return the result, its value is the last {@link Boolean} returned by the condition
     */
    AwaitResult<Boolean> tryAwait() {
        final ConditionTimeoutException timeout = conditionAwaiter.tryAwait(conditionEvaluationHandler);
        if (timeout == null) {
            return AwaitResult.success(conditionEvaluationWrapper.lastValue);
        }
        // timedOut is false when atLeast was violated, then the last (too early) value is stable
        return AwaitResult.failure(timedOut ? lastValueAtTimeout : conditionEvaluationWrapper.lastValue, timeout);
    }

    /**
     * Wraps and delegates to another callable and invokes the {@link org.awaitility.core.ConditionEvaluationHandler}.
     */
    private static class ConditionEvaluationWrapper implements ConditionEvaluator {

        private final Callable<Boolean> matcher;
        private final ConditionSettings settings;
        private final ConditionEvaluationHandler<Object> conditionEvaluationHandler;
        private volatile Boolean lastValue;

        ConditionEvaluationWrapper(Callable<Boolean> matcher, ConditionSettings settings, ConditionEvaluationHandler<Object> conditionEvaluationHandler) {

            this.matcher = matcher;
            this.settings = settings;
            this.conditionEvaluationHandler = conditionEvaluationHandler;
        }

        public ConditionEvaluationResult eval(Duration pollInterval) throws Exception {
            final Boolean value = matcher.call();
            lastValue = value;
            boolean conditionFulfilled = value;
            if (conditionFulfilled) {
                conditionEvaluationHandler.handleConditionResultMatch(getMatchMessage(matcher, settings.getAlias()), true, pollInterval);
            } else {
                conditionEvaluationHandler.handleConditionResultMismatch(getMismatchMessage(matcher, settings.getAlias()), false, pollInterval);

            }
            return new ConditionEvaluationResult(conditionFulfilled);
        }

        private String getMatchMessage(Callable<Boolean> matcher, String conditionAlias) {
            return generateDescriptionPrefix(matcher, conditionAlias) + " returned true";
        }

        private String getMismatchMessage(Callable<Boolean> matcher, String conditionAlias) {
            return generateDescriptionPrefix(matcher, conditionAlias) + " returned false";
        }

        private String generateDescriptionPrefix(Callable<Boolean> matcher, String conditionAlias) {
            String methodDescription = generateMethodDescription(matcher);
            boolean hasAlias = conditionAlias != null;
            if (isLambdaClass(matcher.getClass())) {
                final String prefix;
                if (hasAlias) {
                    prefix = "Condition with alias " + conditionAlias + " defined as a ";
                } else {
                    prefix = "Condition defined as a ";
                }
                return prefix + generateLambdaErrorMessagePrefix(matcher.getClass(), false) + methodDescription;
            }
            return "Callable condition" + (hasAlias ? " with alias " + conditionAlias : "") + methodDescription;
        }

        private String generateMethodDescription(Callable<Boolean> matcher) {
            String methodDescription = "";
            Method enclosingMethod = matcher.getClass().getEnclosingMethod();
            if (enclosingMethod != null) {
                methodDescription = " defined in " + enclosingMethod.toString();
            }
            return methodDescription;
        }
    }
}
