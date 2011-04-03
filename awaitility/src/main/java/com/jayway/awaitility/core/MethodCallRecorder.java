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
package com.jayway.awaitility.core;

import com.jayway.awaitility.proxy.ProxyCreator;
import com.jayway.awaitility.proxy.TypeUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MethodCallRecorder {
	
	private static Object lastTarget;
    private static Method lastMethod;
    private static Object[] lastArgs;
    
    private static InvocationHandler invocationHandler = new InvocationHandler() {
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (shouldBeRecorded(method)) {
				lastMethod = method;
				lastArgs = args;
			}
			return TypeUtils.getDefaultValue(method.getReturnType());
		}

		private boolean shouldBeRecorded(Method method) {
			return !(method.getDeclaringClass().equals(Object.class) && method.getName().equals("finalize"));
		}

    };

	public static Object createProxy(Object target) {
		Object proxy = ProxyCreator.create(target.getClass(), invocationHandler);
		lastTarget = target;
		return proxy;
	}

	public static Object getLastTarget() {
		if (lastTarget == null) {
			throw new IllegalStateException("No method call have been recorded!");
		}
		return lastTarget;
	}
	
	public static Method getLastMethod() {
		if (lastMethod == null) {
			throw new IllegalStateException("No method call have been recorded!");
		}
		return lastMethod;
	}
	
	public static Object[] getLastArgs() {
		if (lastTarget == null) {
			throw new IllegalStateException("No method call have been recorded!");
		}
		return lastArgs;
	}

	public static void reset() {
		lastTarget = null;
		lastMethod = null;
		lastArgs = null;
	}
	
}
