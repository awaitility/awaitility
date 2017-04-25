/*
 * Copyright 2010 the original author or authors.
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
package org.awaitility.classes;

import org.awaitility.core.CheckedExceptionRethrower;

import java.lang.reflect.Constructor;
import java.util.concurrent.Callable;

public class ThrowExceptionUnlessFakeRepositoryEqualsOne implements Callable<Boolean> {

    private final FakeRepository repository;
    private final Class<? extends Throwable> throwable;

    public ThrowExceptionUnlessFakeRepositoryEqualsOne(FakeRepository repository, Class<? extends Throwable> throwable) {
        super();
        this.repository = repository;
        this.throwable = throwable;
    }

    public Boolean call() {
        if (repository.getValue() != 1) {
            Throwable throwable;
            try {
                Constructor<? extends Throwable> constructor = this.throwable.getDeclaredConstructor(String.class);
                constructor.setAccessible(true);
                throwable = constructor.newInstance("Repository value is not 1");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            CheckedExceptionRethrower.safeRethrow(throwable);
        }
        return true;
    }
}

