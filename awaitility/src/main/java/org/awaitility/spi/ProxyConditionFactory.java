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

package org.awaitility.spi;

import org.awaitility.core.Condition;
import org.awaitility.core.ConditionSettings;
import org.hamcrest.Matcher;

/**
 * An SPI that can be used to implement proxy conditions. Typical use case:
 * <pre>
 * await().untilCall( to(someObject).someMethod(), is(4) );
 * </pre>
 *
 * where "to" is a static method defined by the provider that generates the proxy instance.
 *
 * @param <T> The type of the condition
 */
public interface ProxyConditionFactory<T> {

    /**
     * Create a proxy condition
     *
     * @param proxyMethodReturnValue The return value of the proxy method invocation
     * @param matcher The hamcrest matcher
     * @param settings Condition settings
     * @return A condition
     */
    Condition<T> createProxyCondition(T proxyMethodReturnValue, final Matcher<? super T> matcher, ConditionSettings settings);

}
