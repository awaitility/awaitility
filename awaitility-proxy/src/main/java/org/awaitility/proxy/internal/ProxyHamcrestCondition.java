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
package org.awaitility.proxy.internal;

import org.awaitility.core.AbstractHamcrestCondition;
import org.awaitility.core.ConditionSettings;
import org.hamcrest.Matcher;

import java.util.concurrent.Callable;

class ProxyHamcrestCondition<T> extends AbstractHamcrestCondition<T> {

    /**
     * <p>Constructor for ProxyHamcrestCondition.</p>
     *
     * @param supplier a {MethodCaller} object.
     * @param matcher a {@link org.hamcrest.Matcher} object.
     * @param settings a {@link org.awaitility.core.ConditionSettings} object.
     */
    ProxyHamcrestCondition(final MethodCaller<T> supplier, final Matcher<? super T> matcher, ConditionSettings settings) {
        super(supplier, matcher, settings);
    }

    @Override
    protected String getCallableDescription(Callable<T> supplier) {
        final MethodCaller<T> methodCaller = (MethodCaller<T>) supplier;
        return methodCaller.target.getClass().getName() + "." + methodCaller.method.getName() + "()";
    }
}
