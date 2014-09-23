
Scala API
===============================================================================

We have also added a Scala API that allows you to write

~~~scala
await until { numberOfReceivedMessages() > 3 }
~~~

What you actually provide is a no argument function that returns a `scala.Boolean`. To make this work you simply mixin the `AwaitilitySupport` trait that is provided in the awaitility-scala module. Example:

~~~scala
class MyTest extends AwaitilitySupport {
  @Test
  def functionAsCondition() = {
    await until { someCounter() > 3 }
    await until { isDone() }
    await until isDone
  }
  def isDone() : Boolean = {
    // implementation goes here
  }
  def someCounter() : Int = {
    // implementation goes here
  }
// ...
}
~~~

Use the following Maven dependency:

~~~xml
<dependency>
    <groupId>com.jayway.awaitility</groupId>
    <artifactId>awaitility-scala</artifactId>
    <version>${awaitility.version}</version>
</dependency>
~~~


Example
-------------------------------------------------------------------------------

1. [Scala example](awaitility-scala/src/test/scala/com/jayway/awaitility/scala/AwaitilitySupportTest.scala)
