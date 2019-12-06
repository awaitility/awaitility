/*
 * Copyright 2019 the original author or authors.
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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

public final class FailsWithMatcher<T extends Throwable> extends TypeSafeMatcher<Callable<? super Object>> {

    private final Matcher<? super T> matcher;

    private final AtomicReference<Throwable> caught = new AtomicReference<Throwable>();

    private FailsWithMatcher(final Matcher<? super T> matcher) {
        this.matcher = matcher;
    }

    public static <T extends Throwable> Matcher<Callable<? super Object>> failsWith(final Class<T> throwableType) {
        return new FailsWithMatcher<>(instanceOf(throwableType));
    }

    public static <T extends Throwable> Matcher<Callable<? super Object>> failsWith(final Class<T> throwableType, final Matcher<? super T> throwableMatcher) {
        return new FailsWithMatcher<T>(allOf(instanceOf(throwableType), throwableMatcher));
    }

    @Override
    protected boolean matchesSafely(final Callable<? super Object> runnable) {
        try {
            runnable.call();
            return false;
        } catch (final Throwable ex) {
            boolean matches = matcher.matches(ex);
            if (matches) {
                caught.set(ex);
            }
            return matches;
        }
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("fails with ").appendDescriptionOf(matcher);
    }

    public T getCaughtException() {
        //noinspection unchecked
        return (T) caught.get();
    }
}