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
package com.jayway.awaitility.classes;

import java.util.concurrent.Callable;

public class ThrowExceptionUnlessFakeRepositoryEqualsOne implements Callable<Boolean> {

    private final FakeRepository repository;

    public ThrowExceptionUnlessFakeRepositoryEqualsOne(FakeRepository repository) {
        super();
        this.repository = repository;
    }

    public Boolean call() {
        if (repository.getValue() != 1) {
            throw new RuntimeException("Repository value is not 1");
        }
        return true;
    }
}

