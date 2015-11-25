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

/**
 * A poll interval represents how often Awaitility will pause before rechecking of the specified (Awaitility) condition evaluates to true.
 * Note that the naming "poll interval" is a bit misleading. It's actually more of a delay between two successive condition evaluations.
 * I.e if the condition evaluation takes 5 ms and a fixed poll interval of 100 ms is used then the next condition evaluation with happen at
 * (approximately) 105 ms.
 */
public interface PollInterval {

    /**
     * @param pollCount        The number of times the condition has been polled (evaluated)
     * @param previousDuration The duration of the previously returned poll interval
     * @return The duration of the next poll interval
     */
    Duration next(int pollCount, Duration previousDuration);
}
