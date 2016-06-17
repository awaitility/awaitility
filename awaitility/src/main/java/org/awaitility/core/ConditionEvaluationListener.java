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
package org.awaitility.core;

/**
 * A ConditionEvaluationListener is called each time a condition has been evaluated by Awaitility.
 *
 * @param <T> The expected return type of the condition
 */
public interface ConditionEvaluationListener<T> {

    /**
     * Handle an evaluated condition of a matcher.
     *
     * @param condition The condition evaluation result containing various properties of the result of evaluated condition
     */
    void conditionEvaluated(EvaluatedCondition<T> condition);
}
