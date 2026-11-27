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

import org.hamcrest.Matcher;
import org.junit.Test;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

public class StartEvaluationEventTest {

    @Test
    public void fiveArgConstructorPreservesFieldsAndDefaultsPollDelayToZero() {
        Matcher<Integer> matcher = equalTo(42);
        StartEvaluationEvent<Integer> event = new StartEvaluationEvent<>("Starting evaluation", matcher, 10L, 5000L, "myAlias");

        assertThat(event.getDescription(), is("Starting evaluation"));
        assertThat(event.getMatcher(), is(sameInstance(matcher)));
        assertThat(event.getElapsedTimeInMS(), is(10L));
        assertThat(event.getRemainingTimeInMS(), is(5000L));
        assertThat(event.getAlias(), is("myAlias"));
        assertThat(event.getPollDelay(), is(Duration.ZERO));
    }

    @Test
    public void sixArgConstructorStoresExplicitPollDelay() {
        Matcher<String> matcher = equalTo("ok");
        Duration pollDelay = Duration.ofMillis(250);
        StartEvaluationEvent<String> event = new StartEvaluationEvent<>("Starting evaluation", matcher, 0L, 1000L, "alias", pollDelay);

        assertThat(event.getDescription(), is("Starting evaluation"));
        assertThat(event.getMatcher(), is(sameInstance(matcher)));
        assertThat(event.getElapsedTimeInMS(), is(0L));
        assertThat(event.getRemainingTimeInMS(), is(1000L));
        assertThat(event.getAlias(), is("alias"));
        assertThat(event.getPollDelay(), is(Duration.ofMillis(250)));
    }

    @Test
    public void sixArgConstructorSupportsZeroPollDelay() {
        StartEvaluationEvent<Integer> event = new StartEvaluationEvent<>("Starting evaluation", equalTo(1), 0L, 1000L, null, Duration.ZERO);

        assertThat(event.getPollDelay(), is(Duration.ZERO));
    }

    @Test
    public void sixArgConstructorKeepsANullPollDelay() {
        StartEvaluationEvent<Integer> event = new StartEvaluationEvent<>("Starting evaluation", equalTo(1), 0L, 1000L, null, null);

        assertThat(event.getPollDelay(), is(nullValue()));
    }

    @Test
    public void sixArgConstructorKeepsAForeverPollDelay() {
        StartEvaluationEvent<Integer> event = new StartEvaluationEvent<>("Starting evaluation", equalTo(1), 0L, 1000L, null, ForeverDuration.FOREVER);

        assertThat(event.getPollDelay(), is(ForeverDuration.FOREVER));
    }
}
