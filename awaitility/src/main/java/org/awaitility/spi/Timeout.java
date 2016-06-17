/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.awaitility.spi;

/**
 * Allow Awaitility extensions to define their own error messages. E.g. the groovy extension would publish
 * ugly error messages such as
 * <pre>
 *  Condition org.awaitility.groovy.AwaitilitySupport$1 was not fulfilled within 500 milliseconds.
 * </pre>
 * if it where not to set a specific message.
 */
public class Timeout {

    /** Constant <code>timeout_message="null"</code> */
    public static String timeout_message = null;
}
