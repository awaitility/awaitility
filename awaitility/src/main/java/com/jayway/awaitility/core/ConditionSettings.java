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