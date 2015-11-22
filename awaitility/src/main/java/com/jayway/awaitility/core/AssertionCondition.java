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

import com.jayway.awaitility.Duration;

import java.lang.reflect.Method;

import static com.jayway.awaitility.core.LambdaErrorMessageGenerator.generateLambdaErrorMessagePrefix;
import static com.jayway.awaitility.core.LambdaErrorMessageGenerator.isLambdaClass;

/**
 * Condition implementation which takes an executable assertion which should throw {@link java.lang.AssertionError} on assertion failure.
 *
 * @author Marcin Zajączkowski, 2014-03-28
 * @author Johan Haleby
 * @since 1.6.0
 */
public class AssertionCondition implements Condition<Void> {

    private final ConditionAwaiter conditionAwaiter;

    private String lastExceptionMessage;
    private final ConditionEvaluationHandler<Object> conditionEvaluationHandler;

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

        conditionEvaluationHandler = new ConditionEvaluationHandler<Object>(null, settings);

        final ConditionEvaluator callable = new ConditionEvaluator() {
            public boolean eval(Duration pollInterval) throws Exception {
                try {
                    supplier.run();
                    conditionEvaluationHandler.handleConditionResultMatch(getMatchMessage(supplier, settings.getAlias()), null, pollInterval);
                    return true;
                } catch (AssertionError e) {
                    lastExceptionMessage = e.getMessage();
                    conditionEvaluationHandler.handleConditionResultMismatch(getMismatchMessage(supplier, lastExceptionMessage, settings.getAlias()), null, pollInterval);
                    return false;
                }
            }
        };
        conditionAwaiter = new ConditionAwaiter(callable, settings) {
            @Override
            protected String getTimeoutMessage() {
                return getMismatchMessage(supplier, lastExceptionMessage, settings.getAlias());
            }
        };
    }


    private String getMatchMessage(Runnable supplier, String conditionAlias) {
        return generateDescriptionPrefix(supplier, conditionAlias) + " reached its end value";
    }

    private String getMismatchMessage(Runnable supplier, String exceptionMessage, String conditionAlias) {
        return generateDescriptionPrefix(supplier, conditionAlias) + " " + exceptionMessage;
    }

    private String generateDescriptionPrefix(Runnable supplier, String conditionAlias) {
        String methodDescription = generateMethodDescription(supplier);
        boolean hasAlias = conditionAlias != null;
        if (isLambdaClass(supplier.getClass())) {
            final String prefix;
            if (hasAlias) {
                prefix = "Condition with alias " + conditionAlias + " defined as a ";
            } else {
                prefix = "Condition defined as a ";
            }
            return prefix + generateLambdaErrorMessagePrefix(supplier.getClass(), false) + methodDescription;
        }
        return "Runnable condition" + (hasAlias ? " with alias " + conditionAlias : "") + methodDescription;
    }

    private String generateMethodDescription(Runnable supplier) {
        String methodDescription = "";
        Method enclosingMethod = null;
        try {
            enclosingMethod = supplier.getClass().getEnclosingMethod();
        } catch (Error ignored) {
            // A java.lang.InternalError could be thrown when using the Groovy extension using Groovy 2.3.7 for some reason. Bug in Groovy?!
        }
        if (enclosingMethod != null) {
            methodDescription = " defined in " + enclosingMethod.toString();
        }
        return methodDescription;
    }

    /**
     * <p>await.</p>
     *
     * @return a {@link java.lang.Void} object.
     */
    public Void await() {
        conditionAwaiter.await(conditionEvaluationHandler);
        return null;
    }
}
