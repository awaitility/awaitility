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

import static com.jayway.awaitility.Awaitility.callTo;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.jayway.awaitility.classes.FakeRepository;
import com.jayway.awaitility.classes.FakeRepositoryImpl;

public class MethodCallRecorderTest {
	@Test
	public void finalizeShouldNotBeRecorded() throws Exception {
		recordCall(new FakeRepositoryImpl());
		suggestGarbageCollection();
		assertEquals("getValue", MethodCallRecorder.getLastMethod().getName());
	}

	private void recordCall(FakeRepository service) {
		callTo(service).getValue();
	}

	private void suggestGarbageCollection() throws InterruptedException {
		for (int indx = 0; indx < 100; indx++) {
			System.gc();
			Thread.sleep(2);
		}
	}
}
