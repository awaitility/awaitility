package com.jayway.awaitility;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DurationTest {

    @Test public void
    duration_is_equal_to_other_duration_if_number_of_milliseconds_are_the_same() {
        // Given
        final Duration duration1 = new Duration(1000, TimeUnit.MILLISECONDS);
        final Duration duration2 = new Duration(1, TimeUnit.SECONDS);

        // When
        final boolean equals = duration1.equals(duration2);

        // Then
        assertThat(equals, is(true));
    }
}
