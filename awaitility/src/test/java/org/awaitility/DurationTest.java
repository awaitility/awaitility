/*
 * Copyright 2015 the original author or authors.
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

package org.awaitility;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(Enclosed.class)
public class DurationTest {

    public static class Equals {
        @Test public void
        duration_is_equal_to_other_duration_if_number_of_milliseconds_are_the_same() {
            // Given
            final Duration duration1 = new Duration(1000, MILLISECONDS);
            final Duration duration2 = new Duration(1, SECONDS);

            // When
            final boolean equals = duration1.equals(duration2);

            // Then
            assertThat(equals, is(true));
        }
    }

    public static class Plus {
        @Test public void
        plus_can_add_amount_from_greater_time_unit_with_amount_with_lower_time_unit() {
            // When
            Duration sum = new Duration(2, SECONDS).plus(4, MILLISECONDS);

            // Then
            assertThat(sum.getValue(), is(2004L));
            assertThat(sum.getTimeUnit(), is(MILLISECONDS));
        }

        @Test public void
        plus_can_add_amount_from_lower_time_unit_with_amount_with_greater_time_unit() {
            // When
            Duration sum = new Duration(4, MILLISECONDS).plus(2, SECONDS);

            // Then
            assertThat(sum.getValue(), is(2004L));
            assertThat(sum.getTimeUnit(), is(MILLISECONDS));
        }

        @Test public void
        plus_can_add_amount_with_same_time_unit() {
            // When
            Duration sum = new Duration(4, SECONDS).plus(2, SECONDS);

            // Then
            assertThat(sum.getValue(), is(6L));
            assertThat(sum.getTimeUnit(), is(SECONDS));
        }

        @Test public void
        plus_can_add_amount() {
            // When
            Duration sum = new Duration(4, SECONDS).plus(8);

            // Then
            assertThat(sum.getValue(), is(12L));
            assertThat(sum.getTimeUnit(), is(SECONDS));
        }

        @Test public void
        plus_can_add_duration() {
            // When
            Duration sum = new Duration(4, SECONDS).plus(Duration.FIVE_HUNDRED_MILLISECONDS);

            // Then
            assertThat(sum.getValue(), is(4500L));
            assertThat(sum.getTimeUnit(), is(MILLISECONDS));
        }

        @Test public void
        plus_can_add_forever() {
            // When
            Duration sum = new Duration(4, SECONDS).plus(Duration.FOREVER);

            // Then
            assertThat(sum, sameInstance(Duration.FOREVER));
        }

        @Test public void
        forever_can_add_other_amount() {
            // When
            Duration sum = Duration.FOREVER.plus(4);

            // Then
            assertThat(sum, sameInstance(Duration.FOREVER));
        }

        @Test public void
        forever_can_add_zero() {
            // When
            Duration sum = Duration.FOREVER.plus(Duration.ZERO);

            // Then
            assertThat(sum, sameInstance(Duration.FOREVER));
        }

        @Test public void
        zero_can_add_forever() {
            // When
            Duration sum = Duration.ZERO.plus(Duration.FOREVER);

            // Then
            assertThat(sum, sameInstance(Duration.FOREVER));
        }

        @Test public void
        forever_can_plus_with_other_duration() {
            // When
            Duration sum = Duration.FOREVER.plus(new Duration(4, SECONDS));

            // Then
            assertThat(sum, sameInstance(Duration.FOREVER));
        }

        @Test public void
        plus_can_add_zero() {
            // When
            Duration sum = new Duration(4, SECONDS).plus(Duration.ZERO);

            // Then
            assertThat(sum.getValue(), is(4L));
            assertThat(sum.getTimeUnit(), is(SECONDS));
        }

        @Test public void
        zero_can_plus_other_duration() {
            // When
            Duration sum = Duration.ZERO.plus(new Duration(4, SECONDS));

            // Then
            assertThat(sum.getValue(), is(4L));
            assertThat(sum.getTimeUnit(), is(SECONDS));
        }
    }


    public static class Minus {
        @Rule
        public ExpectedException exception = ExpectedException.none();

        @Test public void
        minus_can_subtract_amount_from_greater_time_unit_with_amount_with_lower_time_unit() {
            // When
            Duration sum = new Duration(2, SECONDS).minus(4, MILLISECONDS);

            // Then
            assertThat(sum.getValue(), is(1996L));
            assertThat(sum.getTimeUnit(), is(MILLISECONDS));
        }

        @Test public void
        minus_can_subtract_amount_from_lower_time_unit_with_amount_with_greater_time_unit() {
            // When
            Duration sum = new Duration(4000, MILLISECONDS).minus(2, SECONDS);

            // Then
            assertThat(sum.getValue(), is(2000L));
            assertThat(sum.getTimeUnit(), is(MILLISECONDS));
        }

        @Test public void
        minus_can_subtract_amount_with_same_time_unit() {
            // When
            Duration sum = new Duration(4, SECONDS).minus(2, SECONDS);

            // Then
            assertThat(sum.getValue(), is(2L));
            assertThat(sum.getTimeUnit(), is(SECONDS));
        }

        @Test public void
        minus_can_subtract_amount() {
            // When
            Duration sum = new Duration(4, SECONDS).minus(2);

            // Then
            assertThat(sum.getValue(), is(2L));
            assertThat(sum.getTimeUnit(), is(SECONDS));
        }

        @Test public void
        minus_can_subtract_duration() {
            // When
            Duration sum = new Duration(4, SECONDS).minus(Duration.FIVE_HUNDRED_MILLISECONDS);

            // Then
            assertThat(sum.getValue(), is(3500L));
            assertThat(sum.getTimeUnit(), is(MILLISECONDS));
        }

        @Test public void
        minus_can_subtract_forever() {
            // When
            Duration sum = new Duration(4, SECONDS).minus(Duration.FOREVER);

            // Then
            assertThat(sum, sameInstance(Duration.ZERO));
        }

        @Test public void
        forever_can_minus_with_other_duration() {
            // When
            Duration sum = Duration.FOREVER.minus(new Duration(4, SECONDS));

            // Then
            assertThat(sum, sameInstance(Duration.FOREVER));
        }

        @Test public void
        forever_can_subtract_other_amount() {
            // When
            Duration sum = Duration.FOREVER.minus(4);

            // Then
            assertThat(sum, sameInstance(Duration.FOREVER));
        }

        @Test public void
        forever_can_subtract_other_zero() {
            // When
            Duration sum = Duration.FOREVER.minus(Duration.ZERO);

            // Then
            assertThat(sum, sameInstance(Duration.FOREVER));
        }

        @Test public void
        zero_can_subtract_other_forever() {
            // When
            Duration sum = Duration.ZERO.minus(Duration.FOREVER);

            // Then
            assertThat(sum, sameInstance(Duration.ZERO));
        }

        @Test public void
        minus_can_subtract_zero() {
            // When
            Duration sum = new Duration(4, SECONDS).minus(Duration.ZERO);

            // Then
            assertThat(sum.getValue(), is(4L));
            assertThat(sum.getTimeUnit(), is(SECONDS));
        }

        @Test public void
        zero_cannot_minus_other_duration() {
            exception.expect(IllegalArgumentException.class);
            exception.expectMessage("value must be >= 0, was -4000");

            // When
            Duration sum = Duration.ZERO.minus(new Duration(4, SECONDS));

            // Then
            assertThat(sum.getValue(), is(4L));
            assertThat(sum.getTimeUnit(), is(SECONDS));
        }
    }

    public static class Multiply {

        @Test public void
        multiply_can_multiply_amount() {
            // When
            Duration sum = new Duration(4, SECONDS).multiply(2);

            // Then
            assertThat(sum.getValue(), is(8L));
            assertThat(sum.getTimeUnit(), is(SECONDS));
        }

        @Test public void
        forever_can_multiply_with_amount() {
            // When
            Duration sum = Duration.FOREVER.multiply(3);

            // Then
            assertThat(sum, sameInstance(Duration.FOREVER));
        }

        @Test public void
        multiply_can_multiply_zero() {
            // When
            Duration sum = new Duration(4, SECONDS).multiply(0);

            // Then
            assertThat(sum, sameInstance(Duration.ZERO));
        }

        @Test public void
        zero_can_multiply_other_amount() {
            // When
            Duration sum = Duration.ZERO.multiply(2);

            // Then
            assertThat(sum, sameInstance(Duration.ZERO));
        }
    }

    public static class Divide {
        @Rule
        public ExpectedException exception = ExpectedException.none();

        @Test public void
        divide_can_divide_amount() {
            // When
            Duration sum = new Duration(4, SECONDS).divide(2);

            // Then
            assertThat(sum.getValue(), is(2L));
            assertThat(sum.getTimeUnit(), is(SECONDS));
        }

        @Test public void
        forever_can_divide_with_amount() {
            // When
            Duration sum = Duration.FOREVER.divide(3);

            // Then
            assertThat(sum, sameInstance(Duration.FOREVER));
        }

        @Test public void
        divide_cannot_divide_zero() {
            exception.expect(ArithmeticException.class);
            exception.expectMessage("/ by zero");

            // When
            Duration sum = new Duration(4, SECONDS).divide(0);

            // Then
            assertThat(sum, sameInstance(Duration.ZERO));
        }

        @Test public void
        zero_can_divide_other_amount() {
            // When
            Duration sum = Duration.ZERO.divide(2);

            // Then
            assertThat(sum, sameInstance(Duration.ZERO));
        }
    }
}
