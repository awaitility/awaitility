package com.jayway.awaitility.scala

import org.junit._
import Assert._
import com.jayway.awaitility.Awaitility._;
import org.hamcrest.Matchers._;

@Test
class AwaitilitySupportTest extends AwaitilitySupport {
	class Counter {
		var value = 0
		def count() = {
			value = value + 1
			value
		}
	}

    @Test
    def functionAsCondition() = {
		val c1 = new Counter()
		val c2 = new Counter()
//		await().until( () => c1.count() + c2.count() > 3)
		await until { c1.count() + c2.count() > 3 }
		await until { isDone() }
		await until isDone 
	}
	
	def isDone() : Boolean = true

	var c = 0
	def count() = {
		c = c + 1
		c
	}
}
