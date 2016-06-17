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

package org.awaitility.pollinterval;

import org.awaitility.Duration;

/**
 * A poll interval represents how often Awaitility will pause before reevaluating the supplied condition.
 * <p/>
 * Note that the name "poll interval" is a bit misleading. It's actually a delay between two successive condition evaluations.
 * I.e if the condition evaluation takes 5 ms and a fixed poll interval of 100 ms is used then the next condition evaluation will happen at
 * (approximately) 105 ms. It's called <tt>PollInterval</tt> for historic reasons.
 *
 * @since 1.7.0
 */
public interface PollInterval {

    /**
     * Generate the next poll interval ({@link Duration}) based on the previous {@link Duration} and/or the <code>poll count</code>.
     * The first time the poll interval is called the poll delay is used as <code>previousDuration</code>. By default the poll delay is
     * equal to {@link Duration#ZERO}.
     *
     * @param pollCount        The number of times the condition has been polled (evaluated). Always a positive integer.
     * @param previousDuration The duration of the previously returned poll interval.
     * @return The duration of the next poll interval
     */
    Duration next(int pollCount, Duration previousDuration);
}
