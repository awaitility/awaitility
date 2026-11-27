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

package org.awaitility;

import org.awaitility.core.AwaitResult;
import org.awaitility.core.ConditionFactory;
import org.awaitility.core.ConditionTimeoutException;
import org.awaitility.core.TimeoutEvent;
import org.junit.After;
import org.junit.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

/**
 * Tests that timeoutMessage(..), onTimeout(..), during(..) and untilOrTimeout(..) work together. Each of them is
 * tested on its own elsewhere; these tests cover the places where they meet in ConditionAwaiter.
 */
public class CombinedTimeoutFeaturesTest {

    @After
    public void reset_awaitility_after_each_test() {
        Awaitility.reset();
    }

    private static ConditionFactory shortAwait() {
        return await().pollInterval(Duration.ofMillis(10)).atMost(Duration.ofMillis(100));
    }

    @Test(timeout = 2000)
    public void on_timeout_event_and_exception_include_the_custom_timeout_message() {
        List<TimeoutEvent> events = new CopyOnWriteArrayList<>();

        try {
            shortAwait().timeoutMessage(() -> "custom text").onTimeout(events::add).until(() -> false);
            fail("Expected ConditionTimeoutException");
        } catch (ConditionTimeoutException e) {
            assertThat(e.getMessage(), endsWith("\ncustom text"));
            assertThat(events, hasSize(1));
            assertThat(events.get(0).getDescription(), equalTo(e.getMessage()));
        }
    }

    @Test(timeout = 2000)
    public void on_timeout_callback_failure_and_timeout_message_supplier_failure_are_both_suppressed() {
        IllegalStateException supplierFailure = new IllegalStateException("supplier");
        IllegalStateException callbackFailure = new IllegalStateException("callback");

        try {
            shortAwait()
                    .timeoutMessage(() -> {
                        throw supplierFailure;
                    })
                    .onTimeout(event -> {
                        throw callbackFailure;
                    })
                    .until(() -> false);
            fail("Expected ConditionTimeoutException");
        } catch (ConditionTimeoutException e) {
            assertThat(e.getSuppressed(), arrayContaining(supplierFailure, callbackFailure));
        }
    }

    @Test(timeout = 2000)
    public void until_or_timeout_returns_a_cause_with_the_custom_timeout_message_and_calls_on_timeout() {
        List<TimeoutEvent> events = new CopyOnWriteArrayList<>();

        AwaitResult<Boolean> result = shortAwait().timeoutMessage(() -> "custom text").onTimeout(events::add)
                .untilOrTimeout(() -> false);

        assertThat(result.isSuccess(), is(false));
        assertThat(result.getCause().getMessage(), endsWith("\ncustom text"));
        assertThat(events, hasSize(1));
    }

    @Test(timeout = 2000)
    public void unsatisfiable_during_keeps_its_message_and_gets_the_custom_timeout_message_and_on_timeout() {
        List<TimeoutEvent> events = new CopyOnWriteArrayList<>();

        AwaitResult<Boolean> result = shortAwait().during(Duration.ofMillis(200)).timeoutMessage(() -> "custom text")
                .onTimeout(events::add).untilOrTimeout(() -> true);

        assertThat(result.isSuccess(), is(false));
        assertThat(result.getCause().getMessage(), allOf(containsString("can never be fulfilled"), endsWith("\ncustom text")));
        assertThat(events, hasSize(1));
    }
}
