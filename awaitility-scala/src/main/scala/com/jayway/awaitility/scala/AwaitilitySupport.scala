package com.jayway.awaitility.scala

import java.util.concurrent.Callable

trait AwaitilitySupport {
	implicit def function0ToCallable(function : Function0[scala.Boolean]) : Callable[java.lang.Boolean] = {
		new Callable[java.lang.Boolean] {
			def call() : java.lang.Boolean = function.apply()
		}
	}
}


