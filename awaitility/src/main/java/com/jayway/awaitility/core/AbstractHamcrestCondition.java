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

import org.hamcrest.Matcher;

import java.util.concurrent.Callable;

abstract class AbstractHamcrestCondition<T> implements Condition {

	private ConditionAwaiter conditionAwaiter;

	private T lastResult;

	public AbstractHamcrestCondition(final Callable<T> supplier, final Matcher<? super T> matcher, ConditionSettings settings) {
		if (supplier == null) {
			throw new IllegalArgumentException("You must specify a supplier (was null).");
		}
		if (matcher == null) {
			throw new IllegalArgumentException("You must specify a matcher (was null).");
		}
		Callable<Boolean> callable = new Callable<Boolean>() {
			public Boolean call() throws Exception {
				lastResult = supplier.call();
				return matcher.matches(lastResult);
			}
		};
		conditionAwaiter = new ConditionAwaiter(callable, settings) {
			@Override
			protected String getTimeoutMessage() {
				return String.format("%s expected %s but was <%s>", getCallableDescription(supplier), HamcrestToStringFilter.filter(matcher), lastResult);
			}
		};
	}

	public void await() {
		conditionAwaiter.await();
	}

	abstract String getCallableDescription(final Callable<T> supplier);
}
