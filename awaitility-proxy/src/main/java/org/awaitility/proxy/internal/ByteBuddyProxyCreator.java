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

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import org.awaitility.proxy.CannotCreateProxyException;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.bytebuddy.matcher.ElementMatchers.isFinalizer;
import static net.bytebuddy.matcher.ElementMatchers.not;

/**
 * Creates proxy instance using ByteBuddy.
 *
 * @author Marcin Zajączkowski
 * @since 2.1.0
 */
class ByteBuddyProxyCreator {

    public static Object create(Class<?> targetClass, Class<?> interceptorClass) {
        Class<?> effectiveTargetClass = targetClass;
        Set<Class<?>> interfacesToImplement = new HashSet<Class<?>>();

        if (Modifier.isFinal(targetClass.getModifiers())) {
            if (targetClassHasInterfaces(targetClass)) {
                effectiveTargetClass = Object.class;
                interfacesToImplement = getInterfaceHierarchy(targetClass);
            } else {
                throw new CannotCreateProxyException(String.format(
                        "Cannot create a proxy for class '%s' because it is final and doesn't implement any interfaces.",
                        targetClass.getName()));
            }
        }

        return crateForParameters(effectiveTargetClass, interfacesToImplement, targetClass.getClassLoader(), interceptorClass);
    }

    private static Object crateForParameters(Class<?> effectiveTargetClass, Set<Class<?>> interfacesToImplement, ClassLoader classLoader,
                                             Class<?> interceptorClass) {
        List<Type> interfacesToImplementAsList = new ArrayList<Type>(interfacesToImplement);
        Class<?> proxyType = new ByteBuddy()
                .subclass(effectiveTargetClass)
                .implement(interfacesToImplementAsList)
                .method(not(isFinalizer()))
                .intercept(MethodDelegation.to(interceptorClass))
                .make()
                .load(classLoader)
                .getLoaded();
        return instantiate(proxyType);
    }

    private static Object instantiate(Class<?> dynamicType) {
        try {
            return dynamicType.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean targetClassHasInterfaces(Class<?> targetClass) {
        return getInterfaceHierarchy(targetClass).size() > 0;
    }

    private static Set<Class<?>> getInterfaceHierarchy(Class<?> targetClass) {
        Set<Class<?>> interfaces = new HashSet<Class<?>>();
        if (targetClass == null || targetClass.equals(Object.class)) {
            return interfaces;
        }
        interfaces.addAll(Arrays.asList(targetClass.getInterfaces()));
        interfaces.addAll(getInterfaceHierarchy(targetClass.getSuperclass()));
        return interfaces;
    }
}
