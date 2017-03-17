package org.awaitility.test.osgi;

/*
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements. See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership. The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License. You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied. See the License for the
  specific language governing permissions and limitations
  under the License.
 */

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.awaitility.proxy.AwaitilityClassProxy.to;
import static org.hamcrest.Matchers.is;
import static org.ops4j.pax.exam.CoreOptions.*;

@Ignore
@RunWith(PaxExam.class)
public class AwaitilityTest {

    @Configuration
    public static Option[] configure() throws Exception {
        return new Option[] //
                {
                        mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.hamcrest", "1.3_1"),
                        junitBundles(),
                        systemProperty("pax.exam.osgi.unresolved.fail").value("true"),
                        systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO"),
                        mavenBundle("org.awaitility", "awaitility").versionAsInProject(),
                        mavenBundle("org.objenesis", "objenesis").versionAsInProject(),
                        mavenBundle("net.bytebuddy", "byte-buddy").versionAsInProject(),
                        // CoreOptions.vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005")
                };
    }

    private boolean success;

    @Test
    public void testWaitUntil() {

        Executors.newScheduledThreadPool(1).schedule(new Runnable() {
            @Override
            public void run() {
                success = true;
            }
        }, 1, SECONDS);
        await().atMost(2, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return success;
            }
        });

    }

    @Test
    public void testProxy() {

        Executors.newScheduledThreadPool(1).schedule(new Runnable() {
            @Override
            public void run() {
                success = true;
            }
        }, 1, SECONDS);
        await().atMost(2, TimeUnit.SECONDS).untilCall(to(this).getState(), is(true));
    }

    boolean getState() {
        return success;
    }

}
