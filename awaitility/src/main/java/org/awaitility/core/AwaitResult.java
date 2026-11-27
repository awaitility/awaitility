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
package org.awaitility.core;

/**
 * The immutable result of a <code>untilOrTimeout</code> await statement, such as
 * {@link ConditionFactory#untilOrTimeout(java.util.concurrent.Callable, org.hamcrest.Matcher)}. Instead of throwing a
 * {@link ConditionTimeoutException} when the condition is not fulfilled in time, <code>untilOrTimeout</code> returns a failed result. E.g.
 * <p>&nbsp;</p>
 * <pre>
 * AwaitResult&lt;Integer&gt; result = await().atMost(Duration.ofSeconds(2)).untilOrTimeout(repository::count, is(greaterThan(2)));
 * if (!result.isSuccess()) {
 *     log.warn("Still only {} items: {}", result.getValue(), result.getCause().getMessage());
 * }
 * </pre>
 *
 * @param <T> the type of the awaited value
 * @since 4.4.0
 */
public final class AwaitResult<T> {

    private final T value;
    private final ConditionTimeoutException cause;

    private AwaitResult(T value, ConditionTimeoutException cause) {
        this.value = value;
        this.cause = cause;
    }

    static <T> AwaitResult<T> success(T value) {
        return new AwaitResult<>(value, null);
    }

    static <T> AwaitResult<T> failure(T lastValue, ConditionTimeoutException cause) {
        if (cause == null) {
            throw new IllegalArgumentException("A failed result must have a cause (was null).");
        }
        return new AwaitResult<>(lastValue, cause);
    }

    /**
     * @return <code>true</code> if the condition was fulfilled in time, <code>false</code> if the corresponding
     * <code>until</code> method would have thrown a {@link ConditionTimeoutException}.
     * @since 4.4.0
     */
    public boolean isSuccess() {
        return cause == null;
    }

    /**
     * Get the value that was observed by the await statement.
     * <ul>
     * <li>On success this is the value that fulfilled the condition, i.e. the same value that <code>until</code> returns.</li>
     * <li>On failure this is the <i>last observed</i> value. It did <b>not</b> fulfill the condition in time (or, if
     * <code>atLeast</code> was violated, it fulfilled the condition too early). It is <code>null</code> if the
     * condition was never evaluated to completion.</li>
     * <li>For <code>untilOrTimeout(Callable&lt;Boolean&gt;)</code> it is the last returned {@link Boolean}.</li>
     * <li>For <code>untilAssertedOrTimeout</code> it is always <code>null</code>.</li>
     * </ul>
     *
     * @return the observed value, may be <code>null</code>
     * @since 4.4.0
     */
    public T getValue() {
        return value;
    }

    /**
     * @return the {@link ConditionTimeoutException} that the corresponding <code>until</code> method would have thrown,
     * with the same message, or <code>null</code> on success.
     * @since 4.4.0
     */
    public ConditionTimeoutException getCause() {
        return cause;
    }

    /**
     * Get the value if the condition was fulfilled in time, otherwise throw the {@link ConditionTimeoutException}
     * returned by {@link #getCause()}. This makes it easy to inspect a result and then fail the same way as
     * <code>until</code> would have done.
     *
     * @return the value that fulfilled the condition
     * @throws ConditionTimeoutException if the condition was not fulfilled in time
     * @since 4.4.0
     */
    public T orElseThrow() {
        if (cause != null) {
            throw cause;
        }
        return value;
    }

    @Override
    public String toString() {
        return isSuccess() ? "AwaitResult[success, value=" + value + "]"
                : "AwaitResult[failure, lastValue=" + value + ", cause=" + cause.getMessage() + "]";
    }
}
