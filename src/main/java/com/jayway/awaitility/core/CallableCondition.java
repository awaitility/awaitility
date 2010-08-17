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

class CallableCondition implements Condition {

	private final ConditionAwaiter conditionAwaiter;

	@SuppressWarnings("unchecked")
	public CallableCondition(Callable<Boolean> matcher, ConditionSettings settings) {
		final String timeoutMessage;
		if (matcher == null) {
			timeoutMessage = "";
		} else {
			final Class<? extends Callable> type = matcher.getClass();
			final Method enclosingMethod = type.getEnclosingMethod();
			if (type.isAnonymousClass() && enclosingMethod != null) {
				timeoutMessage = String.format("Condition returned by method \"%s\" in class %s was not fulfilled",
						enclosingMethod.getName(), enclosingMethod.getDeclaringClass().getName());
			} else {
				timeoutMessage = String.format("Condition %s was not fulfilled", type.getName());
			}
		}
		conditionAwaiter = new ConditionAwaiter(matcher, timeoutMessage, settings);
	}

	public void await() throws Exception {
		conditionAwaiter.await();
	}
}
