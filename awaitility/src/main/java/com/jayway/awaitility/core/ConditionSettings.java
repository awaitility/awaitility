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

import static com.jayway.awaitility.Duration.SAME_AS_POLL_INTERVAL;

import com.jayway.awaitility.Duration;

class ConditionSettings {
	private final String alias;
	private final Duration maxWaitTime;
	private final Duration pollInterval;
	private final Duration pollDelay;
	private final boolean catchUncaughtExceptions;

	public ConditionSettings(String alias, boolean catchUncaughtExceptions, Duration maxWaitTime,
			Duration pollInterval, Duration pollDelay) {
		if (maxWaitTime == null) {
			throw new IllegalArgumentException("You must specify a maximum waiting time (was null).");
		}
		if (pollInterval == null) {
			throw new IllegalArgumentException("You must specify a poll interval (was null).");
		}
		if (pollDelay == null) {
			throw new IllegalArgumentException("You must specify a poll delay (was null).");
		}
		this.alias = alias;
		this.maxWaitTime = maxWaitTime;
		this.pollInterval = pollInterval;
		this.pollDelay = pollDelay == SAME_AS_POLL_INTERVAL ? pollInterval : pollDelay;
		this.catchUncaughtExceptions = catchUncaughtExceptions;
	}

	public String getAlias() {
		return alias;
	}

	public Duration getMaxWaitTime() {
		return maxWaitTime;
	}

	public Duration getPollInterval() {
		return pollInterval;
	}

	public Duration getPollDelay() {
		return pollDelay;
	}

	public boolean hasAlias() {
		return alias != null;
	}

	public boolean shouldCatchUncaughtExceptions() {
		return catchUncaughtExceptions;
	}
}