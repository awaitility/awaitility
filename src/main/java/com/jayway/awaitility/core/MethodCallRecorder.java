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

import java.lang.reflect.Method;

import com.jayway.awaitility.proxy.ProxyCreator;
import com.jayway.awaitility.proxy.TypeUtils;

public class MethodCallRecorder {
	
	private static Object lastTarget;
    private static Method lastMethod;
    private static Object[] lastArgs;
    
    private static ProxyCreator proxyCreator = new ProxyCreator() {
		@Override
		protected Object callReceived(Method method, Object[] args) {
			lastMethod = method;
			lastArgs = args;
			return TypeUtils.getDefaultValue(method.getReturnType());
		}
    };

	public static Object createProxy(Object target) {
		Object proxy = proxyCreator.create(target);
		lastTarget = target;
		return proxy;
	}

	public static Object getLastTarget() {
		return lastTarget;
	}
	public static Method getLastMethod() {
		return lastMethod;
	}
	public static Object[] getLastArgs() {
		return lastArgs;
	}
	
}
