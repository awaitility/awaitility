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
import java.util.function.Supplier;

// Simulate sealed classes

public abstract class FailFastCondition {
    /**
     * The reason used when no reason is given (or the reason supplier returns <code>null</code>) for a callable fail fast condition.
     */
    static final String DEFAULT_FAILURE_REASON = "Fail fast condition triggered";

    private FailFastCondition() {
    }

    /**
     * Returns the failure reason. If the reason was given as a {@link Supplier} (see
     * {@link ConditionFactory#failFastWithReason(Supplier, Callable)}), the supplier is called each time this method is called,
     * and any exception it throws propagates to the caller.
     *
     * @return the failure reason. For a {@link CallableFailFastCondition} this is never <code>null</code> (the default reason is used
     * when no reason was given). For a {@link CallableFailFastCondition.FailFastAssertion} it is <code>null</code> when no reason was given,
     * and then the assertion's message is used.
     */
    public abstract String getFailFastFailureReason();

    public static final class CallableFailFastCondition extends FailFastCondition {

        private final Callable<Boolean> failFastCondition;
        private final Supplier<String> failFastFailureReason;

        public CallableFailFastCondition(String failFastFailureReason, Callable<Boolean> failFastCondition) {
            this(failFastCondition, failFastFailureReason == null ? null : () -> failFastFailureReason);
        }

        private CallableFailFastCondition(Callable<Boolean> failFastCondition, Supplier<String> failFastFailureReason) {
            this.failFastCondition = failFastCondition;
            this.failFastFailureReason = failFastFailureReason;
        }

        /**
         * Creates a fail fast condition whose reason is supplied lazily, i.e. only when the condition is triggered.
         */
        static CallableFailFastCondition withReasonSupplier(Supplier<String> failFastFailureReason, Callable<Boolean> failFastCondition) {
            return new CallableFailFastCondition(failFastCondition, failFastFailureReason);
        }

        public Callable<Boolean> getFailFastCondition() {
            return this.failFastCondition;
        }

        @Override
        public String getFailFastFailureReason() {
            String reason = failFastFailureReason == null ? null : failFastFailureReason.get();
            return reason == null ? DEFAULT_FAILURE_REASON : reason;
        }

        public static final class FailFastAssertion extends FailFastCondition {
            private final Supplier<String> failFastFailureReason;
            private final ThrowingRunnable failFastAssertion;

            public FailFastAssertion(String failFastFailureReason, ThrowingRunnable failFastAssertion) {
                this(failFastAssertion, failFastFailureReason == null ? null : () -> failFastFailureReason);
            }

            private FailFastAssertion(ThrowingRunnable failFastAssertion, Supplier<String> failFastFailureReason) {
                if (failFastAssertion == null) {
                    throw new IllegalArgumentException("failFastAssertion cannot be null");
                }
                this.failFastFailureReason = failFastFailureReason;
                this.failFastAssertion = failFastAssertion;
            }

            /**
             * Creates a fail fast assertion whose reason is supplied lazily, i.e. only when the assertion fails.
             */
            static FailFastAssertion withReasonSupplier(Supplier<String> failFastFailureReason, ThrowingRunnable failFastAssertion) {
                return new FailFastAssertion(failFastAssertion, failFastFailureReason);
            }

            public ThrowingRunnable getFailFastAssertion() {
                return failFastAssertion;
            }

            /**
             * @return the failure reason, or <code>null</code> if none was given (then the assertion's message is used)
             */
            @Override
            public String getFailFastFailureReason() {
                return failFastFailureReason == null ? null : failFastFailureReason.get();
            }
        }
    }
}
