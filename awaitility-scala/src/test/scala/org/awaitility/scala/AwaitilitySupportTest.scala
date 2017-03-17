/*
 * Copyright 2016 the original author or authors.
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
package org.awaitility.scala

import java.util.concurrent.TimeUnit.MILLISECONDS

import org.awaitility.Awaitility._
import org.awaitility.core.ConditionTimeoutException
import org.hamcrest.CoreMatchers.is
import org.hamcrest.Matchers.{containsString, endsWith, startsWith}
import org.hamcrest.{CoreMatchers, Matchers}
import org.junit.Assert._
import org.junit._

@Test
class AwaitilitySupportTest extends AwaitilitySupport {

  @Test
  def functionAsCondition() = {
    val c1 = new Counter()
    val c2 = new Counter()

    await until { c1.count() + c2.count() > 3 }
    await until { isDone() }
    await until isDone
  }

  @Test(expected = classOf[ConditionTimeoutException])
  def timeout() = {
    await atMost(500, MILLISECONDS) until { 2 == 1 }
  }

  @Test
  def awaitWithAlias() = {
    try {
      await("scala") atMost(500, MILLISECONDS) until { 2 == 1 }
      fail("Expected timeout exception")
    } catch {
        case e : ConditionTimeoutException =>
          assertEquals("Condition with alias 'scala' didn't complete within 500 milliseconds because condition was not fulfilled.", e getMessage)
    }
  }

  @Test
  def functionAsSupplierWithMatcher() {
    val c1 = new Counter()
    val c2 = new Counter()

    await until (c1.count() + c2.count(),  is(6))
    await until (isDone(), is(java.lang.Boolean.TRUE))
    await until (isDone, CoreMatchers is java.lang.Boolean.TRUE )
  }

  @Test
  def awaitWithAliasSupplierAndMatcher() = {
    try {
      await("scala") atMost(500, MILLISECONDS) until (2 == 1, is(java.lang.Boolean.TRUE))
      fail("Expected timeout exception")
    } catch {
        case e : ConditionTimeoutException =>
          assertThat(e getMessage, startsWith("Condition with alias 'scala' didn't complete within 500 milliseconds because"))
          assertThat(e getMessage, endsWith(" expected <true> but was <false>."))
    }
  }

  @Test
  def functionAsRunnable() {
    val c1 = new Counter()
    val c2 = new Counter()

    await untilAsserted { assertThat(c1.count() + c2.count(),  is(6)) }
    await untilAsserted { assertThat(isDone(), is(true)) }
    await untilAsserted { assertThat(isDone, CoreMatchers is true)  }
  }

  @Test
  def awaitWithAliasAndRunnable() = {
    try {
      await("scala") atMost(500, MILLISECONDS) untilAsserted { assertThat(2 == 1, is(true))}
      fail("Expected timeout exception")
    } catch {
        case e : ConditionTimeoutException =>
          assertThat(e getMessage, startsWith("Condition with alias 'scala' didn't complete within 500 milliseconds because"))
          assertThat(e getMessage, containsString("Expected: is <true>"))
          assertThat(e getMessage, endsWith("but: was <false>."))
    }
  }

  class Counter {
      var value = 0
      def count() = {
        value = value + 1
        value
      }
    }

    def isDone() : Boolean = true
}
