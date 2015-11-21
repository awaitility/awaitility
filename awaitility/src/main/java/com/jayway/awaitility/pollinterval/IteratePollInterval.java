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

package com.jayway.awaitility.pollinterval;

import com.jayway.awaitility.Duration;

public class IteratePollInterval implements PollInterval {

    private final Function function;
    private final Duration startDuration;

    public IteratePollInterval(Function function) {
        this(function, Duration.ONE_HUNDRED_MILLISECONDS);
    }

    public IteratePollInterval(Function function, Duration startDuration) {
        this.function = function;
        this.startDuration = startDuration;
    }

    public Duration next(int pollCount, Duration previousDuration) {
        return function.apply(previousDuration == null ? startDuration : previousDuration);
    }

    public interface Function {

        /**
         * Applies this function to the given argument.
         *
         * @param previousDuration The previous duration
         * @return The next duration
         */
        Duration apply(Duration previousDuration);
    }
}
