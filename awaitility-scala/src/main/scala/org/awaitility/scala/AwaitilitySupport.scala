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

import java.util.concurrent.Callable

import org.awaitility.core.ThrowingRunnable
import org.awaitility.spi.Timeout._

trait AwaitilitySupport {
  timeout_message = "Condition was not fulfilled"

  implicit def byNameFunctionToCallableOfType[T](function: => T): Callable[T] = {
    new Callable[T] {
      def call(): T = function
    }
  }

  implicit def byNameFunctionToCallableOfBoolean(function: => scala.Boolean): Callable[java.lang.Boolean] = {
    new Callable[java.lang.Boolean] {
      def call(): java.lang.Boolean = function
    }
  }

  implicit def byNameFunctionToRunnable[T](function: => T): ThrowingRunnable = {
    new ThrowingRunnable {
      override def run(): Unit = function
    }
  }
}


