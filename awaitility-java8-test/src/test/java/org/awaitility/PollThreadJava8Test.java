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

package org.awaitility;

import org.awaitility.classes.Asynch;
import org.awaitility.classes.ExceptionThrowingAsynch;
import org.awaitility.classes.FakeRepository;
import org.awaitility.classes.FakeRepositoryImpl;
import org.awaitility.core.ConditionTimeoutException;
import org.awaitility.core.InternalExecutorServiceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.platform.commons.JUnitException;
import org.opentest4j.AssertionFailedError;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.given;
import static org.awaitility.Awaitility.with;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;

@SuppressWarnings("Duplicates")
class PollThreadJava8Test {
    private FakeRepository fakeRepository;

    @BeforeEach
    void setup() {
        fakeRepository = new FakeRepositoryImpl();
        Awaitility.reset();
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void canRunConditionEvaluationsInTheSameThreadAsTheTestThread() {
        new Asynch(fakeRepository).perform();
        AtomicReference<Thread> threadAtomicReference = new AtomicReference<>();

        with().pollInSameThread().and().conditionEvaluationListener(__ -> threadAtomicReference.set(Thread.currentThread())).
                await().atMost(1000, MILLISECONDS).until(() -> fakeRepository.getValue() == 1);

        assertThat(threadAtomicReference.get()).isEqualTo(Thread.currentThread());
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void canRunConditionEvaluationsInTheSameThreadAsTheTestThreadWhenConfiguredStatically() {
        new Asynch(fakeRepository).perform();
        AtomicReference<Thread> threadAtomicReference = new AtomicReference<>();

        Awaitility.pollInSameThread();

        with().conditionEvaluationListener(__ -> threadAtomicReference.set(Thread.currentThread())).
                await().atMost(1000, MILLISECONDS).until(() -> fakeRepository.getValue() == 1);

        assertThat(threadAtomicReference.get()).isEqualTo(Thread.currentThread());
    }

    @Test
    void uncaughtExceptionsArePropagatedToAwaitingThreadButCannotBreakForeverBlockWhenConditionIsEvaluatedFromTheTestThread() throws Exception {
        new ExceptionThrowingAsynch(new IllegalStateException("Illegal state!")).perform();
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertTimeoutPreemptively(Duration.ofMillis(700),
                    () -> given().catchUncaughtExceptions().and().pollInSameThread().await().forever().until(() -> fakeRepository.getValue() == 1)
            )
        );
        assertInstanceOf(JUnitException.class, error.getCause());
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void canTimeoutWhenPollingInSameThreadAsTest() throws Exception {
        new Asynch(fakeRepository).perform();

        assertThrows(ConditionTimeoutException.class,
                () -> given().pollInSameThread().await().atMost(300, MILLISECONDS).until(() -> fakeRepository.getValue() == 1));
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void canRunConditionEvaluationsCustomTestWithoutAliasThreadUsingJava8MethodReference() {
        new Asynch(fakeRepository).perform();
        AtomicReference<Thread> threadAtomicReference = new AtomicReference<>();

        with().pollThread(Thread::new).and().conditionEvaluationListener(__ -> threadAtomicReference.set(Thread.currentThread())).
                await().atMost(1000, MILLISECONDS).until(() -> fakeRepository.getValue() == 1);

        assertThat(threadAtomicReference.get()).isNotEqualTo(Thread.currentThread());
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void pollThreadSupplierIsCalledOncePerTest() {
        new Asynch(fakeRepository).perform();
        List<Thread> conditionThreads = new CopyOnWriteArrayList<>();

        with().pollThread(Thread::new).and().conditionEvaluationListener(__ -> conditionThreads.add(Thread.currentThread())).
                await().atMost(1000, MILLISECONDS).until(() -> fakeRepository.getValue() == 1);

        assertThat(new HashSet<>(conditionThreads)).doesNotContain(Thread.currentThread()).hasSize(1);
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void canRunConditionEvaluationsInCustomThread() {
        new Asynch(fakeRepository).perform();
        AtomicReference<Thread> expectedThread = new AtomicReference<>();
        AtomicReference<Thread> actualThread = new AtomicReference<>();

        with().pollThread(r -> {
            Thread thread = new Thread(r);
            expectedThread.set(thread);
            return thread;
        }).and().conditionEvaluationListener(__ -> actualThread.set(Thread.currentThread())).
                await().atMost(1000, MILLISECONDS).until(() -> fakeRepository.getValue() == 1);

        assertThat(actualThread.get()).isNotEqualTo(Thread.currentThread()).isEqualTo(expectedThread.get());
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void canRunConditionEvaluationsInCustomThreadWhenConfiguredStatically() {
        new Asynch(fakeRepository).perform();
        AtomicReference<Thread> expectedThread = new AtomicReference<>();
        AtomicReference<Thread> actualThread = new AtomicReference<>();

        Awaitility.pollThread(r -> {
            Thread thread = new Thread(r);
            expectedThread.set(thread);
            return thread;
        });

        with().conditionEvaluationListener(__ -> actualThread.set(Thread.currentThread())).
                await().atMost(1000, MILLISECONDS).until(() -> fakeRepository.getValue() == 1);

        assertThat(actualThread.get()).isNotEqualTo(Thread.currentThread()).isEqualTo(expectedThread.get());
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void canRunConditionInSpecificExecutorService() throws ExecutionException, InterruptedException {
        ExecutorService executorService = InternalExecutorServiceFactory.create(Thread::new);
        FakeRepository threadLocalRepo = executorService.submit(() -> new FakeRepository() {
            ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

            @Override
            public int getValue() {
                Integer myValue = threadLocal.get();
                return myValue == null ? 0 : myValue;
            }

            @Override
            public void setValue(int value) {
                threadLocal.set(value);
            }
        }).get();

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(600);
                executorService.submit(() -> threadLocalRepo.setValue(1));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

        given().pollExecutorService(executorService).await().atMost(1000, MILLISECONDS).until(() -> threadLocalRepo.getValue() == 1);
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void canRunConditionInSpecificExecutorServiceWhenExecutorServiceIsConfiguredStatically() throws ExecutionException, InterruptedException {

        ExecutorService executorService = InternalExecutorServiceFactory.create(Thread::new);
        Awaitility.pollExecutorService(executorService);
        FakeRepository threadLocalRepo = executorService.submit(() -> new FakeRepository() {
            ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

            @Override
            public int getValue() {
                Integer myValue = threadLocal.get();
                return myValue == null ? 0 : myValue;
            }

            @Override
            public void setValue(int value) {
                threadLocal.set(value);
            }
        }).get();

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(600);
                executorService.submit(() -> threadLocalRepo.setValue(1));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

        await().atMost(1000, MILLISECONDS).until(() -> threadLocalRepo.getValue() == 1);
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void awaitilityPollThreadIsGivenANameEqualToAwaitilityThreadWhenNotUsingAnAlias() {
        new Asynch(fakeRepository).perform();
        AtomicReference<Thread> thread = new AtomicReference<>();

        with().conditionEvaluationListener(__ -> thread.set(Thread.currentThread())).
                await().atMost(1000, MILLISECONDS).until(() -> fakeRepository.getValue() == 1);

        assertThat(thread.get().getName()).isEqualTo("awaitility-thread");
    }

    @Timeout(value = 2000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
    @Test
    void awaitilityPollThreadIsGivenANameIncludingAliasWhenUsingAnAlias() {
        new Asynch(fakeRepository).perform();
        AtomicReference<Thread> thread = new AtomicReference<>();

        with().conditionEvaluationListener(__ -> thread.set(Thread.currentThread())).
                await("my alias").atMost(1000, MILLISECONDS).until(() -> fakeRepository.getValue() == 1);

        assertThat(thread.get().getName()).isEqualTo("awaitility[my alias]");
    }
}
