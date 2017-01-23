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

import org.awaitility.reflect.WhiteboxImpl;
import org.awaitility.reflect.exception.FieldNotFoundException;

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

    /**
     * <p>Constructor for FieldSupplierBuilder.</p>
     *
     * @param object a {@link java.lang.Object} object.
     */
    public FieldSupplierBuilder(Object object) {
        assertNotNullParameter(object, "Object passed to fieldIn");
        this.object = object;
    }

    /**
     * Find a field based on a type. E.g.
     * <p>&nbsp;</p>
     * <code>
     * await().until(fieldIn(object).ofType(int.class), equalTo(2));
     * </code>
     * <p>&nbsp;</p>
     * You can also specify the field more accurately by continuing the statement:
     * E.g.
     * <code>
     * await().until(fieldIn(object).ofType(int.class).andWithName("fieldName"), equalTo(2));
     * </code>
     * <p>&nbsp;</p>
     * or
     * <p>&nbsp;</p>
     * <code>
     * await().until(fieldIn(object).ofType(int.class).andAnnotatedWith(MyAnnotation.class).andWithName("fieldName"), equalTo(2));
     * </code>
     *
     * @param fieldType The type of the field.
     * @param <T>       The type of the field
     * @return The field supplier
     */
    public <T> NameAndAnnotationFieldSupplier<T> ofType(Class<T> fieldType) {
        this.expectedFieldType = fieldType;
        return new NameAndAnnotationFieldSupplier<T>();
    }

    public class NameFieldSupplier<T> implements Callable<T> {
        private final Field foundField;

        public NameFieldSupplier() {
            foundField = WhiteboxImpl.getFieldAnnotatedWith(object, expectedAnnotation);
            if (!foundField.getType().isAssignableFrom(expectedFieldType)) {
                throw new FieldNotFoundException(String.format(
                        "Couldn't find a field of type %s annotated with %s in %s.", expectedFieldType.getClass()
                                .getName(), expectedAnnotation.getClass().getName(), WhiteboxImpl.getType(object)
                                .getName()
                ));
            }
        }

        /**
         * Find a field based on the type and name. E.g.
         * <p>&nbsp;</p>
         * <code>
         * await().until(fieldIn(object).ofType(int.class).andWithName("fieldName"), equalTo(2));
         * </code>
         *
         * @param fieldName The name of the field
         * @return The supplier
         */
        public Callable<T> andWithName(final String fieldName) {
            assertNotNullParameter(fieldName, "fieldName");
            return new Callable<T>() {
                public T call() throws Exception {
                    return (T) WhiteboxImpl.getByNameAndType(object, fieldName, expectedFieldType);
                }
            };
        }

        public T call() throws Exception {
            return (T) (foundField == null ? WhiteboxImpl.getInternalState(object, expectedFieldType) : foundField
                    .get(object));
        }
    }


    public class NameAndAnnotationFieldSupplier<T> implements Callable<T> {
        /**
         * Find a field based on the type and name. E.g.
         * <p>&nbsp;</p>
         * <code>
         * await().until(fieldIn(object).ofType(int.class).andWithName("fieldName"), equalTo(2));
         * </code>
         *
         * @param fieldName The name of the field
         * @return The supplier
         */
        public AnnotationFieldSupplier<T> andWithName(final String fieldName) {
            assertNotNullParameter(fieldName, "fieldName");
            expectedFieldName = fieldName;
            return new AnnotationFieldSupplier<T>();
        }

        /**
         * Find a field based on the type and an annotation. E.g.
         * <p>&nbsp;</p>
         * <code>
         * await().until(fieldIn(object).ofType(int.class).andAnnotatedWith(MyAnnotation.class), equalTo(2));
         * </code>
         *
         * @param annotationType The name of the field
         * @return The supplier
         */
        public NameFieldSupplier<T> andAnnotatedWith(Class<? extends Annotation> annotationType) {
            assertNotNullParameter(annotationType, "annotationType");
            expectedAnnotation = annotationType;
            return new NameFieldSupplier<T>();
        }

        public T call() throws Exception {
            return (T) WhiteboxImpl.getInternalState(object, expectedFieldType);
        }
    }

    public class AnnotationFieldSupplier<T> implements Callable<T> {
        public AnnotationFieldSupplier() {
        }

        /**
         * Find a field based on a name, type and annotation. E.g.
         * <p>&nbsp;</p>
         * <code>
         * await().until(fieldIn(object).ofType(int.class).andWithName("fieldName").andAnnotatedWith(MyAnnotation.class), equalTo(2));
         * </code>
         *
         * @param annotationType The type of the annotation
         * @return The supplier
         */
        public Callable<T> andAnnotatedWith(final Class<? extends Annotation> annotationType) {
            assertNotNullParameter(annotationType, "annotationType");
            Field field = WhiteboxImpl.getFieldAnnotatedWith(object, annotationType);
            if (!field.getName().equals(expectedFieldName)) {
                throw new FieldNotFoundException(String.format(
                        "Couldn't find a field with name %s annotated with %s in %s.", expectedFieldName,
                        annotationType.getClass().getName(), WhiteboxImpl.getType(object).getName()));
            }
            expectedAnnotation = annotationType;
            return this;
        }

        public T call() throws Exception {
            return (T) WhiteboxImpl.getByNameAndType(object, expectedFieldName, expectedFieldType);
        }
    }

    public class NameAndTypeFieldSupplier<T> implements Callable<T> {
        /**
         * Find a field based on the annotation and field name. E.g.
         * <p>&nbsp;</p>
         * <code>
         * await().until(fieldIn(object).annotatedWith(MyAnnotation.class).andWithName("fieldName"), equalTo(someObject));
         * </code>
         *
         * @param fieldName The type of name of the field
         * @return The supplier
         */
        public AnnotationFieldSupplier<T> andWithName(String fieldName) {
            assertNotNullParameter(fieldName, "fieldName");
            FieldSupplierBuilder.this.expectedFieldName = fieldName;
            return new AnnotationFieldSupplier<T>();
        }

        /**
         * Find a field based on the annotation and field type. E.g.
         * <p>&nbsp;</p>
         * <code>
         * await().until(fieldIn(object).annotatedWith(MyAnnotation.class).andOfType(int.class), equalTo(2));
         * </code>
         *
         * @param type The class representing the type of the field
         * @param <S>  The type of the field
         * @return The supplier
         */
        public <S> NameAndAnnotationFieldSupplier<S> andOfType(Class<S> type) {
            assertNotNullParameter(type, "Expected field type");
            FieldSupplierBuilder.this.expectedFieldType = type;
            return new NameAndAnnotationFieldSupplier<S>();
        }

        @SuppressWarnings("rawtypes")
        public T call() throws Exception {
            return (T) WhiteboxImpl.getFieldAnnotatedWith(object, expectedAnnotation).get(object);
        }
    }

    private void assertNotNullParameter(Object parameterValue, String name) {
        if (parameterValue == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
    }

    Object getObject() {
        return object;
    }

    String getExpectedFieldName() {
        return expectedFieldName;
    }

    Class<?> getExpectedFieldType() {
        return expectedFieldType;
    }

    Class<? extends Annotation> getExpectedAnnotation() {
        return expectedAnnotation;
    }
}
