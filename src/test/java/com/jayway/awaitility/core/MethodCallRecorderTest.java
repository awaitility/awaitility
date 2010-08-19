package com.jayway.awaitility.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.jayway.awaitility.Awaitility;
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
		Awaitility.callTo(service).getValue();
	}

	private void suggestGarbageCollection() throws InterruptedException {
		for (int indx=0; indx<100; indx++) {
			System.gc();
			Thread.sleep(2);
		}
	}
}
