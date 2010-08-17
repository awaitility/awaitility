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
import java.util.concurrent.Callable;

import org.hamcrest.Matcher;

class CallableHamcrestCondition<T> extends AbstractHamcrestCondition<T> {

	public CallableHamcrestCondition(final Callable<T> supplier, final Matcher<T> matcher, ConditionSettings settings) {
		super(supplier, matcher, settings);
	}

	@Override
	String getTimeoutMessage(Callable<T> supplier, String matcherDescription) {
		final String message;
		Method enclosingMethod = supplier.getClass().getEnclosingMethod();
		if (supplier.getClass().isAnonymousClass() && enclosingMethod != null) {
			message = String.format("Condition returned by method \"%s\" in class %s was not %s", enclosingMethod
					.getName(), enclosingMethod.getDeclaringClass().getName(), matcherDescription);

		} else {
			message = String.format("%s was not %s", supplier.getClass().getName(), matcherDescription);
		}
		return message;
	}
}
