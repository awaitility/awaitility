![Awaitility](resources/Awaitility_logo_red_small.png) 

[![Build Status](https://travis-ci.org/jayway/awaitility.svg)](https://travis-ci.org/jayway/awaitility)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jayway.awaitility/awaitility/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jayway.awaitility/awaitility)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/com.jayway.awaitility/awaitility/badge.svg)](http://www.javadoc.io/doc/com.jayway.awaitility/awaitility)

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
* 2015-11-27: [Awaitility 1.7.0](http://dl.bintray.com/johanhaleby/generic/awaitility-1.7.0.zip) is released with support for non-fixed [poll intervals](https://github.com/jayway/awaitility/wiki/Usage#polling). By default Awaitility ships with [fibonacci](https://github.com/jayway/awaitility/wiki/Usage#fibonacci-poll-interval), [iterative](https://github.com/jayway/awaitility/wiki/Usage#iterative-poll-interval) and ability to [roll your own](https://github.com/jayway/awaitility/wiki/Usage#custom-poll-interval). To simplify custom poll intervals [Duration](https://github.com/jayway/awaitility/wiki/Usage#duration) has been updated with methods such as `plus`, `minus`, `multiply` and `divide`. A new helper method called [matches](https://github.com/jayway/awaitility/wiki/Usage#example-10---checked-exceptions-in-runnable-lambda-expressions) has also been added to Awaitility to reduce boiler plate code when creating lambda expressions that call methods that throws exception. See [change log](http://github.com/jayway/awaitility/raw/master/changelog.txt) for details and see [this blog](http://code.haleby.se/2015/11/27/non-fixed-poll-intervals-in-awaitility/) for additional info.
* 2015-09-27: [Awaitility 1.6.5](http://dl.bintray.com/johanhaleby/generic/awaitility-1.6.5.zip) is released with support for [ignoring exceptions](https://github.com/jayway/awaitility/wiki/Usage#example-9---ignoring-exceptions). 
* 2015-09-14: [Awaitility 1.6.4](http://dl.bintray.com/johanhaleby/generic/awaitility-1.6.4.zip) is released. See [change log](http://github.com/jayway/awaitility/raw/master/changelog.txt) for details. 

[Older news](https://github.com/jayway/awaitility/wiki/OldNews)

## Documentation

* [Getting started](https://github.com/jayway/awaitility/wiki/Getting_started)
* [User Guide](https://github.com/jayway/awaitility/wiki/Usage)
* [Downloads](https://github.com/jayway/awaitility/wiki/Downloads)
* [Awaitility Javadoc](http://www.javadoc.io/doc/com.jayway.awaitility/awaitility/1.7.0)

## Links
* [Change log](https://github.com/jayway/awaitility/raw/master/changelog.txt)
* Awaitility on [Ohloh](https://www.ohloh.net/p/awaitility)
* [Mailing list](http://groups.google.com/group/awaitility) for questions and support