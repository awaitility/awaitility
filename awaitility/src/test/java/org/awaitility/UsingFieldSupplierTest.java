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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.fieldIn;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

public class UsingFieldSupplierTest {
    private FakeRepository fakeRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        fakeRepository = new FakeRepositoryImpl();
        Awaitility.reset();
    }

    
    @Test(timeout = 2000)
    public void ofTypeAndName() throws Exception {
        new Asynch(fakeRepository).perform();
        await().until(fieldIn(fakeRepository).ofType(int.class).andWithName("value"), equalTo(1));
        assertEquals(1, fakeRepository.getValue());
    }
    @Test(timeout = 2000)
    public void typeOnly() throws Exception {
        new Asynch(fakeRepository).perform();
        await().until(fieldIn(fakeRepository).ofType(int.class), equalTo(1));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void typeAndAnnotation() throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        await().until(fieldIn(repository).ofType(int.class).andAnnotatedWith(ExampleAnnotation.class), equalTo(1));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000)
    public void typeAndNameAndAnnotation() throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        await().until(
                fieldIn(repository).ofType(int.class).andWithName("value").andAnnotatedWith(ExampleAnnotation.class),
                equalTo(1));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000)
    public void typeAndAnnotationAndName() throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        await().until(
                fieldIn(repository).ofType(int.class).andAnnotatedWith(ExampleAnnotation.class).andWithName("value"),
                equalTo(1));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000)
    public void givenStaticFieldAndUsingOfTypeAndName() throws Exception {
        FakeRepositoryWithStaticFieldAndAnnotation repository = new FakeRepositoryWithStaticFieldAndAnnotation();
        new Asynch(repository).perform();
        await().until(fieldIn(FakeRepositoryWithStaticFieldAndAnnotation.class).ofType(int.class).andWithName("value"),
                equalTo(1));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000, expected = FieldNotFoundException.class)
    public void givenStaticFieldAndUsingOfTypeAndNameThrowsFieldNotFoundExceptionWhenUsingInstance() throws Exception {
        FakeRepositoryWithStaticFieldAndAnnotation repository = new FakeRepositoryWithStaticFieldAndAnnotation();
        new Asynch(repository).perform();
        await().until(fieldIn(repository).ofType(int.class).andWithName("value"), equalTo(1));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000, expected = FieldNotFoundException.class)
    public void givenTypeAndNameWhenNameMatchButTypeDoesntThenFieldNotFoundExceptionIsThrown() throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        byte one = (byte) 1;
        await().until(fieldIn(repository).ofType(byte.class).andWithName("value"), equalTo(one));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000, expected = FieldNotFoundException.class)
    public void givenTypeAndNameWhenTypeMatchButNameDoesntThenFieldNotFoundExceptionIsThrown() throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        await().until(fieldIn(repository).ofType(int.class).andWithName("value2"), equalTo(1));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000, expected = FieldNotFoundException.class)
    public void givenTypeAndNameAndAnnotationWhenNameAndTypeMatchButAnnotationNotFoundThenFieldNotFoundExceptionIsThrown()
            throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        await().until(
                fieldIn(repository).ofType(int.class).andWithName("value").andAnnotatedWith(ExampleAnnotation2.class),
                equalTo(1));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000, expected = FieldNotFoundException.class)
    public void givenTypeAndNameAndAnnotationWhenNameAndAnnotationMatchButTypeNotFoundThenFieldNotFoundExceptionIsThrown()
            throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        byte one = (byte) 1;
        await().until(fieldIn(repository).ofType(byte.class).andWithName("value").andAnnotatedWith(ExampleAnnotation.class),
                equalTo(one));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000, expected = FieldNotFoundException.class)
    public void givenTypeAndAnnotationAndNameWhenNameAndTypeMatchButAnnotationNotFoundThenFieldNotFoundExceptionIsThrown()
            throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        await().until(
                fieldIn(repository).ofType(int.class).andAnnotatedWith(ExampleAnnotation2.class).andWithName("value"),
                equalTo(1));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000, expected = FieldNotFoundException.class)
    public void givenTypeAndAnnotationAndNameWhenNameAndAnnotationMatchButTypeNotFoundThenFieldNotFoundExceptionIsThrown()
            throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        byte one = (byte) 1;
        await().until(fieldIn(repository).ofType(byte.class).andAnnotatedWith(ExampleAnnotation.class).andWithName("value"),
                equalTo(one));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000, expected = FieldNotFoundException.class)
    public void givenAnnotationAndTypeWhenAnnotationMatchButTypeDoesntThenFieldNotFoundExceptionIsThrown()
            throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        byte one = (byte) 1;
        await().until(fieldIn(repository).ofType(byte.class).andAnnotatedWith(ExampleAnnotation.class), equalTo(one));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000, expected = FieldNotFoundException.class)
    public void givenAnnotationAndTypeWhenTypeMatchButAnnotationDoesntThenFieldNotFoundExceptionIsThrown()
            throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        await().until(fieldIn(repository).ofType(int.class).andAnnotatedWith(ExampleAnnotation2.class), equalTo(1));
        assertEquals(1, repository.getValue());
    }

    @Test
    public void showsErrorMessageContainingClassAndTypeWhenOnlyTypeSpecifiedWhenTimeout() throws Exception {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage("Field in org.awaitility.classes.FakeRepositoryWithAnnotation of type int expected <1> but was <0> within 200 milliseconds.");

        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        await().atMost(200, MILLISECONDS).until(fieldIn(repository).ofType(int.class), equalTo(1));
    }

    @Test
    public void showsErrorMessageContainingClassAndTypeAndFieldNameWhenTypeAndNameSpecifiedWhenTimeout() throws Exception {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage("Field private volatile int org.awaitility.classes.FakeRepositoryWithAnnotation.value expected <1> but was <0> within 200 milliseconds.");

        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        await().atMost(200, MILLISECONDS).until(fieldIn(repository).ofType(int.class).andWithName("value"), equalTo(1));
    }

    @Test
    public void showsErrorMessageContainingClassAndTypeAndFieldNameAndAnnotationWhenTypeAndNameAndAnnotationTypeSpecifiedWhenTimeout() throws Exception {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage("Field private volatile int org.awaitility.classes.FakeRepositoryWithAnnotation.value expected <1> but was <0> within 200 milliseconds.");

        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        await().atMost(200, MILLISECONDS).until(fieldIn(repository).ofType(int.class).andWithName("value").andAnnotatedWith(ExampleAnnotation.class), equalTo(1));
    }

    @Test
    public void showsErrorMessageContainingClassAndTypeAndAnnotationWhenTypeAndAnnotationTypeSpecifiedWhenTimeout() throws Exception {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage("Field in org.awaitility.classes.FakeRepositoryWithAnnotation annotated with org.awaitility.classes.ExampleAnnotation and of type int expected <1> but was <0> within 200 milliseconds.");

        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        await().atMost(200, MILLISECONDS).until(fieldIn(repository).ofType(int.class).andAnnotatedWith(ExampleAnnotation.class), equalTo(1));
    }
}
