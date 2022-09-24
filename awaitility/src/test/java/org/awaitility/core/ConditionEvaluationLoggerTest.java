/*
 * Copyright 2020 the original author or authors.
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

import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.ONE_HUNDRED_MILLISECONDS;
import static org.awaitility.core.ConditionEvaluationLogger.conditionEvaluationLogger;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ConditionEvaluationLoggerTest {

    private final PrintStream standardOut = System.out;

    @After
    @SuppressWarnings("unused")
    public void resetDefaultSystemOut() {
        System.setOut(standardOut);
        Awaitility.reset();
    }

    /**
     * Test should check that await.logging(Consumer) properly logging data to Consumer
     */
    @Test(timeout = 2000)
    public void it_is_possible_to_use_logging_with_syntactic_sugar_via_condition_evaluation_logger_by_consumer() {
        CopyOnWriteArrayList<String> logs = new CopyOnWriteArrayList<>();

        await().with()
                .logging(logs::add)
                .pollInterval(ONE_HUNDRED_MILLISECONDS)
                .until(logs::size, is(4));

        assertThat(
                logs,
                everyItem(
                        anyOf(
                                equalTo("Starting evaluation"),
                                containsString("expected <4> but was"),
                                containsString("reached its end value of <4>")
                        )
                )
        );
    }

    /**
     * Test should check that Awaitility.setLoggingListener(new ConditionEvaluationLogger(Consumer)) properly logging data to Consumer
     */
    @Test(timeout = 2000)
    public void it_is_possible_to_use_logging_with_Awaitlity_static_settings_via_condition_evaluation_logger_by_consumer() {
        CopyOnWriteArrayList<String> logs = new CopyOnWriteArrayList<>();
        Awaitility.setLoggingListener(new ConditionEvaluationLogger(logs::add));

        await().with()
                .pollInterval(ONE_HUNDRED_MILLISECONDS)
                .until(logs::size, is(4));

        assertThat(
                logs,
                everyItem(
                        anyOf(
                                equalTo("Starting evaluation"),
                                containsString("expected <4> but was"),
                                containsString("reached its end value of <4>")
                        )
                )
        );
    }

    /**
     * Test should check that Awaitility.setLogging(new ConditionEvaluationLogger(Consumer)) properly logging data to Consumer
     */
    @Test(timeout = 2000)
    public void it_is_possible_to_use_logging_with_Awaitlity_static_settings_via_consumer() {
        CopyOnWriteArrayList<String> logs = new CopyOnWriteArrayList<>();
        Awaitility.setLogging(logs::add);

        await().with()
                .pollInterval(ONE_HUNDRED_MILLISECONDS)
                .until(logs::size, is(4));

        assertThat(
                logs,
                everyItem(
                        anyOf(
                                equalTo("Starting evaluation"),
                                containsString("expected <4> but was"),
                                containsString("reached its end value of <4>")
                        )
                )
        );
    }

    /**
     * Test should check that await.logging() properly logging data to System.out
     */
    @Test(timeout = 2000)
    public void it_is_possible_to_use_sout_logging_with_syntactic_sugar_via_condition_evaluation_logger() {
        final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
        final AtomicInteger stub = new AtomicInteger(0);

        await().with()
                .logging()
                .pollInterval(ONE_HUNDRED_MILLISECONDS)
                .until(stub::getAndIncrement, is(4));

        assertThat(
                Arrays.asList(outputStreamCaptor.toString().split("\n")),
                everyItem(
                        anyOf(
                                equalTo("Starting evaluation\r"),
                                containsString("expected <4> but was"),
                                containsString("reached its end value of <4>")
                        )
                )
        );
    }

    /**
     * Test should check that Awaitility.setDefaultLogging() properly logging data to System.out
     */
    @Test(timeout = 2000)
    public void it_is_possible_to_use_sout_logging_with_Awaitility_static_settings_via_condition_evaluation_logger() {
        Awaitility.setDefaultLogging();

        final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
        final AtomicInteger stub = new AtomicInteger(0);

        await().with()
                .pollInterval(ONE_HUNDRED_MILLISECONDS)
                .until(stub::getAndIncrement, is(4));

        assertThat(
                Arrays.asList(outputStreamCaptor.toString().split("\n")),
                everyItem(
                        anyOf(
                                equalTo("Starting evaluation\r"),
                                containsString("expected <4> but was"),
                                containsString("reached its end value of <4>")
                        )
                )
        );
    }

    @Test(timeout = 2000)
    public void it_is_possible_to_override_the_way_condition_evaluation_logger_logs_the_results_when_using_ctor_to_create_the_condition_evaluation_logger() {
        CopyOnWriteArrayList<String> logs = new CopyOnWriteArrayList<>();

        await().with().conditionEvaluationListener(new ConditionEvaluationLogger(logs::add)).
                pollInterval(ONE_HUNDRED_MILLISECONDS).
                until(logs::size, is(4));

        assertThat(logs, everyItem(anyOf(equalTo("Starting evaluation"), containsString("expected <4> but was"), containsString("reached its end value of <4>"))));
    }
    
    @Test(timeout = 2000)
    public void it_is_possible_to_override_the_way_condition_evaluation_logger_logs_the_results_when_using_static_method_to_create_the_condition_evaluation_logger() {
        CopyOnWriteArrayList<String> logs = new CopyOnWriteArrayList<>();

        await().with().conditionEvaluationListener(conditionEvaluationLogger(logs::add)).
                pollInterval(ONE_HUNDRED_MILLISECONDS).
                until(logs::size, is(4));

        assertThat(logs, everyItem(anyOf(equalTo("Starting evaluation"), containsString("expected <4> but was"), containsString("reached its end value of <4>"))));
    }
}