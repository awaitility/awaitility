/*
 * Copyright 2026 the original author or authors.
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

/**
 * An interface for matching values in Awaitility conditions.
 * <p>
 * This is intentionally <b>not</b> a {@code @FunctionalInterface} to avoid
 * ambiguity with {@link java.util.function.Predicate} overloads. Implementations
 * must provide a {@link #describe()} method for meaningful error messages.
 * </p>
 *
 * @param <T> the type of value to match
 */
public interface Matcher<T> {

    /**
     * Evaluates the matcher for the given value.
     *
     * @param value the value to match against
     * @return {@code true} if the value matches, {@code false} otherwise
     */
    boolean matches(T value);

    /**
     * Returns a human-readable description of the expected value.
     *
     * @return a description of what the matcher expects
     */
    String describe();

    /**
     * Returns a human-readable description of why the given value did not match.
     *
     * @param value the value that did not match
     * @return a description of the mismatch
     */
    default String describeMismatch(T value) {
        return "was <" + value + ">";
    }
}
