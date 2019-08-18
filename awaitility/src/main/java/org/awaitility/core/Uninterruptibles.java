/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.awaitility.core;

import java.time.Duration;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * These code snippets are copied from the Guava library (e.g. <a href="https://github.com/google/guava/blob/bf9e8fa954bd76fd6642445fa644c729f91f30f2/guava/src/com/google/common/util/concurrent/Uninterruptibles.java#L382-L402">Uninterruptibles#sleepUninterruptibly</a>)
 * to solve <a href="https://github.com/awaitility/awaitility/issues/134">issue 134</a>.
 */
class Uninterruptibles {

    static void sleepUninterruptibly(long sleepFor, TimeUnit unit) {
        boolean interrupted = false;
        try {
            long remainingNanos = unit.toNanos(sleepFor);
            long end = System.nanoTime() + remainingNanos;
            while (true) {
                try {
                    // TimeUnit.sleep() treats negative timeouts just like zero.
                    NANOSECONDS.sleep(remainingNanos);
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                    remainingNanos = end - System.nanoTime();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Invokes {@code future.}{@link Future#get() get()} uninterruptibly.
     *
     * @throws ExecutionException if the computation threw an exception
     * @throws CancellationException if the computation was cancelled
     */
    static <V> V getUninterruptibly(Future<V> future) throws ExecutionException {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    return future.get();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Invokes {@code future.}{@link Future#get(long, TimeUnit) get(timeout, unit)} uninterruptibly.
     *
     * @throws ExecutionException if the computation threw an exception
     * @throws CancellationException if the computation was cancelled
     * @throws TimeoutException if the wait timed out
     */
    static <V> V getUninterruptibly(Future<V> future, Duration timeout)
            throws ExecutionException, TimeoutException {
        return getUninterruptibly(future, saturatedToNanos(timeout), TimeUnit.NANOSECONDS);
    }

    /**
     * Invokes {@code future.}{@link Future#get(long, TimeUnit) get(timeout, unit)} uninterruptibly.
     *
     * @throws ExecutionException if the computation threw an exception
     * @throws CancellationException if the computation was cancelled
     * @throws TimeoutException if the wait timed out
     */
    static <V> V getUninterruptibly(Future<V> future, long timeout, TimeUnit unit)
            throws ExecutionException, TimeoutException {
        boolean interrupted = false;
        try {
            long remainingNanos = unit.toNanos(timeout);
            long end = System.nanoTime() + remainingNanos;

            while (true) {
                try {
                    // Future treats negative timeouts just like zero.
                    return future.get(remainingNanos, NANOSECONDS);
                } catch (InterruptedException e) {
                    interrupted = true;
                    remainingNanos = end - System.nanoTime();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Shuts down an executor service uninterruptibly
     * Note that this method is created by Johan Haleby and is thus not covered by the Guava license
     *
     * @param executor The executor service to shutdown
     * @param timeout The timeout amount
     * @param unit The time unit
     */
    static void shutdownUninterruptibly(ExecutorService executor, long timeout, TimeUnit unit) {
        boolean interrupted = false;
        try {
            long remainingNanos = unit.toNanos(timeout);
            long end = System.nanoTime() + remainingNanos;
            executor.shutdown();

            while (true) {
                try {
                    if (!executor.awaitTermination(remainingNanos, unit)) {
                        executor.shutdownNow();
                    }
                    break;
                } catch (InterruptedException e) {
                    interrupted = true;
                    remainingNanos = end - System.nanoTime();
                    executor.shutdownNow();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Returns the number of nanoseconds of the given duration without throwing or overflowing.
     *
     * <p>Instead of throwing {@link ArithmeticException}, this method silently saturates to either
     * {@link Long#MAX_VALUE} or {@link Long#MIN_VALUE}. This behavior can be useful when decomposing
     * a duration in order to call a legacy API which requires a {@code long, TimeUnit} pair.
     */
    private static long saturatedToNanos(Duration duration) {
        // Using a try/catch seems lazy, but the catch block will rarely get invoked (except for
        // durations longer than approximately +/- 292 years).
        try {
            return duration.toNanos();
        } catch (ArithmeticException tooBig) {
            return duration.isNegative() ? Long.MIN_VALUE : Long.MAX_VALUE;
        }
    }
}