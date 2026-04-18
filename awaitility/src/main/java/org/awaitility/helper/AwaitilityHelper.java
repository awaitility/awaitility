package org.awaitility.helper;


import java.util.concurrent.Callable;
import java.util.function.Function;

public class AwaitilityHelper {

    public static <P, R1, R2> Callable<R2> andThen(Function<P, R1> firstFunc, Function<R1, R2> otherFunc) {
        return () -> firstFunc.andThen(otherFunc).apply(null);
    }

    public static <P, R1, R2> Callable<R2> andBefore(Function<R1, R2> firstFunc, Function<P, R1> otherFunc) {
        return () -> firstFunc.compose(otherFunc).apply(null);
    }
}
