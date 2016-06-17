/*
 * Copyright 2015 the original author or authors.
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

package org.awaitility.core;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

/**
 * A {@link Throwable} used as a cause if deadlocked threads are detected by Awaitility.
 */
public class DeadlockException extends Throwable {

    private final ThreadInfo[] threadInfos;

    public DeadlockException(long[] threads) {
        super("Deadlocked threads detected");

        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        threadInfos = bean.getThreadInfo(threads, true, true);
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getMessage()).append(":\n\n");

        for (ThreadInfo info : threadInfos)
            sb.append(info.toString());

        return sb.toString();
    }

    public ThreadInfo[] getThreadInfos() {
        return Arrays.copyOf(threadInfos, threadInfos.length);
    }
}
