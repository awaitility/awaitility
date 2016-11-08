/*
 * Copyright 2008 the original author or authors.
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
package org.awaitility.reflect;

import org.awaitility.reflect.exception.FieldNotFoundException;
import org.awaitility.reflect.exception.TooManyFieldsFoundException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Various utilities for accessing internals of a class. Basically a simplified
 * reflection utility. Copied with permission from PowerMock project.
 */
public class WhiteboxImpl {

    /**
     * Get the value of a field using reflection. This method will iterate
     * through the entire class hierarchy and return the value of the first
     * field named <tt>fieldName</tt>. If you want to get a specific field value
     * at specific place in the class hierarchy please refer to
     *
     * @param <T>       the generic type
     * @param object    the object to modify
     * @param fieldName the name of the field
     * @return the internal state.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getInternalState(Object object, String fieldName) {
        Field foundField = findFieldInHierarchy(object, fieldName);
        try {
            return (T) foundField.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Internal error: Failed to get field in method getInternalState.", e);
        }
    }

    /**
     * Find field in hierarchy.
     *
     * @param object    the object
     * @param fieldName the field name
     * @return the field
     */
    private static Field findFieldInHierarchy(Object object, String fieldName) {
        return findFieldInHierarchy(object, new FieldNameMatcherStrategy(fieldName));
    }

    /**
     * Find field in hierarchy.
     *
     * @param object   the object
     * @param strategy the strategy
     * @return the field
     */
    private static Field findFieldInHierarchy(Object object, FieldMatcherStrategy strategy) {
        assertObjectInGetInternalStateIsNotNull(object);
        return findSingleFieldUsingStrategy(strategy, object, true, getType(object));
    }

    /**
     * Assert object in get internal state is not null.
     *
     * @param object the object
     */
    private static void assertObjectInGetInternalStateIsNotNull(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("The object containing the field cannot be null");
        }
    }

    /**
     * Find single field using strategy.
     *
     * @param strategy       the strategy
     * @param object         the object
     * @param checkHierarchy the check hierarchy
     * @param startClass     the start class
     * @return the field
     */
    private static Field findSingleFieldUsingStrategy(FieldMatcherStrategy strategy, Object object,
                                                      boolean checkHierarchy, Class<?> startClass) {
        assertObjectInGetInternalStateIsNotNull(object);
        Field foundField = null;
        final Class<?> originalStartClass = startClass;
        while (startClass != null) {
            final Field[] declaredFields = startClass.getDeclaredFields();
            for (Field field : declaredFields) {
                if (strategy.matches(field) && hasFieldProperModifier(object, field)) {
                    if (foundField != null) {
                        throw new TooManyFieldsFoundException("Two or more fields matching " + strategy + ".");
                    }
                    foundField = field;
                }
            }
            if (foundField != null) {
                break;
            } else if (!checkHierarchy) {
                break;
            }
            startClass = startClass.getSuperclass();
        }
        if (foundField == null) {
            strategy.notFound(originalStartClass, !isClass(object));
            return null;
        }
        foundField.setAccessible(true);
        return foundField;
    }

    /**
     * Checks for field proper modifier.
     *
     * @param object the object
     * @param field  the field
     * @return true, if successful
     */
    private static boolean hasFieldProperModifier(Object object, Field field) {
        return ((object instanceof Class<?> && Modifier.isStatic(field.getModifiers())) || !(object instanceof Class<?> || Modifier
                .isStatic(field.getModifiers())));
    }

    /**
     * Get the value of a field using reflection. This method will traverse the
     * super class hierarchy until the first field of type <tt>fieldType</tt> is
     * found. The value of this field will be returned.
     *
     * @param <T>       the generic type
     * @param object    the object to modify
     * @param fieldType the type of the field
     * @return the internal state
     */
    @SuppressWarnings("unchecked")
    public static <T> T getInternalState(Object object, Class<T> fieldType) {
        Field foundField = findFieldInHierarchy(object, new AssignableToFieldTypeMatcherStrategy(fieldType));
        try {
            return (T) foundField.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Internal error: Failed to get field in method getInternalState.", e);
        }
    }

    /**
     * Throw exception if field was not found.
     *
     * @param type      the type
     * @param fieldName the field name
     * @param field     the field
     */
    public static void throwExceptionIfFieldWasNotFound(Class<?> type, String fieldName, Field field) {
        if (field == null) {
            throw new FieldNotFoundException("No field was found with name '" + fieldName + "' in class "
                    + type.getName() + ".");
        }
    }

    /**
     * Gets the type.
     *
     * @param object the object
     * @return The type of the of an object.
     */
    public static Class<?> getType(Object object) {
        Class<?> type = null;
        if (isClass(object)) {
            type = (Class<?>) object;
        } else if (object != null) {
            type = object.getClass();
        }
        return type;
    }

    /**
     * Get field annotated with a particular annotation. This method traverses
     * the class hierarchy when checking for the annotation.
     *
     * @param object         The object to look for annotations. Note that if're you're
     *                       passing an object only instance fields are checked, passing a
     *                       class will only check static fields.
     * @param annotationType The annotation types to look for
     * @return A set of all fields containing the particular annotation(s).
     * @since 1.3
     */
    public static Field getFieldAnnotatedWith(Object object, Class<? extends Annotation> annotationType) {
        return findSingleFieldUsingStrategy(new FieldAnnotationMatcherStrategy(annotationType), object, true,
                getType(object));
    }

    /**
     * Checks if is class.
     *
     * @param argument the argument
     * @return a boolean.
     */
    public static boolean isClass(Object argument) {
        return argument instanceof Class<?>;
    }

    /**
     * <p>getByNameAndType.</p>
     *
     * @param object a {@link java.lang.Object} object.
     * @param fieldName a {@link java.lang.String} object.
     * @param expectedFieldType a {@link java.lang.Class} object.
     * @param <T> a T object.
     * @return a T object.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getByNameAndType(Object object, String fieldName, Class<T> expectedFieldType) {
        Field foundField = findSingleFieldUsingStrategy(new FieldNameAndTypeMatcherStrategy(fieldName,
                expectedFieldType), object, true, getType(object));
        try {
            return (T) foundField.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Internal error: Failed to get field in method getInternalState.", e);
        }
    }
}
