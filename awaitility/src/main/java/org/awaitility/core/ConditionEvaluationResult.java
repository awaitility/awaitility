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
package org.awaitility.core;

class ConditionEvaluationResult {
    private final Throwable trace;
    private final Throwable throwable;
    private final boolean successful;

    /**
     * Return the result of the condition evaluation as either just <code>true</code> or <code>false</code>.
     * If <code>false</code> this means that the condition could be retried later.
     *
     * @param successful <code>true</code> or <code>false</code>
     */
    ConditionEvaluationResult(boolean successful) {
        this(successful, null, null);
    }

    /**
     * Fail the condition evaluation due to an exception with an optional trace.
     * The purpose of the trace is to identify where in the code (which line) that
     * contained the condition that failed. Note that a trace is independent of the <code>throwable</code>
     * (i.e. the condition must necessarily have failed with an exception)
     * @param throwable The exception that caused the condition to fail
     */
    ConditionEvaluationResult(boolean successful, Throwable throwable, Throwable trace) {
        this.successful = successful;
        this.throwable = throwable;
        this.trace = trace;
    }

    boolean isSuccessful() {
        return successful;
    }

    boolean isError() {
        return !isSuccessful();
    }

    Throwable getThrowable() {
        return throwable;
    }

    Throwable getTrace() {
        return trace;
    }

    boolean hasThrowable() {
        return throwable != null;
    }

    boolean hasTrace() {
        return trace != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConditionEvaluationResult)) return false;

        ConditionEvaluationResult result = (ConditionEvaluationResult) o;

        if (successful != result.successful) return false;
        return throwable != null ? throwable.equals(result.throwable) : result.throwable == null;
    }

    @Override
    public int hashCode() {
        int result = throwable != null ? throwable.hashCode() : 0;
        result = 31 * result + (successful ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConditionEvaluationResult{" +
                "trace=" + trace +
                ", throwable=" + throwable +
                ", successful=" + successful +
                '}';
    }
}
