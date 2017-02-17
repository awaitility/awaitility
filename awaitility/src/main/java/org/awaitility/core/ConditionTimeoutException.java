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

package org.awaitility.core;

/**
 * A runtime exception thrown by Awaitility when a condition was not fulfilled within the specified threshold.
 *
 */
public class ConditionTimeoutException extends RuntimeException {

    /**
     * <p>Constructor for ConditionTimeoutException.</p>
     *
     * @param message A description of why the timeout occurred.
     */
    public ConditionTimeoutException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for ConditionTimeoutException.</p>
     *
     * @param message A description of why the timeout occurred.
     * @param throwable The cause
     */
    public ConditionTimeoutException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
