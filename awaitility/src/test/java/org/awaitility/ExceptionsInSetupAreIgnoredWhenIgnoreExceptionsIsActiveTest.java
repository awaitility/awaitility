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
import org.awaitility.classes.FakeRepository;
import org.awaitility.classes.FakeRepositoryImpl;
import org.awaitility.classes.ThrowExceptionUnlessFakeRepositoryEqualsOne;
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
        return new ThrowExceptionUnlessFakeRepositoryEqualsOne(fakeRepository, IllegalArgumentException.class);
    }

    @Test
    public void exceptionsInTestSetupAreIgnoredWhenIgnoringExceptions() {
        // nothing here, test logic sits in @BeforeClass method
    }
}
