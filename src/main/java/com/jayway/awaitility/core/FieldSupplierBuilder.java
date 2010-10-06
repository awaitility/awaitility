package com.jayway.awaitility.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.concurrent.Callable;

import com.jayway.awaitility.reflect.WhiteboxImpl;
import com.jayway.awaitility.reflect.exception.FieldNotFoundException;

public class FieldSupplierBuilder {

    private final Object object;
    private String expectedFieldName;
    private Class<?> expectedFieldType;
    private Class<? extends Annotation> expectedAnnotation;

    public FieldSupplierBuilder(Object object) {
        this.object = object;
    }

    public <T> NameFieldSupplier<T> ofType(Class<T> fieldType) {
        this.expectedFieldType = fieldType;
        return new NameFieldSupplier<T>();
    }

    public <T> TypeFieldSupplier<T> withName(String fieldName) {
        this.expectedFieldName = fieldName;
        return new TypeFieldSupplier<T>();
    }

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

    public class TypeFieldSupplier<Type> implements Callable<Type> {
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

        public <T> Callable<T> andOfType(final Class<T> fieldType) {
            return new Callable<T>() {
                public T call() throws Exception {
                    return WhiteboxImpl.getByNameAndType(object, expectedFieldName, fieldType);
                }
            };
        }

        public Type call() throws Exception {
            return WhiteboxImpl.getInternalState(object, expectedFieldName);
        }
    }

    public class NameAndTypeFieldSupplier<Type> implements Callable<Type> {
        public TypeFieldSupplier<Type> andWithName(String fieldName) {
            FieldSupplierBuilder.this.expectedFieldName = fieldName;
            return new TypeFieldSupplier<Type>();
        }

        public <T> NameFieldSupplier<T> andOfType(Class<T> type) {
            FieldSupplierBuilder.this.expectedFieldType = type;
            return new NameFieldSupplier<T>();
        }

        @SuppressWarnings("rawtypes")
        public Type call() throws Exception {
            return (Type) WhiteboxImpl.getFieldAnnotatedWith(object, expectedAnnotation).get(object);
        }
    }
}