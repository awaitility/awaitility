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

import com.jayway.awaitility.core.ConditionTimeoutException
import com.jayway.awaitility.groovy.classes.Asynch
import spock.lang.Specification

import static com.jayway.awaitility.Awaitility.await
import static java.util.concurrent.TimeUnit.MILLISECONDS
import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat

class AwaitilityExtensionModuleGroovyTest extends Specification {

  def asynch = new Asynch()

  def "groovy closure support should work"() {
    when: asynch.perform()
    then: await().until { asynch.getValue() == 1 }
  }

  def "groovy closure runnable should work in spock"() {
    when: asynch.perform()
    then: await().until(new Runnable() {

      @Override
      void run() {
        assertThat(asynch.getValue(), equalTo(1))
      }
    });
  }

  def "timeout messages shouldn't contain anonymous class details"() {
    when:
    asynch.perform()
    await().atMost(500, MILLISECONDS).until { asynch.getValue() == 2 }

    then:
    ConditionTimeoutException e = thrown()
    e.message == "Condition was not fulfilled within 500 milliseconds."
  }

  def "await alias should be preserved in timeout messages"() {
    when:
    asynch.perform()
    await("groovy").atMost(500, MILLISECONDS).until { asynch.getValue() == 2 }

    then:
    ConditionTimeoutException e = thrown()
    e.message == "Condition with alias 'groovy' didn't complete within 500 milliseconds because condition was not fulfilled."
  }
}
