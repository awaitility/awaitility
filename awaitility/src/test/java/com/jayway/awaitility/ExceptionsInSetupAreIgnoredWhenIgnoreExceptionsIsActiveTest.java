package com.jayway.awaitility;

import com.jayway.awaitility.classes.Asynch;
import com.jayway.awaitility.classes.FakeRepository;
import com.jayway.awaitility.classes.FakeRepositoryImpl;
import com.jayway.awaitility.classes.ThrowExceptionUnlessFakeRepositoryEqualsOne;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class ExceptionsInSetupAreIgnoredWhenIgnoreExceptionsIsActiveTest {

    private static FakeRepository fakeRepository = new FakeRepositoryImpl();

    @BeforeClass
    public static void exceptionThrowingSetupStep() {
        new Asynch(fakeRepository).perform();
        Awaitility
                .with().ignoreExceptions()
                .await().atMost(1000, TimeUnit.MILLISECONDS).until(
                    conditionsThatIsThrowingAnExceptionForATime()
                );
    }

    private static Callable<Boolean> conditionsThatIsThrowingAnExceptionForATime() {
        return new ThrowExceptionUnlessFakeRepositoryEqualsOne(fakeRepository);
    }

    @Test
    public void exceptionsInTestSetupAreIgnoredWhenIgnoringExceptions() {
        // nothing here, test logic sits in @BeforeClass method
    }
}
