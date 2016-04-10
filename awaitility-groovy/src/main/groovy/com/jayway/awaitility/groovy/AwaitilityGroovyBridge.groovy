/*
* Copyright 2014 the original author or authors.
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
package com.jayway.awaitility.groovy

import com.jayway.awaitility.core.ConditionFactory

import java.util.concurrent.Callable

import static com.jayway.awaitility.spi.Timeout.timeout_message

/**
 * Previously used to enhance Awaitility to allow for Groovy closures.
 *
 * @deprecated This implementation has a big flaw. When not used for all test classes, some test cases might or might not fail, depending on execution order.
 * Since version 1.8.0 it's no longer required to explicitly bootstrap Awaitility in Groovy.
 * @see AwaitilityExtensionModule
 */
@Deprecated
class AwaitilityGroovyBridge {

  static void initializeAwaitilityGroovySupport() {
    timeout_message = "Condition was not fulfilled"

    def originalMethod = ConditionFactory.metaClass.getMetaMethod("until", Runnable.class)
    ConditionFactory.metaClass.until { Runnable runnable ->
      if (runnable instanceof Closure) {
        delegate.until(runnable as Callable<Boolean>)
      } else {
        originalMethod.invoke(delegate, runnable)
      }
      // Return true to signal that everything went OK (for spock tests)
      true
    }
  }
}
