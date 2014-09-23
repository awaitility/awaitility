
Usage
===============================================================================

* For Scala documentation click [here](scala.md).
* For Groovy documentation click [here](groovy.md).


__Table of contents__

* [Static imports](#static-imports)
* [Usage examples](#usage-examples)
    - [Example 1 - Simple](#example-1---simple)
    - [Example 2 - Better reuse](#example-2---better-reuse)
    - [Example 3 - Proxy based conditions](#example-3---proxy-based-conditions)
    - [Example 4 - Fields](#example-4---fields)
    - [Example 5 - Atomic](#example-5---atomic)
    - [Example 6 - Advanced](#example-6---advanced)
    - [Example 7 - Java 8](#example-7---java-8)
    - [Example 8 - Using AssertJ or Fest Assert](#example-8---using-assertj-or-fest-assert)
* [Exception handling](#exception-handling)
* [Defaults](#defaults)
* [Polling](#polling)
* [Condition Evaluation Listener](#condition-evaluation-listener)
* [Important](#important)
* [Example of usage](#example-of-usage)


Static imports
-------------------------------------------------------------------------------

In order to use Awaitility effectively it's recommended to statically import the following methods from the Awaitility framework:

1. `com.jayway.awaitility.Awaitility.*`

It may also be useful to import these methods:

1. `com.jayway.awaitility.Duration.*`
2. `java.util.concurrent.TimeUnit.*`
3. `org.hamcrest.Matchers.*`
4. `org.junit.Assert.*`


Usage examples
-------------------------------------------------------------------------------

### Example 1 - Simple

Let's assume that we send a "add user" message to our asynchronous system like this:

~~~java
publish(new AddUserMessage("Awaitility Rocks"));
~~~

In your test case Awaitility can help you to easily verify that the database has been updated. In its simplest form it may look something like this:

~~~java
await().until(newUserIsAdded());
~~~

`newUserIsAdded is a method that you implement yourself in your test case. It specifies the condition that must be fulfilled in order for Awaitility to stop waiting.

~~~java
private Callable<Boolean> newUserIsAdded() {
      return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                  return userRepository.size() == 1; // The condition that must be fulfilled
            }
      };
}
~~~

By default Awaitility will wait for 10 seconds and if the size of the user respository is not equal to 1 during this time it'll throw a [ConditionTimeoutException](http://awaitility.googlecode.com/svn/tags/1.6.1/apidocs/com/jayway/awaitility/core/ConditionTimeoutException.html) failing the test. If you want a different timeout you can define it like this:

~~~java
await().atMost(5, SECONDS).until(newUserWasAdded());
~~~


### Example 2 - Better reuse

Awaitility also supports splitting up the condition into a supplying and a matching part for better reuse. The example above can also be written as:

~~~java
await().until( userRepositorySize(), equalTo(1) );
~~~

The `userRepositorySize` method is now a Callable of type Integer:

~~~java
private Callable<Integer> userRepositorySize() {
      return new Callable<Integer>() {
            public Integer call() throws Exception {
                  return userRepository.size(); // The condition supplier part
            }
      };
}
~~~

equalTo is a standard [Hamcrest](http://code.google.com/p/hamcrest/) matcher specifiying the matching part of the condition for Awaitility.

Now we could reuse the `userRepositorySize` in a different test. E.g. let's say we have a test that adds three users at the same time:

~~~java
publish(new AddUserMessage("User 1"), new AddUserMessage("User 2"), new AddUserMessage("User 3"));
~~~

We now reuse the `userRepositorySize` supplier and simply update the Hamcrest matcher:

~~~java
await().until( userRepositorySize(), equalTo(3) );
~~~


### Example 3 - Proxy based conditions

It's also possible to achieve the same result by building up the supplier part using a proxy:

~~~java
await().untilCall( to(userRepository).size(), equalTo(3) );
~~~

to is method in Awaitility which you can use to define the suppling part inline in the await statement. Thus you don't need to create a `Callable` yourself since it's done for you. Which option you like best depends on the use case and readability.


### Example 4 - Fields

You can also build the suppling part by referring to a field. E.g:

~~~java
await().until( fieldIn(object).ofType(int.class), equalTo(2) );
~~~

or:

~~~java
await().until( fieldIn(object).ofType(int.class).andWithName("fieldName"), equalTo(2) );
~~~

or:

~~~java
await().until( fieldIn(object).ofType(int.class).andAnnotatedWith(MyAnnotation.class), equalTo(2) );
~~~


### Example 5 - Atomic

If you're using [Atomic](http://download.oracle.com/javase/1,5.0/docs/api/java/util/concurrent/atomic/package-summary.html) structures Awaitility provides a simple way to wait until they match a specific value:

~~~java
AtomicInteger atomic = new AtomicInteger(0);
// Do some async stuff that eventually updates the atomic integer
await().untilAtomic(atomic, equalTo(1));
~~~

Waiting for an `AtomicBoolean` is even simpler:

~~~java
AtomicBoolean atomic = new AtomicBoolean(false);
// Do some async stuff that eventually updates the atomic boolean
await().untilTrue(atomic);
~~~


### Example 6 - Advanced

Use a poll interval of 100 milliseconds with an initial delay of 20 milliseconds until customer status is equal to "REGISTERED". This example also uses a named await by specifying an alias ("customer registration"). This makes it easy to find out which await statement that failed if you have multiple awaits in the same test.

~~~java
with().pollInterval(ONE_HUNDERED_MILLISECONDS).and().with().pollDelay(20, MILLISECONDS).await("customer registration").until(
            costumerStatus(), equalTo(REGISTERED));
~~~


### Example 7 - Java 8

If you're using Java 8 you can also use lambda expressions in your conditions:

~~~java
await().atMost(5, SECONDS).until(() -> userRepository.size() == 1);
~~~

Or method references:

~~~java
await().atMost(5, SECONDS).until(userRepository::isNotEmpty);
~~~

Or a combination of method references and Hamcrest matchers:

~~~java
await().atMost(5, SECONDS).until(userRepository::size, is(1));
~~~

For examples refer to the [Jayway team blog](http://www.jayway.com/2014/04/23/java-8-and-assertj-support-in-awaitility-1-6-0/).


### Example 8 - Using AssertJ or Fest Assert

Starting from version 1.6.1 you can use [AssertJ](http://joel-costigliola.github.io/assertj/) or [Fest Assert](https://code.google.com/p/fest/) as an alternative to Hamcrest (it's actually possible to use any third party library that throws exception on error). For example:

~~~java
await().atMost(5, SECONDS).until(() -> assertThat(fakeRepository.getValue()).isEqualTo(1));
~~~


Exception handling
-------------------------------------------------------------------------------

By default Awaitility catches uncaught exceptions in all threads and propagates them to the awaiting thread. This means that your test-case will indicate failure even if it's not the test-thread that threw the uncaught exception.


Defaults
-------------------------------------------------------------------------------

If you don't specify any timeout Awaitility will wait for 10 seconds and then throw a [ConditionTimeoutException](http://awaitility.googlecode.com/svn/tags/1.6.1/apidocs/com/jayway/awaitility/core/ConditionTimeoutException.html) if the condition has not been fulfilled. The default poll interval and poll delay is 100 milliseconds. You can also specify the default values yourself using:

~~~java
  Awaitility.setDefaultTimeout(..)
  Awaitility.setDefaultPollInterval(..)
  Awaitility.setDefaultPollDelay(..)
~~~

You can also reset back to the default values using `Awaitility.reset()`.


Polling
-------------------------------------------------------------------------------

Awaitility starts to check if the specified condition (the one you create using the Awaitility DSL) matches for the first time after a "poll delay" (the initial delay before the polling begins). By default Awaitility uses the same poll delay as poll interval which means that it checks the condition periodically first after the given poll delay, and subsequently with the given poll interval; that is conditions are checked after pollDelay then pollDelay+pollInterval, then pollDelay + 2 pollInterval, and so on. If you change the poll interval the poll delay will also change to match the specified poll interval unless you've specified a poll delay explicitly.

Note that since Awaitility uses polling to verify that a condition matches it's not recommended to use it for precise performance testing. In these cases it's better to use an AOP framework such as AspectJ's compile-time weaving.


Condition Evaluation Listener
-------------------------------------------------------------------------------

Awaitility 1.6.1 introduced the concept of [Condition Evaluation Listener](http://awaitility.googlecode.com/svn/tags/1.6.1/apidocs/com/jayway/awaitility/core/ConditionEvaluationListener.html). This can be used to get information each time a Hamcrest-based condition has been evaluated by Awaitility. You can for example use this to get the intermediate values of a condition before it is fulfilled. It can also be used for logging purposes. For example:

~~~java
with().
         conditionEvaluationListener(condition -> System.out.printf("%s (elapsed time %dms, remaining time %dms)\n", condition.getDescription(), condition.getElapsedTimeInMS(), condition.getRemainingTimeInMS())).
         await().atMost(Duration.TEN_SECONDS).until(new CountDown(5), is(equalTo(0)));
~~~

will print the following to the console:

~~~
    com.jayway.awaitility.AwaitilityJava8Test$CountDown expected (<0> or a value less than <0>) but was <5> (elapsed time 101ms, remaining time 1899ms)
    com.jayway.awaitility.AwaitilityJava8Test$CountDown expected (<0> or a value less than <0>) but was <4> (elapsed time 204ms, remaining time 1796ms)
    com.jayway.awaitility.AwaitilityJava8Test$CountDown expected (<0> or a value less than <0>) but was <3> (elapsed time 306ms, remaining time 1694ms)
    com.jayway.awaitility.AwaitilityJava8Test$CountDown expected (<0> or a value less than <0>) but was <2> (elapsed time 407ms, remaining time 1593ms)
    com.jayway.awaitility.AwaitilityJava8Test$CountDown expected (<0> or a value less than <0>) but was <1> (elapsed time 508ms, remaining time 1492ms)
    com.jayway.awaitility.AwaitilityJava8Test$CountDown reached its end value of (<0> or a value less than <0>) (elapsed time 610ms, remaining time 1390ms)
~~~

There's a built-in `ConditionEvaluationListener` for logging called [ConditionEvaluationLogger](http://awaitility.googlecode.com/svn/tags/1.6.1/apidocs/com/jayway/awaitility/core/ConditionEvaluationLogger.html) that you can use like this:

~~~java
with().conditionEvaluationListener(new ConditionEvaluationLogger()).await(). ...
~~~

Note that currently this only works for Hamcrest-based conditions.


Important
-------------------------------------------------------------------------------

Awaitility does nothing to ensure thread safety or thread synchronization! This is your responsibility! Make sure your code is correctly synchronized or that you are using thread safe data structures such as volatile fields or classes such as AtomicInteger and ConcurrentHashMap.


Example of usage
-------------------------------------------------------------------------------

1. [Awaitility test case](awaitility/src/test/java/com/jayway/awaitility/AwaitilityTest.java)
2. [Field supplier test case](awaitility/src/test/java/com/jayway/awaitility/UsingFieldSupplierTest.java)
3. [Atomic test case](awaitility/src/test/java/com/jayway/awaitility/UsingAtomicTest.java)
4. [Presentation](http://awaitility.googlecode.com/files/awaitility-khelg-2011.pdf) from [Jayway](http://www.jayway.com/)'s KHelg 2011
