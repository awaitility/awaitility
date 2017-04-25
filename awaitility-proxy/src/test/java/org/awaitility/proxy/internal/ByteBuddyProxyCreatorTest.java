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

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.awaitility.classes.ClassWithMethods;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class ByteBuddyProxyCreatorTest {

    @Test
    public void interceptsStandardMethodCalls() {
        ClassWithMethods object = (ClassWithMethods) ByteBuddyProxyCreator.create(ClassWithMethods.class, TestMethodExecutor.class);

        assertEquals("test aMethod", object.aMethod());
    }

    @Test
    public void interceptsReflectionMethodCalls() throws Exception {
        ClassWithMethods object = (ClassWithMethods) ByteBuddyProxyCreator.create(ClassWithMethods.class, TestMethodExecutor.class);

        Method method = object.getClass().getMethod("aMethod");
        assertEquals("test aMethod", method.invoke(object));
    }

    @SuppressWarnings("FinalizeCalledExplicitly")
    @Test
    public void ignoreFinalizeCalls() {
        ClassWithMethods object = (ClassWithMethods) ByteBuddyProxyCreator.create(ClassWithMethods.class, TestFinalizeMethodExecutor.class);
        assertEquals(TestFinalizeMethodExecutor.finalizeCallCounter, 0);

        object.finalize();

        assertEquals(TestFinalizeMethodExecutor.finalizeCallCounter, 0);
    }

    public static class TestMethodExecutor {
        @RuntimeType
        public static Object interceptExecutionDetails(@Origin Method method, @AllArguments Object[] args) {
            return "test " + method.getName();
        }
    }

    public static class TestFinalizeMethodExecutor {
        //Not thread-safe - dirty hack to test intercepting finalize method call
        private static int finalizeCallCounter;

        @RuntimeType
        public static Object interceptExecutionDetails(@Origin Method method, @AllArguments Object[] args) {
            finalizeCallCounter++;
            return null;
        }
    }
}
