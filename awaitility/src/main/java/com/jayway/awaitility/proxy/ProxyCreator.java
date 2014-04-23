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

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>ProxyCreator class.</p>
 *
 * @author johan
 * @version $Id: $Id
 */
public final class ProxyCreator {
	
	private ProxyCreator() {}
	
	/**
	 * <p>create.</p>
	 *
	 * @param targetClass a {@link java.lang.Class} object.
	 * @param invocationHandler a {@link java.lang.reflect.InvocationHandler} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public static Object create(Class<? extends Object> targetClass, InvocationHandler invocationHandler) {
		Object proxy = null;
		if (Modifier.isFinal(targetClass.getModifiers())) {
			if (targetClassHasInterfaces(targetClass)) {
				proxy = createInterfaceProxy(targetClass, invocationHandler);
			} else {
				throw new CannotCreateProxyException(
						String
								.format(
										"Cannot create a proxy for class '%s' because it is final and doesn't implement any interfaces.",
										targetClass.getName()));
			}
		} else {
			proxy = createCGLibProxy(targetClass, invocationHandler);
		}
		return proxy;
	}

	private static Object createCGLibProxy(Class<? extends Object> targetClass, final InvocationHandler invocationHandler) {
		Object proxy;
		// Create CGLib Method interceptor
		MethodInterceptor interceptor = new MethodInterceptor() {
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				return invocationHandler.invoke(obj, method, args);
			}
		};

		// Create the proxy
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(targetClass);
		enhancer.setCallbackType(interceptor.getClass());
        Class<?> proxiedClass = enhancer.createClass();
        Enhancer.registerCallbacks(proxiedClass, new Callback[] { interceptor });
        /* To make the proxy creator work with Eclipse plugins */
		enhancer.setClassLoader(ProxyCreator.class.getClassLoader());

		// Instantiate the proxied class
		Objenesis objenesis = new ObjenesisStd();
		proxy = objenesis.newInstance(proxiedClass);
		return proxy;
	}

	private static Object createInterfaceProxy(Class<?> targetClass, InvocationHandler invocationHandler) {
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
				getInterfaceHierarchy(targetClass), invocationHandler);
	}

	private static boolean targetClassHasInterfaces(Class<? extends Object> targetClass) {
		Class<?>[] interfaces = getInterfaceHierarchy(targetClass);
		return interfaces != null && interfaces.length >= 1;
	}

	private static Class<?>[] getInterfaceHierarchy(Class<? extends Object> targetClass) {
		if (targetClass == null || targetClass.equals(Object.class)) {
			return new Class<?>[0];
		}
		Set<Class<?>> interfaces = new HashSet<Class<?>>();
		interfaces.addAll(Arrays.asList(((Class<?>) targetClass).getInterfaces()));
		interfaces.addAll(Arrays.asList(getInterfaceHierarchy(((Class<?>) targetClass).getSuperclass())));
		return interfaces.toArray(new Class<?>[interfaces.size()]);
	}
}
