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
package com.jayway.awaitility.groovy

import com.jayway.awaitility.groovy.classes.Asynch
import org.junit.Test

// @Mixin(AwaitilitySupport)
class AwaitilitySupportTest extends AwaitilitySupport {

  @Test
  def void groovyClosureSupport() throws Exception {
    def asynch = new Asynch().perform()

    await().until { asynch.getValue() == 1 }
  }
}
