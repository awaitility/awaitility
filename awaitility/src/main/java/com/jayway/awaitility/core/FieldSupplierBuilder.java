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
import com.jayway.awaitility.reflect.exception.FieldNotFoundException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.concurrent.Callable;

/**
 * The field supplier builder allows you to create a supplier based a field.
 */
public class FieldSupplierBuilder {

    private final Object object;
    private String expectedFieldName;
    private Class<?> expectedFieldType;
    private Class<? extends Annotation> expectedAnnotation;

    public FieldSupplierBuilder(Object object) {
        this.object = object;
    }

    /**
     * Find a field based on a type. E.g.
     *
     * <code>
     * await().until(fieldIn(object).ofType(int.class), equalTo(2));
     * </code>
     *
     * @param fieldType The type of the field.
     * @param <T> The type of the field
     * @return The supplier
     */
    public <T> NameFieldSupplier<T> ofType(Class<T> fieldType) {
        this.expectedFieldType = fieldType;
        return new NameFieldSupplier<T>();
    }
    /**
     * Find a field based on the field name. E.g.
     *
     * <code>
     * await().until(fieldIn(object).withName("fieldName"), equalTo(someObject));
     * </code>
     *
     * @param fieldName The name of the field.
     * @return The supplier
     */
    public <T> TypeFieldSupplier<T> withName(String fieldName) {
        this.expectedFieldName = fieldName;
        return new TypeFieldSupplier<T>();
    }

    /**
     * Find a field based that is annotated with a specific annotation. E.g.
     *
     * <code>
     * await().until(fieldIn(object).annotatedWith(MyAnnotation.class), equalTo(someObject));
     * </code>
     *
     * @param annotationType The field annotation
     * @return The supplier
     */
    public <T> NameAndTypeFieldSupplier<T> annotatedWith(Class<? extends Annotation> annotationType) {
        expectedAnnotation = annotationType;
        return new NameAndTypeFieldSupplier<T>();
    }

    public class NameFieldSupplier<T> implements Callable<T> {
        private Field foundField;

        public NameFieldSupplier() {
            if (expectedAnnotation != null) {
                foundField = WhiteboxImpl.getFieldAnnotatedWith(object, expectedAnnotation);
                if (!foundField.getType().isAssignableFrom(expectedFieldType)) {
                    throw new FieldNotFoundException(String.format(
                            "Couldn't find a field of type %s annotated with %s in %s.", expectedFieldType.getClass()
                                    .getName(), expectedAnnotation.getClass().getName(), WhiteboxImpl.getType(object)
                                    .getName()));
                }
            }
        }

        /**
         * Find a field based on the type and name. E.g.
         *
         * <code>
         * await().until(fieldIn(object).ofType(int.class).andWithName("fieldName"), equalTo(2));
         * </code>
         *
         * @param fieldName The name of the field
         * @return The supplier
         */
        public Callable<T> andWithName(final String fieldName) {
            return new Callable<T>() {
                @SuppressWarnings("rawtypes")
                public T call() throws Exception {
                    return (T) WhiteboxImpl.getByNameAndType(object, fieldName, expectedFieldType);
                }
            };
        }

        @SuppressWarnings("rawtypes")
        public T call() throws Exception {
            return (T) ((T) foundField == null ? WhiteboxImpl.getInternalState(object, expectedFieldType) : foundField
                    .get(object));
        }
    }

    public class TypeFieldSupplier<T> implements Callable<T> {
        public TypeFieldSupplier() {
            if (expectedAnnotation != null) {
                Field field = WhiteboxImpl.getFieldAnnotatedWith(object, expectedAnnotation);
                if (!field.getName().equals(expectedFieldName)) {
                    throw new FieldNotFoundException(String.format(
                            "Couldn't find a field with name %s annotated with %s in %s.", expectedFieldName,
                            expectedAnnotation.getClass().getName(), WhiteboxImpl.getType(object).getName()));
                }
            }
        }

        /**
         * Find a field based on the name and type. E.g.
         *
         * <code>
         * await().until(fieldIn(object).withName("fieldName").andOfType(int.class), equalTo(2));
         * </code>
         *
         * @param fieldType The type of the field
         * @return The supplier
         */
        public <S> Callable<S> andOfType(final Class<S> fieldType) {
            return new Callable<S>() {
                public S call() throws Exception {
                    return WhiteboxImpl.getByNameAndType(object, expectedFieldName, fieldType);
                }
            };
        }

        public T call() throws Exception {
            return WhiteboxImpl.<T> getInternalState(object, expectedFieldName);
        }
    }

    public class NameAndTypeFieldSupplier<T> implements Callable<T> {
        /**
         * Find a field based on the annotation and field name. E.g.
         *
         * <code>
         * await().until(fieldIn(object).annotatedWith(MyAnnotation.class).andWithName("fieldName"), equalTo(someObject));
         * </code>
         *
         * @param fieldName The type of name of the field
         * @return The supplier
         */
        public TypeFieldSupplier<T> andWithName(String fieldName) {
            FieldSupplierBuilder.this.expectedFieldName = fieldName;
            return new TypeFieldSupplier<T>();
        }

        /**
         * Find a field based on the annotation and field type. E.g.
         *
         * <code>
         * await().until(fieldIn(object).annotatedWith(MyAnnotation.class).andOfType(int.class), equalTo(2));
         * </code>
         *
         * @param type The type of the field
         * @return The supplier
         */
        public <S> NameFieldSupplier<S> andOfType(Class<S> type) {
            FieldSupplierBuilder.this.expectedFieldType = type;
            return new NameFieldSupplier<S>();
        }

        @SuppressWarnings("rawtypes")
        public T call() throws Exception {
            return (T) WhiteboxImpl.getFieldAnnotatedWith(object, expectedAnnotation).get(object);
        }
    }
}