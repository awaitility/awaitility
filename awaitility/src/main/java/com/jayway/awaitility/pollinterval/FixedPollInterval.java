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

import java.util.concurrent.TimeUnit;

public class FixedPollInterval implements PollInterval {

    private final Duration duration;

    public FixedPollInterval(Duration duration) {
        this.duration = duration;
    }

    public FixedPollInterval(long pollInterval, TimeUnit unit) {
        this(new Duration(pollInterval, unit));
    }

    public Duration next(int pollCount, Duration previousDuration) {
        return duration;
    }
}
