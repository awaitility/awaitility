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

import org.awaitility.core.Condition;
import org.awaitility.core.ConditionSettings;
import org.awaitility.spi.ProxyConditionFactory;
import org.hamcrest.Matcher;

public class ByteBuddyProxyConditionFactory<T> implements ProxyConditionFactory<T> {

    @Override
    public Condition<T> createProxyCondition(T __, Matcher<? super T> matcher, ConditionSettings settings) {
        final MethodCaller<T> supplier = new MethodCaller<T>(MethodCallRecorder.getLastTarget(), MethodCallRecorder
                .getLastMethod(), MethodCallRecorder.getLastArgs());
        MethodCallRecorder.reset();
        return new ProxyHamcrestCondition<T>(supplier, matcher, settings);
    }
}
