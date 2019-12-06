package org.awaitility.classes;

class SafeExceptionRethrower {

    static <T> T safeRethrow(Throwable t) {
        SafeExceptionRethrower.<RuntimeException>safeRethrow0(t);
        return null;
    }

    private static <T extends Throwable> void safeRethrow0(Throwable t) throws T {
        //noinspection ConstantConditions,unchecked
        throw (T) t;
    }
}