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

import com.jayway.awaitility.reflect.WhiteboxImpl;
import org.hamcrest.Matcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

class CallableHamcrestCondition<T> extends AbstractHamcrestCondition<T> {

    public CallableHamcrestCondition(final Callable<T> supplier, final Matcher<? super T> matcher, ConditionSettings settings) {
        super(supplier, matcher, settings);
    }

    @Override
    String getCallableDescription(final Callable<T> supplier) {
        final Class<? extends Callable> supplierClass = supplier.getClass();
        Method enclosingMethod = supplierClass.getEnclosingMethod();
        if(isFieldSupplier(supplierClass)) {
            return generateFieldSupplierErrorMessage(supplier);
        } else if (supplierClass.isAnonymousClass() && enclosingMethod != null) {
            return enclosingMethod.getDeclaringClass().getName() + "." + enclosingMethod.getName() + " Callable";
        } else {
            return supplierClass.getName();
        }
    }

    private boolean isFieldSupplier(Class<?> supplierClass) {
        return supplierClass.isMemberClass() && supplierClass.getEnclosingClass() == FieldSupplierBuilder.class;
    }

    private String generateFieldSupplierErrorMessage(Callable<T> supplier) {
        final FieldSupplierBuilder fieldSupplier = WhiteboxImpl.getInternalState(supplier, "this$0");
        final Class<? extends Annotation> expectedAnnotation = fieldSupplier.getExpectedAnnotation();
        final String expectedFieldName = fieldSupplier.getExpectedFieldName();
        final Class<?> expectedFieldType = fieldSupplier.getExpectedFieldType();
        final Object object = fieldSupplier.getObject();

        StringBuilder builder = new StringBuilder();
        if(expectedFieldName == null) {
            builder.append("Field in ");
            builder.append(object.getClass().getName());
            if(expectedAnnotation != null) {
                builder.append(" annotated with ");
                builder.append(expectedAnnotation.getName());
                builder.append(" and");
            }
            builder.append(" of type ");
            builder.append(expectedFieldType);
        } else {

            try {
                final Field declaredField = object.getClass().getDeclaredField(expectedFieldName);
                builder.append("Field ");
                builder.append(declaredField);
            } catch (Exception e) {
                throw new RuntimeException("Internal error", e);
            }
        }

        return builder.toString();
    }
}
