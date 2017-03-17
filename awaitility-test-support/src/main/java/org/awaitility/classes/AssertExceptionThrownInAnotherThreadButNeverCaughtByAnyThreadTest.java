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

package org.awaitility.classes;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

// TODO: 2017-03-17 Very ugly, consider refactoring
public abstract class AssertExceptionThrownInAnotherThreadButNeverCaughtByAnyThreadTest {
    @SuppressWarnings({"ThrowFromFinallyBlock", "finally"})
    public AssertExceptionThrownInAnotherThreadButNeverCaughtByAnyThreadTest() throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(byteArrayOutputStream, true, "UTF-8"));
        try {
            testLogic();
        } finally {
            String errorMessage = byteArrayOutputStream.toString("UTF-8");
            try {
                if (!errorMessage.contains("Illegal state!")) {
                    throw new IllegalArgumentException("Didn't contain expected string \"Illegal state!\"");
                }
            } finally {
                System.setErr(System.err);
            }
        }
    }

    public abstract void testLogic();
}