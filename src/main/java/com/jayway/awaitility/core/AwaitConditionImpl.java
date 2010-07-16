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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AwaitConditionImpl implements Condition, UncaughtExceptionHandler {
	private final Duration maxWaitTime;
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	private final CountDownLatch latch;
	private Exception exception = null;
	private final String alias;

	public AwaitConditionImpl(String alias, final Duration maxWaitTime, final Callable<Boolean> condition,
			Duration pollInterval, Duration pollDelay) {
		if (maxWaitTime == null) {
			throw new IllegalArgumentException("You must specify a maximum waiting time (was null).");
		}
		if (condition == null) {
			throw new IllegalArgumentException("You must specify a condition (was null).");
		}
		if (pollInterval == null) {
			throw new IllegalArgumentException("You must specify a poll interval (was null).");
		}
		if (pollDelay == null) {
			throw new IllegalArgumentException("You must specify a poll delay (was null).");
		}
		this.alias = alias;
		latch = new CountDownLatch(1);
		this.maxWaitTime = maxWaitTime;
		Runnable command = new Runnable() {
			public void run() {
				try {
					if (condition.call()) {
						latch.countDown();
					}
				} catch (Exception e) {
					exception = e;
					latch.countDown();
				}
			}
		};
		executor.scheduleAtFixedRate(command, pollDelay.getValueInMS(), pollInterval.getValueInMS(),
				TimeUnit.MILLISECONDS);
	}

	public void await() throws Exception {
		try {
			final long timeout = maxWaitTime.getValue();
			final boolean finishedBeforeTimeout;
			if (maxWaitTime == Duration.FOREVER) {
				latch.await();
				finishedBeforeTimeout = true;
			} else if (maxWaitTime == Duration.SAME_AS_POLL_INTERVAL) {
				throw new IllegalStateException("Cannot use 'SAME_AS_POLL_INTERVAL' as maximum wait time.");
			} else {
				finishedBeforeTimeout = latch.await(timeout, maxWaitTime.getTimeUnit());
			}
			if (exception != null) {
				throw exception;
			} else if (!finishedBeforeTimeout) {
				throw new TimeoutException(String.format("Condition%sdidn't complete within %s %s.",
						alias == null ? " " : String.format(" with alias '%s' ", alias), timeout, maxWaitTime
								.getTimeUnit().toString().toLowerCase()));
			}
		} finally {
			executor.shutdown();
			if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		}
	}

	public void uncaughtException(Thread thread, Throwable throwable) {
		if (throwable instanceof Exception) {
			exception = (Exception) throwable;
			if (latch.getCount() != 0) {
				latch.countDown();
			}
		} else {
			throw new RuntimeException(throwable);
		}
	}

	public Condition andCatchAllUncaughtExceptions() {
		Thread.setDefaultUncaughtExceptionHandler(this);
		return this;
	}
}
