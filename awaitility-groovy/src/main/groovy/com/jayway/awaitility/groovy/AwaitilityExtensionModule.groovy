/*
* Copyright 2016 the original author or authors.
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

import com.jayway.awaitility.core.AssertionCondition
import com.jayway.awaitility.core.ConditionFactory

import java.util.concurrent.Callable

import static com.jayway.awaitility.spi.Timeout.timeout_message

class AwaitilityExtensionModule {

  static {
    timeout_message = "Condition was not fulfilled"
  }

  static def until(ConditionFactory self, Runnable runnable) {
    if (runnable instanceof Callable)
      self.until(runnable as Callable)
    else
      self.until(new AssertionCondition(runnable, self.generateConditionSettings()))
    // Return true to signal that everything went OK (for spock tests)
    return true
  }

}
