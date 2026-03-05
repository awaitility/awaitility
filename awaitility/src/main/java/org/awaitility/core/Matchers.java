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

import java.util.Objects;

/**
 * Built-in {@link Matcher} factory methods for common matching operations.
 * <p>
 * These matchers can be used with {@link org.awaitility.core.ConditionFactory#until(java.util.concurrent.Callable, Matcher)}.
 * </p>
 */
public final class Matchers {

    private Matchers() {
    }

    public static <T> Matcher<T> equalTo(T expected) {
        return new Matcher<T>() {
            @Override
            public boolean matches(T value) {
                return Objects.equals(value, expected);
            }

            @Override
            public String describe() {
                return formatValue(expected);
            }

            @Override
            public String describeMismatch(T value) {
                return "was " + formatValue(value);
            }
        };
    }

    private static String formatValue(Object value) {
        if (value instanceof String) {
            return "\"" + value + "\"";
        }
        if (value instanceof Long) {
            return "<" + value + "L>";
        }
        if (value instanceof Short) {
            return "<" + value + "s>";
        }
        if (value instanceof Float) {
            return "<" + value + "F>";
        }
        return "<" + value + ">";
    }

    public static <T extends Comparable<T>> Matcher<T> greaterThan(T expected) {
        return new Matcher<T>() {
            @Override
            public boolean matches(T value) {
                return value != null && value.compareTo(expected) > 0;
            }

            @Override
            public String describe() {
                return "a value greater than " + formatValue(expected);
            }

            @Override
            public String describeMismatch(T value) {
                if (value == null) {
                    return "was null";
                }
                int cmp = value.compareTo(expected);
                if (cmp == 0) {
                    return formatValue(value) + " was equal to " + formatValue(expected);
                }
                return formatValue(value) + " was less than " + formatValue(expected);
            }
        };
    }

    public static <T> Matcher<T> is(Matcher<T> delegate) {
        return delegate;
    }

    @SuppressWarnings("unchecked")
    public static <T> Matcher<T> is(T value) {
        return (Matcher<T>) equalTo(value);
    }

    public static <T> Matcher<Iterable<? super T>> hasItem(T item) {
        return new Matcher<Iterable<? super T>>() {
            @Override
            public boolean matches(Iterable<? super T> value) {
                if (value == null) return false;
                for (Object element : value) {
                    if (Objects.equals(element, item)) return true;
                }
                return false;
            }

            @Override
            public String describe() {
                return "a collection containing <" + item + ">";
            }
        };
    }

    public static Matcher<Throwable> instanceOf(Class<?> type) {
        return new Matcher<Throwable>() {
            @Override
            public boolean matches(Throwable value) {
                return type.isInstance(value);
            }

            @Override
            public String describe() {
                return "an instance of " + type.getName();
            }
        };
    }
}
