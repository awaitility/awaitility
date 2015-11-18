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

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

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
        this(new ThrowingRunnableAdapter(supplier), settings);
    }

    /**
     * <p>Constructor for AssertionCondition.</p>
     *
     * @param throwingSupplier a {@link ThrowingRunnable} object.
     * @param settings         a {@link com.jayway.awaitility.core.ConditionSettings} object.
     */
    public AssertionCondition(final ThrowingRunnable throwingSupplier, final ConditionSettings settings) {
        if (throwingSupplier == null) {
            throw new IllegalArgumentException("You must specify a throwingSupplier (was null).");
        }

        final Class<?> supplierClass = getUserClass(throwingSupplier);
        conditionEvaluationHandler = new ConditionEvaluationHandler<Object>(null, settings);

        final Callable<Boolean> callable = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                try {
                    throwingSupplier.run();
                    conditionEvaluationHandler.handleConditionResultMatch(getMatchMessage(supplierClass, settings.getAlias()), null);
                    return true;
                } catch (AssertionError e) {
                    lastExceptionMessage = e.getMessage();
                    conditionEvaluationHandler.handleConditionResultMismatch(getMismatchMessage(supplierClass, lastExceptionMessage, settings.getAlias()), null);
                    return false;
                }
            }
        };

        conditionAwaiter = new ConditionAwaiter(callable, settings) {
            @Override
            protected String getTimeoutMessage() {
                return getMismatchMessage(supplierClass, lastExceptionMessage, settings.getAlias());
            }
        };
    }

    private Class<?> getUserClass(ThrowingRunnable throwingSupplier) {
        if (throwingSupplier instanceof ThrowingRunnableAdapter) {
            return ((ThrowingRunnableAdapter) throwingSupplier).getOriginalClass();
        }

        return throwingSupplier.getClass();
    }

    private String getMatchMessage(Class<?> supplierClass, String conditionAlias) {
        return generateDescriptionPrefix(supplierClass, conditionAlias) + " reached its end value";
    }

    private String getMismatchMessage(Class<?> supplierClass, String exceptionMessage, String conditionAlias) {
        return generateDescriptionPrefix(supplierClass, conditionAlias) + " " + exceptionMessage;
    }

    private String generateDescriptionPrefix(Class<?> supplierClass, String conditionAlias) {
        String methodDescription = generateMethodDescription(supplierClass);
        boolean hasAlias = conditionAlias != null;
        if (isLambdaClass(supplierClass)) {
            final String prefix;
            if (hasAlias) {
                prefix = "Condition with alias " + conditionAlias + " defined as a ";
            } else {
                prefix = "Condition defined as a ";
            }
            return prefix + generateLambdaErrorMessagePrefix(supplierClass, false) + methodDescription;
        }
        return "Runnable condition" + (hasAlias ? " with alias " + conditionAlias : "") + methodDescription;
    }

    private String generateMethodDescription(Class<?> supplierClass) {
        String methodDescription = "";
        Method enclosingMethod = null;
        try {
            enclosingMethod = supplierClass.getEnclosingMethod();
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
        conditionEvaluationHandler.start();
        conditionAwaiter.await();
        return null;
    }
}

class ThrowingRunnableAdapter implements ThrowingRunnable {

    private final Runnable supplier;

    public ThrowingRunnableAdapter(Runnable supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("You must specify a supplier (was null).");
        }

        this.supplier = supplier;
    }

    @Override
    public void run() throws Exception {
        supplier.run();
    }

    Class<?> getOriginalClass() {
        return supplier.getClass();
    }
}
