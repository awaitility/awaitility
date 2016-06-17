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
package org.awaitility.reflect;

import org.awaitility.reflect.exception.FieldNotFoundException;

import java.lang.reflect.Field;

class FieldNameMatcherStrategy extends FieldMatcherStrategy {

    private final String fieldName;

    /**
     * <p>Constructor for FieldNameMatcherStrategy.</p>
     *
     * @param fieldName a {@link java.lang.String} object.
     */
    public FieldNameMatcherStrategy(String fieldName) {
        if (fieldName == null || fieldName.equals("") || fieldName.startsWith(" ")) {
            throw new IllegalArgumentException("field name cannot be null.");
        }
        this.fieldName = fieldName;
    }

    /** {@inheritDoc} */
    @Override
    public boolean matches(Field field) {
        return fieldName.equals(field.getName());
    }

    /** {@inheritDoc} */
    @Override
    public void notFound(Class<?> type, boolean isInstanceField) throws FieldNotFoundException {
        throw new FieldNotFoundException(String.format(
                "No %s field named \"%s\" could be found in the class hierarchy of %s.", isInstanceField ? "instance"
                        : "static", fieldName, type.getName()));
    }

    /**
     * <p>toString.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return "fieldName " + fieldName;
    }
}
