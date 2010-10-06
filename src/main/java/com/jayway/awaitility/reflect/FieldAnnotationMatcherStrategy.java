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
package com.jayway.awaitility.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import com.jayway.awaitility.reflect.exception.FieldNotFoundException;

public class FieldAnnotationMatcherStrategy extends FieldMatcherStrategy {

    final Class<? extends Annotation> annotation;

    public FieldAnnotationMatcherStrategy(Class<? extends Annotation> annotation) {
        if (annotation == null) {
            throw new IllegalArgumentException("You must specify an annotation.");
        }
        this.annotation = annotation;
    }

    @Override
    public boolean matches(Field field) {
        if (field.isAnnotationPresent(annotation)) {
            return true;
        }
        return false;
    }

    @Override
    public void notFound(Class<?> type, boolean isInstanceField) throws FieldNotFoundException {
        throw new FieldNotFoundException("No field with annotation of type \"" + annotation.getName()
                + "\" could be found in the class hierarchy of " + type.getName() + ".");
    }

    @Override
    public String toString() {
        return "annotation " + annotation.getName();
    }
}