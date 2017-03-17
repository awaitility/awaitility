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
package org.awaitility.groovy

import org.awaitility.core.ConditionTimeoutException
import org.awaitility.groovy.classes.Asynch
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import java.util.concurrent.Callable

import static java.util.concurrent.TimeUnit.MILLISECONDS
import static org.awaitility.Awaitility.await
import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertThat

class AwaitilityExtensionModuleTest {

  @Rule
  public def ExpectedException exception = ExpectedException.none()

  @Test
  def void groovyBooleanClosureSupport() {
    def asynch = new Asynch().perform()

    await().until { asynch.getValue() == 1 }
  }

  @Test
  def void groovyNonBooleanClosureSupport() {
    int calls = 0
    // using "false" to detect a buggy implementation making use of toBoolean(): "false".toBoolean() returns false
    Closure<String> stringClosure = { ++calls > 1 ? "false" : null }

    await().until(stringClosure)
    // e37a311 caused conditions to be evaluated twice
    assert calls == 2
  }

  @Test
  def void groovyRunnableSupport() {
    def asynch = new Asynch().perform()

    await().until(new Runnable() {
      @Override
      void run() {
        assertThat(asynch.getValue(), equalTo(1))
      }
    })
  }

  @Test
  def void groovyBooleanCallableSupport() {
    def asynch = new Asynch().perform()

    await().until(new Callable<Boolean>() {
      @Override
      Boolean call() {
        return asynch.getValue() == 1
      }
    })
  }

  @Test
  def void timeoutMessagesDoesntContainAnonymousClassDetails() {
    exception.expect ConditionTimeoutException
    exception.expectMessage "Condition was not fulfilled within 500 milliseconds"

    def asynch = new Asynch().perform()

    await().atMost(500, MILLISECONDS).until { asynch.getValue() == 2 }
  }

  @Test
  def void awaitWithAlias() {
    exception.expect ConditionTimeoutException
    exception.expectMessage "Condition with alias 'groovy' didn't complete within 500 milliseconds"

    def asynch = new Asynch().perform()

    await("groovy").atMost(500, MILLISECONDS).until { asynch.getValue() == 2 }
  }

  @Test
  def void untilAssertedTest() {
    def asynch = new Asynch().perform()

    await("groovy").atMost(2000, MILLISECONDS).untilAsserted { assertEquals(1, asynch.getValue()) }
  }

  @Test
  def void untilWithAssertionThrowsException() {
    exception.expect AssertionError

    def asynch = new Asynch().perform()
    await("groovy").atMost(2000, MILLISECONDS).until { assertEquals(2, asynch.getValue()) }
  }
}
