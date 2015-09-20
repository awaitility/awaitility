package com.jayway.awaitility;

import com.jayway.awaitility.classes.Asynch;
import com.jayway.awaitility.classes.FakeRepository;
import com.jayway.awaitility.classes.FakeRepositoryImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class AwaitilityIgnoreExceptionsJava8Test {
    private FakeRepository fakeRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        fakeRepository = new FakeRepositoryImpl();
        Awaitility.reset();
    }

    @Test(timeout = 2000)
    public void exceptionIgnoringWorksWithPredicates() {
        new Asynch(fakeRepository).perform();
        await().atMost(1000, MILLISECONDS).and().ignoreExceptionsMatching(e -> e.getMessage().endsWith("is not 1")).until(() -> {
            if (fakeRepository.getValue() != 1) {
                throw new IllegalArgumentException("Repository value is not 1");
            }
            return true;
        });
    }

    @Test(timeout = 2000)
    public void exceptionIgnoringWorksWithPredicatesStatically() {
        new Asynch(fakeRepository).perform();
        Awaitility.ignoreExceptionsByDefaultMatching(e -> e instanceof RuntimeException);
        await().atMost(1000, MILLISECONDS).until(() -> {
            if (fakeRepository.getValue() != 1) {
                throw new IllegalArgumentException("Repository value is not 1");
            }
            return true;
        });
    }
}
