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
package org.awaitility;

import org.awaitility.core.ForeverDuration;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;


/**
 * <p>Duration class.</p>
 */
public final class Durations {

    /**
     * Constant <code>FOREVER</code>
     */
    public static final Duration FOREVER = ForeverDuration.FOREVER;

    /**
     * Constant <code>ONE_MILLISECOND</code>
     */
    public static final Duration ONE_MILLISECOND = Duration.of(1, MILLIS);

    /**
     * Constant <code>ONE_HUNDRED_MILLISECONDS</code>
     */
    public static final Duration ONE_HUNDRED_MILLISECONDS = Duration.of(100, MILLIS);
    /**
     * Constant <code>TWO_HUNDRED_MILLISECONDS</code>
     */
    public static final Duration TWO_HUNDRED_MILLISECONDS = Duration.of(200, MILLIS);
    /**
     * Constant <code>FIVE_HUNDRED_MILLISECONDS</code>
     */
    public static final Duration FIVE_HUNDRED_MILLISECONDS = Duration.of(500, MILLIS);
    /**
     * Constant <code>ONE_SECOND</code>
     */
    public static final Duration ONE_SECOND = Duration.of(1, SECONDS);
    /**
     * Constant <code>TWO_SECONDS</code>
     */
    public static final Duration TWO_SECONDS = Duration.of(2, SECONDS);
    /**
     * Constant <code>FIVE_SECONDS</code>
     */
    public static final Duration FIVE_SECONDS = Duration.of(5, SECONDS);
    /**
     * Constant <code>TEN_SECONDS</code>
     */
    public static final Duration TEN_SECONDS = Duration.of(10, SECONDS);
    /**
     * Constant <code>ONE_MINUTE</code>
     */
    public static final Duration ONE_MINUTE = Duration.of(60, SECONDS);
    /**
     * Constant <code>TWO_MINUTES</code>
     */
    public static final Duration TWO_MINUTES = Duration.of(120, SECONDS);
    /**
     * Constant <code>FIVE_MINUTES</code>
     */
    public static final Duration FIVE_MINUTES = Duration.of(300, SECONDS);
    /**
     * Constant <code>TEN_MINUTES</code>
     */
    public static final Duration TEN_MINUTES = Duration.of(600, SECONDS);

}
