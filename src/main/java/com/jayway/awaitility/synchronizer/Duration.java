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
package com.jayway.awaitility.synchronizer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.TimeUnit;

public class Duration {
	public static final Duration FOREVER = new Duration();
	public static final Duration ONE_HUNDRED_MILLISECONDS = new Duration(100, MILLISECONDS);
	public static final Duration TWO_HUNDRED_MILLISECONDS = new Duration(200, MILLISECONDS);
	public static final Duration FIVE_HUNDRED_MILLISECONDS = new Duration(500, MILLISECONDS);
	public static final Duration ONE_SECOND = new Duration(1, SECONDS);
	public static final Duration TWO_SECONDS = new Duration(2, SECONDS);
	public static final Duration FIVE_SECONDS = new Duration(5, SECONDS);
	public static final Duration TEN_SECONDS = new Duration(10, SECONDS);
	public static final Duration ONE_MINUTE = new Duration(60, SECONDS);
	public static final Duration TWO_MINUTES = new Duration(120, SECONDS);
	public static final Duration FIVE_MINUTES = new Duration(300, SECONDS);
	public static final Duration TEN_MINUTES = new Duration(600, SECONDS);;

	private final long value;
	private final TimeUnit unit;

	private Duration() {
		this.value = -1;
		this.unit = null;
	}

	public Duration(long value, TimeUnit unit) {
		if (value <= 0) {
			throw new IllegalArgumentException("value must be > 0");
		}
		if (unit == null) {
			throw new IllegalArgumentException("TimeUnit cannot be null");
		}
		this.value = value;
		this.unit = unit;
	}

	public TimeUnit getTimeUnit() {
		return unit;
	}

	public long getValue() {
		return value;
	}
}