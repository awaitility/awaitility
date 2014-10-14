package com.jayway.awaitility;

import com.jayway.awaitility.classes.Asynch;
import com.jayway.awaitility.classes.FakeRepository;
import com.jayway.awaitility.classes.FakeRepositoryImpl;
import com.jayway.awaitility.core.ConditionEvaluationLogger;
import com.jayway.awaitility.core.ConditionTimeoutException;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.awaitility.Awaitility.with;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Tests for await().until(Runnable) using AssertionCondition.
 *
 * @author Marcin Zajączkowski, 2014-03-28
 */
public class AwaitilityJava8Test {

    private FakeRepository fakeRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public TestName testName = new TestName();

    @Before
    public void setup() {
        fakeRepository = new FakeRepositoryImpl();
        Awaitility.reset();
    }

    @Test(timeout = 2000)
    public void awaitAssertJAssertionAsLambda() {
        new Asynch(fakeRepository).perform();
        await().until(() -> Assertions.assertThat(fakeRepository.getValue()).isEqualTo(1));
    }

    @Test(timeout = 2000)
    public void awaitUsingLambdaVersionOfCallableBoolean() {
        new Asynch(fakeRepository).perform();
        await().until(() -> fakeRepository.getValue() == 1);
    }

    @SuppressWarnings("Convert2Lambda")
    @Test(timeout = 2000)
    public void awaitAssertJAssertionAsAnonymousClass() {
        new Asynch(fakeRepository).perform();
        await().until(new Runnable() {
            @Override
            public void run() {
                Assertions.assertThat(fakeRepository.getValue()).isEqualTo(1);
            }
        });
    }

    @Test(timeout = 2000)
    public void awaitAssertJAssertionDisplaysOriginalErrorMessageAndTimeoutWhenConditionTimeoutExceptionOccurs() {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage(startsWith("Condition that uses lambda expression in " + AwaitilityJava8Test.class.getName()));
        exception.expectMessage(endsWith("expected:<[1]> but was:<[0]> within 120 milliseconds."));

        new Asynch(fakeRepository).perform();
        with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).until(
                () -> Assertions.assertThat(fakeRepository.getValue()).isEqualTo(1));
    }

    @Test(timeout = 2000)
    public void awaitJUnitAssertionAsLambda() {
        new Asynch(fakeRepository).perform();
        await().until(() -> assertEquals(1, fakeRepository.getValue()));
    }

    @Test(timeout = 2000)
    public void awaitJUnitAssertionDisplaysOriginalErrorMessageAndTimeoutWhenConditionTimeoutExceptionOccurs() {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage(startsWith("Condition that uses lambda expression in " + AwaitilityJava8Test.class.getName()));
        exception.expectMessage(endsWith("expected:<1> but was:<0> within 120 milliseconds."));

        with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).until(
                () -> assertEquals(1, fakeRepository.getValue()));
    }

    @Test(timeout = 2000)
    public void lambdaErrorMessageLooksAlrightWhenUsingMethodReferences() {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage("Lambda expression in com.jayway.awaitility.AwaitilityJava8Test that uses com.jayway.awaitility.classes.FakeRepository: expected <1> but was <0> within 120 milliseconds.");
        with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).until(fakeRepository::getValue, equalTo(1));
    }

    @SuppressWarnings("Convert2MethodRef")
    @Test(timeout = 2000)
    public void lambdaErrorMessageLooksAlrightWhenUsingLambda() {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage("Lambda expression in com.jayway.awaitility.AwaitilityJava8Test: expected <1> but was <0> within 120 milliseconds.");

        with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).until(() -> fakeRepository.getValue(), equalTo(1));
    }

    @SuppressWarnings({"Convert2MethodRef", "CodeBlock2Expr"})
    @Test(timeout = 2000)
    public void lambdaErrorMessageLooksAlrightWhenUsingLambdaWithCurlyBraces() {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage("Lambda expression in com.jayway.awaitility.AwaitilityJava8Test: expected <1> but was <0> within 120 milliseconds.");

        with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).until(() -> {
            return fakeRepository.getValue();
        }, equalTo(1));
    }

    @Test(timeout = 2000)
    public void lambdaErrorMessageLooksAlrightWhenAwaitUsingLambdaVersionOfCallableBoolean() {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage("Condition with lambda expression in com.jayway.awaitility.AwaitilityJava8Test was not fulfilled within 200 milliseconds.");

        await().atMost(200, MILLISECONDS).until(() -> fakeRepository.getValue() == 2);
    }

    @Test(timeout = 10000)
    public void conditionResultsCanBeLoggedToSystemOut() {
        with()
                .conditionEvaluationListener(condition -> System.out.printf("%s (elapsed time %dms, remaining time %dms)\n", condition.getDescription(), condition.getElapsedTimeInMS(), condition.getRemainingTimeInMS()))
                .pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
                .atMost(Duration.TWO_SECONDS)
                .until(new CountDown(5), anyOf(is(0), lessThan(0)));
    }

    @Test(timeout = 10000)
    public void loggingIntermediaryHandlerLogsToSystemOut() {
        with()
                .conditionEvaluationListener(new ConditionEvaluationLogger(SECONDS))
                .pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
                .atMost(Duration.TWO_SECONDS)
                .until(new CountDown(5), is(equalTo(0)));
    }

    @Test(timeout = 2000)
    public void expectedMatchMessageForAssertionConditionsWhenUsingLambdasWithoutAlias() {
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        CountDown countDown = new CountDown(10);
        with()
                .conditionEvaluationListener(condition -> {
                    try {
                        countDown.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    lastMatchMessage.set(condition.getDescription());
                })
                .until(() -> assertEquals(5, (int) countDown.get()));

        String expectedMatchMessage = String.format("%s reached its end value", CountDown.class.getName());
        assertThat(lastMatchMessage.get(), allOf(startsWith("Condition that uses lambda expression"), endsWith(expectedMatchMessage)));
    }

    @Test(timeout = 2000)
    public void expectedMatchMessageForAssertionConditionsWhenUsingLambdasWithAlias() {
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        CountDown countDown = new CountDown(10);
        with()
                .conditionEvaluationListener(condition -> {
                    try {
                        countDown.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    lastMatchMessage.set(condition.getDescription());
                }).await("my alias")
                .until(() -> assertEquals(5, (int) countDown.get()));

        String expectedMatchMessage = String.format("%s reached its end value", CountDown.class.getName());
        assertThat(lastMatchMessage.get(), allOf(startsWith("Condition with alias my alias that uses lambda expression"), endsWith(expectedMatchMessage)));
    }

    @Test(timeout = 2000)
    public void expectedMismatchMessageForAssertionConditionsWhenUsingLambdasWithoutAlias() {
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        CountDown countDown = new CountDown(10);
        try {
            with()
                    .conditionEvaluationListener(condition -> {
                        try {
                            countDown.call();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        lastMatchMessage.set(condition.getDescription());
                    }).await().atMost(150, MILLISECONDS).until(() -> assertEquals(-1, (int) countDown.get()));
            fail("Test should fail");
        } catch (ConditionTimeoutException e) {
            assertThat(lastMatchMessage.get(), allOf(startsWith("Condition that uses lambda expression in"), containsString("expected:<-1> but was:<")));
        }
    }

    @Test(timeout = 2000)
    public void expectedMismatchMessageForAssertionConditionsWhenUsingLambdasWithAlias() {
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        CountDown countDown = new CountDown(10);
        try {
            with().conditionEvaluationListener(condition -> {
                try {
                    countDown.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                lastMatchMessage.set(condition.getDescription());
            }).await("my alias").atMost(150, MILLISECONDS).until(() -> assertEquals(-1, (int) countDown.get()));
            fail("Test should fail");
        } catch (ConditionTimeoutException e) {
            assertThat(lastMatchMessage.get(), startsWith("Condition with alias my alias that uses lambda expression"));
        }
    }

    @SuppressWarnings("Convert2Lambda")
    @Test(timeout = 2000)
    public void expectedMatchMessageForAssertionConditionsWhenNotUsingLambdasWithoutAlias() {
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        CountDown countDown = new CountDown(10);
        with()
                .conditionEvaluationListener(condition -> {
                    try {
                        countDown.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    lastMatchMessage.set(condition.getDescription());
                })
                .until(new Runnable() {
                    @Override
                    public void run() {
                        assertEquals(5, (int) countDown.get());
                    }
                });

        assertThat(lastMatchMessage.get(), allOf(startsWith("Runnable condition defined in"), containsString(testName.getMethodName()), endsWith("reached its end value")));
    }

    @SuppressWarnings("Convert2Lambda")
    @Test(timeout = 2000)
    public void expectedMatchMessageForAssertionConditionsWhenNotUsingLambdasWithAlias() {
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        CountDown countDown = new CountDown(10);
        with()
                .conditionEvaluationListener(condition -> {
                    try {
                        countDown.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    lastMatchMessage.set(condition.getDescription());
                }).await("my alias")
                .until(new Runnable() {
                    @Override
                    public void run() {
                        assertEquals(5, (int) countDown.get());
                    }
                });

        assertThat(lastMatchMessage.get(), allOf(startsWith("Runnable condition with alias my alias defined in"), containsString(testName.getMethodName()), endsWith("reached its end value")));
    }

    @SuppressWarnings("Convert2Lambda")
    @Test(timeout = 2000)
    public void expectedMismatchMessageForAssertionConditionsWhenNotUsingLambdasWithoutAlias() {
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        CountDown countDown = new CountDown(10);
        try {
            with()
                    .conditionEvaluationListener(condition -> {
                        lastMatchMessage.set(condition.getDescription());
                        try {
                            countDown.call();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).await().atMost(150, MILLISECONDS).until(new Runnable() {
                @Override
                public void run() {
                    assertEquals(-1, (int) countDown.get());
                }
            });
            fail("Expected to fail");
        } catch (ConditionTimeoutException e) {
            assertThat(lastMatchMessage.get(), allOf(startsWith("Runnable condition defined in"), containsString(testName.getMethodName()), containsString("expected:")));
        }
    }

    @SuppressWarnings("Convert2Lambda")
    @Test(timeout = 2000)
    public void expectedMismatchMessageForAssertionConditionsWhenNotUsingLambdasWithAlias() {
        final AtomicReference<String> lastMatchMessage = new AtomicReference<>();
        CountDown countDown = new CountDown(10);
        try {
            with()
                    .conditionEvaluationListener(condition -> {
                        lastMatchMessage.set(condition.getDescription());
                        try {
                            countDown.call();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).await("my alias").atMost(150, MILLISECONDS)
                    .until(new Runnable() {
                        @Override
                        public void run() {
                            assertEquals(5, (int) countDown.get());
                        }
                    });
            fail("Expected to fail");
        } catch (ConditionTimeoutException e) {
            assertThat(lastMatchMessage.get(), allOf(startsWith("Runnable condition with alias my alias defined in"), containsString(testName.getMethodName()), containsString("expected:")));
        }
    }

    private static class CountDown implements Callable<Integer> {

        private int countDown;

        private CountDown(int countDown) {
            this.countDown = countDown;
        }

        public Integer call() throws Exception {
            return countDown--;
        }

        public Integer get() {
            return countDown;
        }
    }
}
