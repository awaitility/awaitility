/*
 * Copyright 2022 the original author or authors.
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

import java.util.concurrent.Callable;

// Simulate sealed classes

public abstract class FailFastCondition {
    private FailFastCondition() {
    }

    public abstract String getFailFastFailureReason();

    public static final class CallableFailFastCondition extends FailFastCondition {
        private static final String DEFAULT_FAILURE_REASON = "Fail fast condition triggered";

        private final Callable<Boolean> failFastCondition;
        private final String failFastFailureReason;

        public CallableFailFastCondition(String failFastFailureReason, Callable<Boolean> failFastCondition) {
            this.failFastCondition = failFastCondition;
            this.failFastFailureReason = failFastFailureReason == null ? DEFAULT_FAILURE_REASON : failFastFailureReason;
        }

        public Callable<Boolean> getFailFastCondition() {
            return this.failFastCondition;
        }

        @Override
        public String getFailFastFailureReason() {
            return this.failFastFailureReason;
        }

        public static final class FailFastAssertion extends FailFastCondition {
            private final String failFastFailureReason;
            private final ThrowingRunnable failFastAssertion;

            public FailFastAssertion(String failFastFailureReason, ThrowingRunnable failFastAssertion) {
                if (failFastAssertion == null) {
                    throw new IllegalArgumentException("failFastAssertion cannot be null");
                }
                this.failFastFailureReason = failFastFailureReason;
                this.failFastAssertion = failFastAssertion;
            }

            public ThrowingRunnable getFailFastAssertion() {
                return failFastAssertion;
            }

            @Override
            public String getFailFastFailureReason() {
                return failFastFailureReason;
            }
        }
    }
}