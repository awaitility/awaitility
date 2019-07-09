/*
 * Copyright 2019 the original author or authors.
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

package org.awaitility.core;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class DurationFactory {

    public static Duration of(long amount, TimeUnit timeUnit) {
        final ChronoUnit chronoUnit;
        switch (timeUnit) {
            case NANOSECONDS:
                chronoUnit = ChronoUnit.NANOS;
                break;
            case MICROSECONDS:
                chronoUnit = ChronoUnit.MICROS;
                break;
            case MILLISECONDS:
                chronoUnit = ChronoUnit.MILLIS;
                break;
            case SECONDS:
                chronoUnit = ChronoUnit.SECONDS;
                break;
            case MINUTES:
                chronoUnit = ChronoUnit.MINUTES;
                break;
            case HOURS:
                chronoUnit = ChronoUnit.HOURS;
                break;
            case DAYS:
                chronoUnit = ChronoUnit.DAYS;
                break;
            default:
                throw new IllegalStateException("Cannot convert " + TimeUnit.class.getSimpleName() + " to a " + ChronoUnit.class.getSimpleName());
        }
        return Duration.of(amount, chronoUnit);
    }
}