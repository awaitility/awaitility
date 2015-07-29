package com.jayway.awaitility.core;

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
