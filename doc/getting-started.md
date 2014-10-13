
Getting Started
===============================================================================

Installation
-------------------------------------------------------------------------------

### Maven users

Add the following dependency to your pom.xml:


#### Java DSL

~~~xml
<dependency>
      <groupId>com.jayway.awaitility</groupId>
      <artifactId>awaitility</artifactId>
      <version>1.6.1</version>
      <scope>test</scope>
</dependency>
~~~


#### Scala DSL

Maven:

~~~xml
<dependency>
      <groupId>com.jayway.awaitility</groupId>
      <artifactId>awaitility-scala</artifactId>
      <version>1.6.1</version>
      <scope>test</scope>
</dependency>
~~~

SBT:

~~~sbt
val awaitility-scala = "com.jayway.awaitility" % "awaitility-scala" % "1.6.1"
~~~


#### Groovy DSL

Maven:

~~~xml
<dependency>
      <groupId>com.jayway.awaitility</groupId>
      <artifactId>awaitility-groovy</artifactId>
      <version>1.6.1</version>
      <scope>test</scope>
</dependency>
~~~

Grapes:

~~~grapes
@Grapes(
    @Grab(group='com.jayway.awaitility', module='awaitility-groovy', version='1.6.1')
)
~~~


### Non-maven users

Download [Awaitility](http://dl.bintray.com/johanhaleby/generic/awaitility-1.6.1.zip) and put it in your class-path. You may also need to download the [third-party dependencies](http://dl.bintray.com/johanhaleby/generic/awaitility-dependencies.zip) and put them in your classpath as well. Scala users must also download [awaitility-scala](http://dl.bintray.com/johanhaleby/generic/awaitility-scala-1.6.1.zip) and Groovy users must download [awaitility-groovy](http://dl.bintray.com/johanhaleby/generic/awaitility-groovy-1.6.1.zip).


Documentation
-------------------------------------------------------------------------------

When you've successfully downloaded and configured Awaitility in your classpath please refer to the [user guide](usage.md) for examples.
