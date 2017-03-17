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
package org.awaitility.proxy;

import org.junit.Test;

import java.util.concurrent.Callable;

import static org.awaitility.Awaitility.await;
import static org.awaitility.proxy.AwaitilityClassProxy.to;
import static org.hamcrest.Matchers.*;

public class DemoTest {

	@Test
	public void testUsingCallable() throws Exception {
		final CounterService service = new CounterServiceImpl();
		service.run();
		await().until(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				return service.getCount() == 1;
			}
		});
	}

	@Test
	public void testUsingCallTo() throws Exception {
		final CounterService service = new CounterServiceImpl();
		service.run();
		await().untilCall(to(service).getCount(), is(equalTo(1)));
	}

	@Test
	public void testUsingGreaterThan() throws Exception {
		final CounterService service = new CounterServiceImpl();
		service.run();
		await().untilCall(to(service).getCount(), greaterThan(2));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCrash() throws Exception {
		final CounterService service = new CounterServiceImpl(new IllegalArgumentException());
		service.run();
		await().untilCall(to(service).getCount(), is(equalTo(1)));
	}
}

interface CounterService extends Runnable {
	int getCount();
}

class CounterServiceImpl implements CounterService {
	private volatile int count = 0;
	private final RuntimeException exception;

	public CounterServiceImpl() {
		this.exception = null;
	}

	public CounterServiceImpl(RuntimeException exception) {
		this.exception = exception;
	}

	public void run() {
		new Thread(new Runnable() {
			public void run() {
				try {
					for (int indx = 0; indx < 5; indx++) {
						Thread.sleep(1000);
						if (exception != null) {
							throw exception;
						}
						count += 1;
					}
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}).start();
	}

	public int getCount() {
		return count;
	}
}