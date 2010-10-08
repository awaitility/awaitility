package com.jayway.awaitility;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.awaitility.Awaitility.fieldIn;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.jayway.awaitility.classes.Asynch;
import com.jayway.awaitility.classes.ExampleAnnotation;
import com.jayway.awaitility.classes.ExampleAnnotation2;
import com.jayway.awaitility.classes.FakeRepository;
import com.jayway.awaitility.classes.FakeRepositoryImpl;
import com.jayway.awaitility.classes.FakeRepositoryWithAnnotation;
import com.jayway.awaitility.classes.FakeRepositoryWithStaticFieldAndAnnotation;
import com.jayway.awaitility.reflect.exception.FieldNotFoundException;

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
    public void nameAndOfType() throws Exception {
        new Asynch(fakeRepository).perform();
        await().until(fieldIn(fakeRepository).withName("value").andOfType(int.class), equalTo(1));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void nameOnly() throws Exception {
        new Asynch(fakeRepository).perform();
        Object expected = 1;
        await().until(fieldIn(fakeRepository).withName("value"), equalTo(expected));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void typeOnly() throws Exception {
        new Asynch(fakeRepository).perform();
        await().until(fieldIn(fakeRepository).ofType(int.class), equalTo(1));
        assertEquals(1, fakeRepository.getValue());
    }

    @Test(timeout = 2000)
    public void annotationOnly() throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        Object one = 1;
        await().until(fieldIn(repository).annotatedWith(ExampleAnnotation.class), equalTo(one));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000)
    public void annotationAndName() throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        Object one = 1;
        await().until(fieldIn(repository).annotatedWith(ExampleAnnotation.class).andWithName("value"), equalTo(one));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000)
    public void annotationAndNameAndType() throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        await().until(
                fieldIn(repository).annotatedWith(ExampleAnnotation.class).andWithName("value").andOfType(int.class),
                equalTo(1));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000)
    public void annotationAndType() throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        await().until(fieldIn(repository).annotatedWith(ExampleAnnotation.class).andOfType(int.class), equalTo(1));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000)
    public void annotationAndTypeAndName() throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        await().until(
                fieldIn(repository).annotatedWith(ExampleAnnotation.class).andOfType(int.class).andWithName("value"),
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
    public void givenNameAndTypeWhenNameMatchButTypeDoesntThenFieldNotFoundExceptionIsThrown() throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        byte one = (byte) 1;
        await().until(fieldIn(repository).withName("value").andOfType(byte.class), equalTo(one));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000, expected = FieldNotFoundException.class)
    public void givenNameAndTypeWhenTypeMatchButNameDoesntThenFieldNotFoundExceptionIsThrown() throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        await().until(fieldIn(repository).withName("value2").andOfType(int.class), equalTo(1));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000, expected = FieldNotFoundException.class)
    public void givenAnnotationAndNameWhenNameMatchButAnnotationNotFoundThenFieldNotFoundExceptionIsThrown()
            throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        Object one = 1;
        await().until(fieldIn(repository).annotatedWith(ExampleAnnotation2.class).andWithName("value"), equalTo(one));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000, expected = FieldNotFoundException.class)
    public void givenAnnotationAndNameAndTypeWhenNameAndTypeMatchButAnnotationNotFoundThenFieldNotFoundExceptionIsThrown()
            throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        await().until(
                fieldIn(repository).annotatedWith(ExampleAnnotation2.class).andWithName("value").andOfType(int.class),
                equalTo(1));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000, expected = FieldNotFoundException.class)
    public void givenAnnotationAndNameAndTypeWhenNameAndAnnotationMatchButTypeNotFoundThenFieldNotFoundExceptionIsThrown()
            throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        byte one = (byte) 1;
        await().until(
                fieldIn(repository).annotatedWith(ExampleAnnotation.class).andWithName("value").andOfType(byte.class),
                equalTo(one));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000, expected = FieldNotFoundException.class)
    public void givenAnnotationAndNameWhenAnnotationMatchButNameNotFoundThenFieldNotFoundExceptionIsThrown()
            throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        Object one = 1;
        await().until(fieldIn(repository).annotatedWith(ExampleAnnotation.class).andWithName("value2"), equalTo(one));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000, expected = FieldNotFoundException.class)
    public void givenAnnotationAndTypeWhenAnnotationMatchButTypeDoesntThenFieldNotFoundExceptionIsThrown()
            throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        byte one = (byte) 1;
        await().until(fieldIn(repository).annotatedWith(ExampleAnnotation.class).andOfType(byte.class), equalTo(one));
        assertEquals(1, repository.getValue());
    }

    @Test(timeout = 2000, expected = FieldNotFoundException.class)
    public void givenAnnotationAndTypeWhenTypeMatchButAnnotationDoesntThenFieldNotFoundExceptionIsThrown()
            throws Exception {
        FakeRepositoryWithAnnotation repository = new FakeRepositoryWithAnnotation();
        new Asynch(repository).perform();
        await().until(fieldIn(repository).annotatedWith(ExampleAnnotation2.class).andOfType(int.class), equalTo(1));
        assertEquals(1, repository.getValue());
    }

}
