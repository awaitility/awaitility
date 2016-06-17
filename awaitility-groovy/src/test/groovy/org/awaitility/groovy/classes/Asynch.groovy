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
package org.awaitility.groovy.classes

import java.util.concurrent.atomic.AtomicInteger

class Asynch {
  private final AtomicInteger atomic = new AtomicInteger();

  def Asynch perform() {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          Thread.sleep(600);
          atomic.set(1);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    });
    thread.start();
    return this;
  }

  def int getValue() {
    return atomic.get();
  }
}
