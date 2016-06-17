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
* 2016-06-17: [Awaitility 2.0.0](http://dl.bintray.com/johanhaleby/generic/awaitility-2.0.0.zip) is released with support for [at least](https://github.com/awaitility/awaitility/wiki/Usage#example-11---at-least) expressions as well as upgraded [Groovy](https://github.com/awaitility/awaitility/wiki/Groovy) and [Scala](https://github.com/awaitility/awaitility/wiki/Scala) support. See [release notes](https://github.com/awaitility/awaitility/wiki/ReleaseNotes20) for more details.
* 2016-06-17: Awaitility has a new website, [http://www.awaitility.org](http://www.awaitility.org). Feel free to update your bookmarks. 
* 2015-11-27: [Awaitility 1.7.0](http://dl.bintray.com/johanhaleby/generic/awaitility-1.7.0.zip) is released with support for non-fixed [poll intervals](https://github.com/awaitility/awaitility/wiki/Usage#polling). By default Awaitility ships with [fibonacci](https://github.com/awaitility/awaitility/wiki/Usage#fibonacci-poll-interval), [iterative](https://github.com/awaitility/awaitility/wiki/Usage#iterative-poll-interval) and ability to [roll your own](https://github.com/awaitility/awaitility/wiki/Usage#custom-poll-interval). To simplify custom poll intervals [Duration](https://github.com/awaitility/awaitility/wiki/Usage#duration) has been updated with methods such as `plus`, `minus`, `multiply` and `divide`. A new helper method called [matches](https://github.com/awaitility/awaitility/wiki/Usage#example-10---checked-exceptions-in-runnable-lambda-expressions) has also been added to Awaitility to reduce boiler plate code when creating lambda expressions that call methods that throws exception. See [change log](http://github.com/awaitility/awaitility/raw/master/changelog.txt) for details and see [this blog](http://code.haleby.se/2015/11/27/non-fixed-poll-intervals-in-awaitility/) for additional info.

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
