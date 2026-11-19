package org.awaitility.core;

import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class InternalExecutorServiceFactoryTest {

    @After
    public void tearDown() {
        Awaitility.reset();
    }

    @Test
    public void finalizerFreeExecutorIsAPlainThreadPoolExecutor() {
        ExecutorService es = InternalExecutorServiceFactory.createFinalizerFree(Thread::new);
        try {
            assertSame(ThreadPoolExecutor.class, es.getClass());
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) es;
            assertEquals(1, tpe.getCorePoolSize());
            assertEquals(1, tpe.getMaximumPoolSize());
            assertEquals(0L, tpe.getKeepAliveTime(TimeUnit.MILLISECONDS));
        } finally {
            es.shutdownNow();
        }
    }

    @Test
    public void finalizerFreeExecutorUsesThreadSupplierAndDefaultThreadName() throws Exception {
        AtomicReference<String> name = new AtomicReference<>();
        ExecutorService es = InternalExecutorServiceFactory.createFinalizerFree((r, threadName) -> {
            name.set(threadName);
            return new Thread(r, threadName);
        }, "myAlias");
        try {
            es.submit(() -> { }).get(5, TimeUnit.SECONDS);
            assertEquals("awaitility[myAlias]", name.get());
        } finally {
            es.shutdownNow();
        }
    }

    @Test
    public void finalizerFreeExecutorCanBeShutDownLikeTheExistingOne() throws Exception {
        ExecutorService es = InternalExecutorServiceFactory.createFinalizerFree(Thread::new);
        es.submit(() -> { }).get(5, TimeUnit.SECONDS);
        es.shutdown();
        assertTrue(es.awaitTermination(5, TimeUnit.SECONDS));
        assertTrue(es.isTerminated());
    }

    @Test(expected = IllegalArgumentException.class)
    public void finalizerFreeFactoryRejectsNullSupplier() {
        InternalExecutorServiceFactory.createFinalizerFree(null);
    }

    @Test
    public void existingPublicCreateStillReturnsTheWrappedExecutor() {
        ExecutorService es = InternalExecutorServiceFactory.create(Thread::new);
        try {
            // Executors.newSingleThreadExecutor wraps the ThreadPoolExecutor, behavior kept as is for users
            assertTrue(es.getClass() != ThreadPoolExecutor.class);
        } finally {
            es.shutdownNow();
        }
    }

    @Test
    public void defaultExecutorUsedByAwaitIsAPlainThreadPoolExecutor() throws Exception {
        ConditionFactory factory = Awaitility.await();
        Method m = ConditionFactory.class.getDeclaredMethod("generateConditionSettings");
        m.setAccessible(true);
        ConditionSettings settings = (ConditionSettings) m.invoke(factory);
        ExecutorService es = settings.getExecutorLifecycle().supplyExecutorService();
        try {
            assertSame(ThreadPoolExecutor.class, es.getClass());
        } finally {
            es.shutdownNow();
        }
    }

    @Test
    public void pollThreadExecutorIsAPlainThreadPoolExecutor() throws Exception {
        assertPlain(Awaitility.await().pollThread(Thread::new));
    }

    @Test
    public void globalDefaultPollThreadExecutorIsAPlainThreadPoolExecutor() throws Exception {
        Awaitility.pollThread(Thread::new);
        // await() copies the global default executor lifecycle into its settings
        assertPlain(Awaitility.await());
    }

    private static void assertPlain(ConditionFactory factory) throws Exception {
        ExecutorService es = factory.getSettings().getExecutorLifecycle().supplyExecutorService();
        try {
            assertSame(ThreadPoolExecutor.class, es.getClass());
        } finally {
            es.shutdownNow();
        }
    }
}
