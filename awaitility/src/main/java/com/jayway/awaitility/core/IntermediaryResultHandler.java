/*
 * Copyright 2010 the original author or authors.
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
package com.jayway.awaitility.core;

/**
 * Handle intermediary result each time evaluation of a condition fails.
 */
public interface IntermediaryResultHandler {

    /**
     * @param mismatchMessage   message returned when Hamcrest matcher fails.
     * @param elapsedTimeInMS   elapsed time in milliseconds.
     * @param remainingTimeInMS remaining time to wait in milliseconds; <code>null</code>, if no timeout defined, i.e., running forever.
     */
    void handle(String mismatchMessage, long elapsedTimeInMS, Long remainingTimeInMS);
}
