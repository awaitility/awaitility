/*
 * Copyright 2010 PowerMock original author or authors.
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
 * This is an internal class and should NOT be used outside of Awaitility! Use it at own risk (please don't :)).
 */
public class CheckedExceptionRethrower {

    /**
     * <p>safeRethrow.</p>
     *
     * @param t   a {@link java.lang.Throwable} object.
     * @param <T> a T object.
     * @return a T object.
     */
    public static <T> T safeRethrow(Throwable t) {
        CheckedExceptionRethrower.<RuntimeException>safeRethrow0(t);
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void safeRethrow0(Throwable t) throws T {
        throw (T) t;
    }

}
