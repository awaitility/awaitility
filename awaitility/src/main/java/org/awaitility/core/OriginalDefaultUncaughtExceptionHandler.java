package org.awaitility.core;

public class OriginalDefaultUncaughtExceptionHandler {

    // null unless explicitly set
    private static volatile Thread.UncaughtExceptionHandler originalDefaultUncaughtExceptionHandler;

    public static Thread.UncaughtExceptionHandler get() {
        return originalDefaultUncaughtExceptionHandler;
    }

    public static void set(Thread.UncaughtExceptionHandler eh) {
        if (!(eh instanceof ConditionAwaiter)) {
            originalDefaultUncaughtExceptionHandler = eh;
        }
    }
}
