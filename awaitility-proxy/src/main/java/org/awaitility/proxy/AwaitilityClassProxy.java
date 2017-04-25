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

package org.awaitility.proxy;

import org.awaitility.proxy.internal.MethodCallRecorder;

public class AwaitilityClassProxy {

    /**
     * Await until a specific method invocation returns something. E.g.
     * <p>
     * <pre>
     * await().until(callTo(service).getCount(), greaterThan(2));
     * </pre>
     * <p>
     * Here we tell Awaitility to wait until the <code>service.getCount()</code>
     * method returns a value that is greater than 2.
     *
     * @param <S>    The type of the service.
     * @param object the object that contains the method of interest.
     * @return A proxy of the service
     */
    @SuppressWarnings("unchecked")
    public static <S> S to(S object) {
        return (S) MethodCallRecorder.createProxy(object);
    }
}
