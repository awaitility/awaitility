
Groovy API
===============================================================================

We have also added a Groovy API that allows you to write

~~~groovy
await().until { numberOfReceivedMessages() > 3 }
~~~

To make this work you simply extend or mixin the AwaitilitySupport class that is provided in the awaitility-groovy module. Example:

~~~groovy
@Mixin(AwaitilitySupport)
class AwaitilitySupportTest {

  @Test
  def void groovyClosureSupport()  {
    ...

    await().until { numberOfReceivedMessages() > 3 }
  }
}
~~~

Use the following Maven dependency:

~~~xml
<dependency>
    <groupId>com.jayway.awaitility</groupId>
    <artifactId>awaitility-groovy</artifactId>
    <version>${awaitility.version}</version>
</dependency>
~~~


Example
-------------------------------------------------------------------------------

1. [Groovy example](awaitility-groovy/src/test/groovy/com/jayway/awaitility/groovy/AwaitilitySupportTest.groovy)
