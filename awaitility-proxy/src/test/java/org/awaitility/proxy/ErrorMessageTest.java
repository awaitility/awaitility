/*
 * Copyright 2017 the original author or authors.
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
package org.awaitility.proxy;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.awaitility.proxy.AwaitilityClassProxy.to;
import static org.hamcrest.Matchers.equalTo;

public class ErrorMessageTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Ignore("For some reason this test fails on Jenkins")
    @Test public void
    proxying_final_methods_throws_exception_with_descriptive_message() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("No method call has been recorded. Perhaps the method was final?");

        AtomicBoolean wasAdded = new AtomicBoolean(false);

        await().untilCall( to(wasAdded).get(), equalTo(true));
    }
}
