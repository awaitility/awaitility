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
package com.jayway.awaitility.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

public abstract class ProxyCreator {

	public Object create(Object target) {
		Object proxy = null;
		Class<? extends Object> targetClass = target.getClass();
		if (Modifier.isFinal(targetClass.getModifiers())) {
			if (targetClassHasInterfaces(targetClass)) {
				// TODO Issue warning?
				proxy = createInterfaceProxy(targetClass);
			} else {
				throw new CannotCreateProxyException(
						String
								.format(
										"Cannot create a proxy for class '%s' because it is final and doesn't implement any interfaces.",
										targetClass.getName()));
			}
		} else {
			proxy = createCGLibProxy(targetClass);
		}
		return proxy;
	}

	private Object createCGLibProxy(Class<? extends Object> targetClass) {
		Object proxy;
		// Create CGLib Method interceptor
		MethodInterceptor interceptor = new MethodInterceptor() {
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				return callReceived(method, args);
			}
		};

		// Create the proxy
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(targetClass);
		enhancer.setCallbackType(interceptor.getClass());
		Class<?> proxiedClass = enhancer.createClass();
		Enhancer.registerCallbacks(proxiedClass, new Callback[] { interceptor });
		// FIXME: Set correct classloader to work with OSGi

		// Instantiate the proxied class
		Objenesis objenesis = new ObjenesisStd();
		proxy = objenesis.newInstance(proxiedClass);
		return proxy;
	}

	private Object createInterfaceProxy(Class<?> targetClass) {
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
				getInterfaceHierarchy(targetClass), new InvocationHandler() {
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						return callReceived(method, args);
					}
				});
	}

	private boolean targetClassHasInterfaces(Class<? extends Object> targetClass) {
		Class<?>[] interfaces = getInterfaceHierarchy(targetClass);
		return interfaces != null && interfaces.length >= 1;
	}

	private Class<?>[] getInterfaceHierarchy(Class<? extends Object> targetClass) {
		if (targetClass == null || targetClass.equals(Object.class)) {
			return new Class<?>[0];
		}
		Set<Class<?>> interfaces = new HashSet<Class<?>>();
		interfaces.addAll(Arrays.asList(((Class<?>) targetClass).getInterfaces()));
		interfaces.addAll(Arrays.asList(getInterfaceHierarchy(((Class<?>) targetClass).getSuperclass())));
		return interfaces.toArray(new Class<?>[interfaces.size()]);
	}

	protected abstract Object callReceived(Method method, Object[] args);
}
