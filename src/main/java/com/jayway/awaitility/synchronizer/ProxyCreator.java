package com.jayway.awaitility.synchronizer;

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

public class ProxyCreator {

	public static Object create(Object target) {
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
		ProxyState.setLastTarget(target);
		return proxy;
	}

	private static Object createCGLibProxy(Class<? extends Object> targetClass) {
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

		// Instantiate the proxied class
		Objenesis objenesis = new ObjenesisStd();
		proxy = objenesis.newInstance(proxiedClass);
		return proxy;
	}

	private static Object createInterfaceProxy(Class<?> targetClass) {
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
				getInterfaceHierarchy(targetClass), new InvocationHandler() {
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						return callReceived(method, args);
					}
				});
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

	private static Object callReceived(Method method, Object[] args) {
		ProxyState.setLastMethod(method);
		ProxyState.setLastArgs(args);
		return TypeUtils.getDefaultValue(method.getReturnType());
	}
}
