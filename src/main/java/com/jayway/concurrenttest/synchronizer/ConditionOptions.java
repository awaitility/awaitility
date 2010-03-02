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
package com.jayway.concurrenttest.synchronizer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;

public class ConditionOptions {

    public static Duration withPollInterval(long time, TimeUnit unit) {
        return new Duration(time, unit);
    }

    public static Duration withPollInterval(Duration pollInterval) {
        if (pollInterval == null) {
            throw new IllegalArgumentException("pollInterval cannot be null");
        }
        return pollInterval;
    }

    public static Duration duration(long time, TimeUnit unit) {
        return new Duration(time, unit);
    }

    public static Duration atMost(Duration duration) {
        if (duration == null) {
            throw new IllegalArgumentException("duration cannot be null");
        }
        return duration;
    }

    public static Duration atMost(long time, TimeUnit unit) {
        return new Duration(time, unit);
    }

    public static Duration forever() {
        return Duration.FOREVER;
    }

    static class MethodCaller<T> implements Supplier<T> {
        private final Object target;
        private final Method method;
        private final Object[] args;

        public MethodCaller(Object target, Method method, Object[] args) {
            this.target = target;
            this.method = method;
            this.args = args;
        }

        @Override
        public T get() throws Exception {
            return (T) method.invoke(target, args);
        }
    }

    private static Object lastTarget;
    private static Method lastMethod;
    private static Object[] lastArgs;
   
    public static <S> S callTo(S service) {
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), service.getClass().getInterfaces(), new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                lastMethod = method;
                lastArgs = args;
                return TypeUtils.getDefaultValue(method.getReturnType());
            }

        });
        lastTarget = service;
        return (S) proxy;
    }

    public static <T> ConditionEvaluator until(T ignore, final Matcher<T> matcher) {
        return until(new MethodCaller<T>(lastTarget, lastMethod, lastArgs), matcher);
    }

    public static <T> ConditionEvaluator until(final Supplier<T> supplier, final Matcher<T> matcher) {
        if (supplier == null) {
            throw new IllegalArgumentException("You must specify a supplier (was null).");
        }
        if (matcher == null) {
            throw new IllegalArgumentException("You must specify a matcher (was null).");
        }
        return new ConditionEvaluator() {
            @Override
            public boolean evaluate() throws Exception {
                return matcher.matches(supplier.get());
            }
        };
    }

    public static ConditionEvaluator until(ConditionEvaluator condition) {
        return condition;
    }
}
