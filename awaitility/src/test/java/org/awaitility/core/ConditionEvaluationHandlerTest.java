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

import org.awaitility.constraint.AtMostWaitConstraint;
import org.awaitility.pollinterval.FixedPollInterval;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

public class ConditionEvaluationHandlerTest {

    @Test
    public void startPassesConfiguredPollDelayToListener() {
        AtomicReference<StartEvaluationEvent<Integer>> capturedEvent = new AtomicReference<>();
        ConditionEvaluationListener<Integer> listener = new ConditionEvaluationListener<Integer>() {
            @Override
            public void conditionEvaluated(EvaluatedCondition<Integer> condition) {
            }

            @Override
            public void beforeEvaluation(StartEvaluationEvent<Integer> event) {
                capturedEvent.set(event);
            }
        };
        Matcher<Integer> matcher = equalTo(7);
        ConditionSettings settings = new ConditionSettings(
                "customAlias",
                true,
                AtMostWaitConstraint.TEN_SECONDS,
                FixedPollInterval.fixed(Duration.ofMillis(100)),
                Duration.ofMillis(250),
                listener,
                throwable -> false,
                null,
                null,
                null,
                null,
                null
        );

        ConditionEvaluationHandler<Integer> handler = new ConditionEvaluationHandler<>(matcher, settings);
        handler.start();

        StartEvaluationEvent<Integer> event = capturedEvent.get();
        assertThat(event, is(notNullValue()));
        assertThat(event.getDescription(), is("Starting evaluation"));
        assertThat(event.getMatcher(), is(sameInstance(matcher)));
        assertThat(event.getElapsedTimeInMS(), is(0L));
        assertThat(event.getRemainingTimeInMS(), is(10000L));
        assertThat(event.getAlias(), is("customAlias"));
        assertThat(event.getPollDelay(), is(Duration.ofMillis(250)));
    }

    @Test
    public void startTreatsNullPollDelayAsZero() {
        AtomicReference<StartEvaluationEvent<Integer>> capturedEvent = new AtomicReference<>();
        ConditionEvaluationListener<Integer> listener = new ConditionEvaluationListener<Integer>() {
            @Override
            public void conditionEvaluated(EvaluatedCondition<Integer> condition) {
            }

            @Override
            public void beforeEvaluation(StartEvaluationEvent<Integer> event) {
                capturedEvent.set(event);
            }
        };
        ConditionSettings settings = new ConditionSettings(
                null,
                true,
                AtMostWaitConstraint.TEN_SECONDS,
                FixedPollInterval.fixed(Duration.ofMillis(100)),
                null,
                listener,
                throwable -> false,
                null,
                null,
                null,
                null,
                null
        );

        ConditionEvaluationHandler<Integer> handler = new ConditionEvaluationHandler<>(equalTo(1), settings);
        handler.start();

        StartEvaluationEvent<Integer> event = capturedEvent.get();
        assertThat(event, is(notNullValue()));
        assertThat(event.getPollDelay(), is(Duration.ZERO));
    }

    @Test
    public void startHandlesNullListenerSafely() {
        ConditionSettings settings = new ConditionSettings(
                null,
                true,
                AtMostWaitConstraint.TEN_SECONDS,
                FixedPollInterval.fixed(Duration.ofMillis(100)),
                Duration.ofMillis(50),
                null,
                throwable -> false,
                null,
                null,
                null,
                null,
                null
        );

        ConditionEvaluationHandler<Integer> handler = new ConditionEvaluationHandler<>(equalTo(1), settings);
        handler.start();
    }
}
