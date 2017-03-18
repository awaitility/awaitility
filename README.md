![Awaitility](resources/Awaitility_logo_red_small.png) 

[![Build Status](https://travis-ci.org/awaitility/awaitility.svg)](https://travis-ci.org/awaitility/awaitility)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.awaitility/awaitility/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.awaitility/awaitility)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/org.awaitility/awaitility/badge.svg)](http://www.javadoc.io/doc/org.awaitility/awaitility)

Testing asynchronous systems is hard. Not only does it require handling threads, timeouts and concurrency issues, but the intent of the test code can be obscured by all these details. Awaitility is a DSL that allows you to express expectations of an asynchronous system in a concise and easy to read manner. For example:

```java
@Test
public void updatesCustomerStatus() throws Exception {
    // Publish an asynchronous event:
    publishEvent(updateCustomerStatusEvent);
    // Awaitility lets you wait until the asynchronous operation completes:
    await().atMost(5, SECONDS).until(customerStatusIsUpdated());
    ...
}
```

## News
* 2017-03-17: Awaitility `3.0.0-rc1` is released. It includes several bug fixes and some major (non-backward compatible) changes: 
  
   1. The proxy creation is now done by bytebuddy instead of cglib and this has been extracted to its own project called `awaitility-proxy`. This means that if you need to use proxies depend on this project in addition to vanilla `awaitility`:
        ```xml    
        <dependency>
            <groupId>org.awaitility</groupId>
            <artifactId>awaitility-proxy</artifactId>
            <version>3.0.0-rc1</version>
        </dependency>
        ``` 
       
        You create a proxy when you do:
    
        ```java
        await().untilCall( to(someObject).someMethod(), is(4) );
        ```
      
        The `to` method has been moved to "org.awaitility.proxy.AwaitilityClassProxy" in the `awaitility-proxy` project.
  2. The `until(Runnable ...)` method in `org.awaitility.core.ConditionFactory` is renamed to `untilAsserted(Runnable ...)`. This means that if you previously did:
    
        ```java
        await().until(() -> assertThat(something()).isEqualTo("Something"));
        ```   

        you now need to do:

        ```java
        await().untilAsserted(() -> assertThat(something()).isEqualTo("Something"));
        ```
 
    To use it depend on:
        
    ```xml    
    <dependency>
        <groupId>org.awaitility</groupId>
        <artifactId>awaitility</artifactId>
        <version>3.0.0-rc1</version>
    </dependency>
    ``` 
    
  **Please help out and test it and provide feedback** 
* 2016-06-17: [Awaitility 2.0.0](http://dl.bintray.com/johanhaleby/generic/awaitility-2.0.0.zip) is released with support for [at least](https://github.com/awaitility/awaitility/wiki/Usage#example-11---at-least) expressions as well as upgraded [Groovy](https://github.com/awaitility/awaitility/wiki/Groovy) and [Scala](https://github.com/awaitility/awaitility/wiki/Scala) support. See [release notes](https://github.com/awaitility/awaitility/wiki/ReleaseNotes20) for more details.
* 2016-06-17: Awaitility has a new website, [http://www.awaitility.org](http://www.awaitility.org). Feel free to update your bookmarks. 

[Older news](https://github.com/awaitility/awaitility/wiki/OldNews)

## Documentation

* [Getting started](https://github.com/awaitility/awaitility/wiki/Getting_started)
* [Usage Guide](https://github.com/awaitility/awaitility/wiki/Usage)
* [Downloads](https://github.com/awaitility/awaitility/wiki/Downloads)
* [Awaitility Javadoc](http://www.javadoc.io/doc/org.awaitility/awaitility/2.0.0)

## Links
* [Change log](https://github.com/awaitility/awaitility/raw/master/changelog.txt)
* Awaitility on [Ohloh](https://www.ohloh.net/p/awaitility)
* [Mailing list](http://groups.google.com/group/awaitility) for questions and support
