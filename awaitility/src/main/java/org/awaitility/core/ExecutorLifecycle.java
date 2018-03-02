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
import java.util.concurrent.TimeUnit;

/**
 * Handles the lifecycle of an executor service.
 *
 * <b>This is an internal class, never use it directly!!</b>
 *
 * @since 3.1.0
 */
public class ExecutorLifecycle {

    private final Supplier<ExecutorService> executorServiceSupplier;
    private final EvaluationCleanup evaluationCleanup;

    private ExecutorLifecycle(Supplier<ExecutorService> executorServiceSupplier,
                              EvaluationCleanup evaluationCleanup) {
        this.executorServiceSupplier = executorServiceSupplier;
        this.evaluationCleanup = evaluationCleanup;
    }

    public static ExecutorLifecycle withoutCleanup(final ExecutorService executorService) {
        return withoutCleanup(new Supplier<ExecutorService>() {
            @Override
            public ExecutorService get() {
                return executorService;
            }
        });
    }

    public static ExecutorLifecycle withoutCleanup(Supplier<ExecutorService> executorServiceSupplier) {
        return new ExecutorLifecycle(executorServiceSupplier, noCleanup());
    }

    public static ExecutorLifecycle withNormalCleanupBehavior(Supplier<ExecutorService> executorServiceSupplier) {
        return new ExecutorLifecycle(executorServiceSupplier, normalCleanupBehavior());
    }

    public ExecutorService supplyExecutorService() {
        return executorServiceSupplier.get();
    }

    void executeNormalCleanupBehavior(ExecutorService executorService) {
        evaluationCleanup.executeNormalCleanupBehavior(executorService);
    }

    void executeUnexpectedCleanupBehavior(ExecutorService executorService) {
        evaluationCleanup.executeUnexpectedCleanupBehavior(executorService);
    }

    private static EvaluationCleanup noCleanup() {
        Consumer<ExecutorService> noop = new Consumer<ExecutorService>() {
            @Override
            public void accept(ExecutorService executorService) {
            }
        };
        return new EvaluationCleanup(noop, noop);
    }

    private static EvaluationCleanup normalCleanupBehavior() {
        // Clean up after a successful or unsuccessful attempt
        return new EvaluationCleanup(new Consumer<ExecutorService>() {
            @Override
            public void accept(ExecutorService executor) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                        executor.awaitTermination(1, TimeUnit.SECONDS);
                    }
                } catch (InterruptedException e) {
                    CheckedExceptionRethrower.safeRethrow(e);
                }
            }
        }, new Consumer<ExecutorService>() {
            @Override
            public void accept(ExecutorService executorService) {
                executorService.shutdownNow();
            }
        });
    }
}
