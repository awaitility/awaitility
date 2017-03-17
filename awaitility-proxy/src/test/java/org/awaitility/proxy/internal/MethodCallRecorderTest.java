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

import org.awaitility.classes.FakeRepository;
import org.awaitility.classes.FakeRepositoryImpl;
import org.junit.Before;
import org.junit.Test;

import static org.awaitility.proxy.AwaitilityClassProxy.to;
import static org.junit.Assert.assertEquals;

public class MethodCallRecorderTest {
	
	@Before
	public void before() {
		MethodCallRecorder.reset();
	}

	@Test
	public void finalizeShouldNotBeRecorded() throws Exception {
		recordCall(new FakeRepositoryImpl());
		suggestGarbageCollection();
		assertEquals("getValue", MethodCallRecorder.getLastMethod().getName());
	}

	@Test(expected=IllegalStateException.class)
	public void exceptionIfNoMethodWasRecorded() throws Exception {
		MethodCallRecorder.getLastTarget();
	}

	private void recordCall(FakeRepository service) {
		to(service).getValue();
	}

	private void suggestGarbageCollection() throws InterruptedException {
		for (int indx = 0; indx < 100; indx++) {
			System.gc();
			Thread.sleep(2);
		}
	}
}
