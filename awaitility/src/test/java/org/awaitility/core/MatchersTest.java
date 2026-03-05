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

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MatchersTest {

    // equalTo

    @Test
    public void equalToMatchesEqualValues() {
        assertTrue(Matchers.equalTo(42).matches(42));
    }

    @Test
    public void equalToDoesNotMatchDifferentValues() {
        assertFalse(Matchers.equalTo(42).matches(99));
    }

    @Test
    public void equalToMatchesNullWithNull() {
        assertTrue(Matchers.equalTo(null).matches(null));
    }

    @Test
    public void equalToDoesNotMatchNullWithNonNull() {
        assertFalse(Matchers.equalTo(null).matches("hello"));
    }

    @Test
    public void equalToDoesNotMatchNonNullWithNull() {
        assertFalse(Matchers.equalTo("hello").matches(null));
    }

    @Test
    public void equalToDescribeFormatsString() {
        assertEquals("\"hello\"", Matchers.equalTo("hello").describe());
    }

    @Test
    public void equalToDescribeFormatsInteger() {
        assertEquals("<42>", Matchers.equalTo(42).describe());
    }

    @Test
    public void equalToDescribeFormatsLong() {
        assertEquals("<42L>", Matchers.equalTo(42L).describe());
    }

    @Test
    public void equalToDescribeFormatsShort() {
        assertEquals("<42s>", Matchers.equalTo((short) 42).describe());
    }

    @Test
    public void equalToDescribeFormatsFloat() {
        assertEquals("<1.5F>", Matchers.equalTo(1.5f).describe());
    }

    @Test
    public void equalToDescribeMismatch() {
        assertEquals("was <99>", Matchers.equalTo(42).describeMismatch(99));
    }

    @Test
    public void equalToDescribeMismatchFormatsString() {
        assertEquals("was \"actual\"", Matchers.equalTo("expected").describeMismatch("actual"));
    }

    // greaterThan

    @Test
    public void greaterThanMatchesGreaterValue() {
        assertTrue(Matchers.greaterThan(5).matches(6));
    }

    @Test
    public void greaterThanDoesNotMatchEqualValue() {
        assertFalse(Matchers.greaterThan(5).matches(5));
    }

    @Test
    public void greaterThanDoesNotMatchLesserValue() {
        assertFalse(Matchers.greaterThan(5).matches(4));
    }

    @Test
    public void greaterThanDoesNotMatchNull() {
        assertFalse(Matchers.greaterThan(5).matches(null));
    }

    @Test
    public void greaterThanDescribe() {
        assertEquals("a value greater than <5>", Matchers.greaterThan(5).describe());
    }

    @Test
    public void greaterThanDescribeMismatchForNull() {
        assertEquals("was null", Matchers.greaterThan(5).describeMismatch(null));
    }

    @Test
    public void greaterThanDescribeMismatchForEqualValue() {
        assertEquals("<5> was equal to <5>", Matchers.greaterThan(5).describeMismatch(5));
    }

    @Test
    public void greaterThanDescribeMismatchForLesserValue() {
        assertEquals("<3> was less than <5>", Matchers.greaterThan(5).describeMismatch(3));
    }

    // is(Matcher)

    @Test
    public void isDelegateReturnsSameMatcher() {
        Matcher<Integer> delegate = Matchers.equalTo(1);
        assertTrue(Matchers.is(delegate) == delegate);
    }

    // is(T)

    @Test
    public void isValueMatchesEqualValue() {
        assertTrue(Matchers.is(42).matches(42));
    }

    @Test
    public void isValueDoesNotMatchDifferentValue() {
        assertFalse(Matchers.is(42).matches(99));
    }

    // hasItem

    @Test
    public void hasItemMatchesWhenItemPresent() {
        assertTrue(Matchers.hasItem("b").matches(Arrays.asList("a", "b", "c")));
    }

    @Test
    public void hasItemDoesNotMatchWhenItemAbsent() {
        assertFalse(Matchers.hasItem("z").matches(Arrays.asList("a", "b", "c")));
    }

    @Test
    public void hasItemDoesNotMatchNull() {
        assertFalse(Matchers.hasItem("a").matches(null));
    }

    @Test
    public void hasItemDoesNotMatchEmptyCollection() {
        assertFalse(Matchers.hasItem("a").matches(Collections.emptyList()));
    }

    @Test
    public void hasItemDescribe() {
        assertEquals("a collection containing <hello>", Matchers.hasItem("hello").describe());
    }

    // instanceOf

    @Test
    public void instanceOfMatchesExactType() {
        assertTrue(Matchers.instanceOf(IllegalArgumentException.class).matches(new IllegalArgumentException()));
    }

    @Test
    public void instanceOfMatchesSubtype() {
        assertTrue(Matchers.instanceOf(RuntimeException.class).matches(new IllegalArgumentException()));
    }

    @Test
    public void instanceOfDoesNotMatchDifferentType() {
        assertFalse(Matchers.instanceOf(IllegalStateException.class).matches(new IllegalArgumentException()));
    }

    @Test
    public void instanceOfDescribe() {
        assertEquals("an instance of java.lang.IllegalArgumentException", Matchers.instanceOf(IllegalArgumentException.class).describe());
    }
}
