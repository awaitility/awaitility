/*
 * Copyright 2018 the original author or authors.
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

import java.util.concurrent.ExecutorService;

/**
 * Handles how threads and thread-pools are cleanup after each conditional evaluation round.
 * I.e. each call to <code>until</code>.
 *
 * @since 3.1.0
 */
class EvaluationCleanup {


    private final Consumer<ExecutorService> normalShutdownBehavior;
    private final Consumer<ExecutorService> unexpectedShutdownBehavior;

    /**
     * @param normalShutdownBehavior The cleanup behavior that is executed when an <code>until</code> method succeeds, fails or times out.
     * @param unexpectedShutdownBehavior The cleanup behavior that is executed for unexpected failures.
     *                                   Currently this means when exceptions are thrown from non-test threads ("uncaught exceptions")
     */
    public EvaluationCleanup(Consumer<ExecutorService> normalShutdownBehavior,
                             Consumer<ExecutorService> unexpectedShutdownBehavior) {

        this.normalShutdownBehavior = normalShutdownBehavior;
        this.unexpectedShutdownBehavior = unexpectedShutdownBehavior;
    }

    Consumer<ExecutorService> getNormalShutdownBehavior() {
        return normalShutdownBehavior;
    }

    Consumer<ExecutorService> getUnexpectedShutdownBehavior() {
        return unexpectedShutdownBehavior;
    }

    void executeNormalCleanupBehavior(ExecutorService executorService) {
        normalShutdownBehavior.accept(executorService);
    }

    void executeUnexpectedCleanupBehavior(ExecutorService executorService) {
        unexpectedShutdownBehavior.accept(executorService);
    }
}
