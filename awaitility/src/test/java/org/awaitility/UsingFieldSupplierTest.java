/*
 * Copyright 2016 the original author or authors.
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

import org.awaitility.classes.*;
import org.awaitility.core.ConditionTimeoutException;
import org.awaitility.reflect.exception.FieldNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.fieldIn;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;

class UsingFieldSupplierTest {
    private FakeRepository fakeRepository;

    @BeforeEach
    void setup() {
        fakeRepository = new FakeRepositoryImpl();
        Awaitility.reset();
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void ofTypeAndName() throws Exception {
        new Asynch(fakeRepository).perform();
        await().until(fieldIn(fakeRepository).ofType(int.class).andWithName("value"), equalTo(1));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void typeOnly() throws Exception {
        new Asynch(fakeRepository).perform();
        await().until(fieldIn(fakeRepository).ofType(int.class), equalTo(1));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void typeAndAnnotation() throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        await().until(fieldIn(repository).ofType(int.class).andAnnotatedWith(ExampleAnnotation.class), equalTo(1));
        assertEquals(1, repository.getValue());
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void typeAndNameAndAnnotation() throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        await().until(
                fieldIn(repository).ofType(int.class).andWithName("value").andAnnotatedWith(ExampleAnnotation.class),
                equalTo(1));
        assertEquals(1, repository.getValue());
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void typeAndAnnotationAndName() throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        await().until(
                fieldIn(repository).ofType(int.class).andAnnotatedWith(ExampleAnnotation.class).andWithName("value"),
                equalTo(1));
        assertEquals(1, repository.getValue());
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void givenStaticFieldAndUsingOfTypeAndName() throws Exception {
        FakeRepositoryWithStaticFieldAndAnnotation repository = new FakeRepositoryWithStaticFieldAndAnnotation();
        new Asynch(repository).perform();
        await().until(fieldIn(FakeRepositoryWithStaticFieldAndAnnotation.class).ofType(int.class).andWithName("value"),
                equalTo(1));
        assertEquals(1, repository.getValue());
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void givenStaticFieldAndUsingOfTypeAndNameThrowsFieldNotFoundExceptionWhenUsingInstance() throws Exception {
        FakeRepositoryWithStaticFieldAndAnnotation repository = new FakeRepositoryWithStaticFieldAndAnnotation();
        new Asynch(repository).perform();
        assertThrows(FieldNotFoundException.class, () -> await().until(fieldIn(repository).ofType(int.class).andWithName("value"), equalTo(1)));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void givenTypeAndNameWhenNameMatchButTypeDoesntThenFieldNotFoundExceptionIsThrown() throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        byte one = (byte) 1;
        assertThrows(FieldNotFoundException.class, () -> await().until(fieldIn(repository).ofType(byte.class).andWithName("value"), equalTo(one)));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void givenTypeAndNameWhenTypeMatchButNameDoesntThenFieldNotFoundExceptionIsThrown() throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        assertThrows(FieldNotFoundException.class, () -> await().until(fieldIn(repository).ofType(int.class).andWithName("value2"), equalTo(1)));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void givenTypeAndNameAndAnnotationWhenNameAndTypeMatchButAnnotationNotFoundThenFieldNotFoundExceptionIsThrown()
            throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        assertThrows(FieldNotFoundException.class, () ->await().until(
                    fieldIn(repository).ofType(int.class).andWithName("value").andAnnotatedWith(ExampleAnnotation2.class),
                    equalTo(1)));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void givenTypeAndNameAndAnnotationWhenNameAndAnnotationMatchButTypeNotFoundThenFieldNotFoundExceptionIsThrown()
            throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        byte one = (byte) 1;
        assertThrows(FieldNotFoundException.class, () -> await().until(fieldIn(repository).ofType(byte.class).andWithName("value").andAnnotatedWith(ExampleAnnotation.class),
                    equalTo(one)));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void givenTypeAndAnnotationAndNameWhenNameAndTypeMatchButAnnotationNotFoundThenFieldNotFoundExceptionIsThrown()
            throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        assertThrows(FieldNotFoundException.class, () -> await().until(
                    fieldIn(repository).ofType(int.class).andAnnotatedWith(ExampleAnnotation2.class).andWithName("value"),
                    equalTo(1)));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void givenTypeAndAnnotationAndNameWhenNameAndAnnotationMatchButTypeNotFoundThenFieldNotFoundExceptionIsThrown()
            throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        byte one = (byte) 1;
        assertThrows(FieldNotFoundException.class, () -> await().until(fieldIn(repository).ofType(byte.class).andAnnotatedWith(ExampleAnnotation.class).andWithName("value"),
                    equalTo(one)));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void givenAnnotationAndTypeWhenAnnotationMatchButTypeDoesntThenFieldNotFoundExceptionIsThrown()
            throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        byte one = (byte) 1;
        assertThrows(FieldNotFoundException.class, () -> await().until(fieldIn(repository).ofType(byte.class).andAnnotatedWith(ExampleAnnotation.class), equalTo(one)));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS, threadMode = SEPARATE_THREAD)
    void givenAnnotationAndTypeWhenTypeMatchButAnnotationDoesntThenFieldNotFoundExceptionIsThrown()
            throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
    }

    @Test
    void showsErrorMessageContainingClassAndTypeWhenOnlyTypeSpecifiedWhenTimeout() {
        Throwable exception = assertThrows(ConditionTimeoutException.class, () -> {
            FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
            new Asynch(repository).perform();
            await().atMost(200, MILLISECONDS).until(fieldIn(repository).ofType(int.class), equalTo(1));
        });
        assertThat(exception.getMessage(), containsString("Field in org.awaitility.classes.FakeRepositoryWithAnnotation of type int expected <1> but was <0> within 200 milliseconds."));
    }

    @Test
    void showsErrorMessageContainingClassAndTypeAndFieldNameWhenTypeAndNameSpecifiedWhenTimeout() {
        Throwable exception = assertThrows(ConditionTimeoutException.class, () -> {
            FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
            new Asynch(repository).perform();
            await().atMost(200, MILLISECONDS).until(fieldIn(repository).ofType(int.class).andWithName("value"), equalTo(1));
        });
        assertThat(exception.getMessage(), containsString("Field private volatile int org.awaitility.classes.FakeRepositoryWithAnnotation.value expected <1> but was <0> within 200 milliseconds."));
    }

    @Test
    void showsErrorMessageContainingClassAndTypeAndFieldNameAndAnnotationWhenTypeAndNameAndAnnotationTypeSpecifiedWhenTimeout() {
        Throwable exception = assertThrows(ConditionTimeoutException.class, () -> {
            FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
            new Asynch(repository).perform();
            await().atMost(200, MILLISECONDS).until(fieldIn(repository).ofType(int.class).andWithName("value").andAnnotatedWith(ExampleAnnotation.class), equalTo(1));
        });
        assertThat(exception.getMessage(), containsString("Field private volatile int org.awaitility.classes.FakeRepositoryWithAnnotation.value expected <1> but was <0> within 200 milliseconds."));
    }

    @Test
    void showsErrorMessageContainingClassAndTypeAndAnnotationWhenTypeAndAnnotationTypeSpecifiedWhenTimeout() {
        Throwable exception = assertThrows(ConditionTimeoutException.class, () -> {
            FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
            new Asynch(repository).perform();
            await().atMost(200, MILLISECONDS).until(fieldIn(repository).ofType(int.class).andAnnotatedWith(ExampleAnnotation.class), equalTo(1));
        });
        assertThat(exception.getMessage(), containsString("Field in org.awaitility.classes.FakeRepositoryWithAnnotation annotated with org.awaitility.classes.ExampleAnnotation and of type int expected <1> but was <0> within 200 milliseconds."));
    }
}
