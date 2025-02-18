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
import java.time.temporal.ChronoUnit;

/**
 * <p>Duration class.</p>
 */
public final class Durations {

    /**
     * Constant <code>FOREVER</code>
     */
    public static final Duration FOREVER = ChronoUnit.FOREVER.getDuration();

    /**
     * Constant <code>ONE_MILLISECOND</code>
     */
    public static final Duration ONE_MILLISECOND = Duration.ofMillis(1);

    /**
     * Constant <code>ONE_HUNDRED_MILLISECONDS</code>
     */
    public static final Duration ONE_HUNDRED_MILLISECONDS = Duration.ofMillis(100);
    /**
     * Constant <code>TWO_HUNDRED_MILLISECONDS</code>
     */
    public static final Duration TWO_HUNDRED_MILLISECONDS = Duration.ofMillis(200);
    /**
     * Constant <code>FIVE_HUNDRED_MILLISECONDS</code>
     */
    public static final Duration FIVE_HUNDRED_MILLISECONDS = Duration.ofMillis(500);
    /**
     * Constant <code>ONE_SECOND</code>
     */
    public static final Duration ONE_SECOND = Duration.ofSeconds(1);
    /**
     * Constant <code>TWO_SECONDS</code>
     */
    public static final Duration TWO_SECONDS = Duration.ofSeconds(2);
    /**
     * Constant <code>FIVE_SECONDS</code>
     */
    public static final Duration FIVE_SECONDS = Duration.ofSeconds(5);
    /**
     * Constant <code>TEN_SECONDS</code>
     */
    public static final Duration TEN_SECONDS = Duration.ofSeconds(10);
    /**
     * Constant <code>ONE_MINUTE</code>
     */
    public static final Duration ONE_MINUTE = Duration.ofMinutes(1);
    /**
     * Constant <code>TWO_MINUTES</code>
     */
    public static final Duration TWO_MINUTES = Duration.ofMinutes(2);
    /**
     * Constant <code>FIVE_MINUTES</code>
     */
    public static final Duration FIVE_MINUTES = Duration.ofMinutes(5);
    /**
     * Constant <code>TEN_MINUTES</code>
     */
    public static final Duration TEN_MINUTES = Duration.ofMinutes(10);

}
