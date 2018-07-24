![Awaitility](resources/Awaitility_logo_red_small.png) 

[![Build Status](https://travis-ci.org/awaitility/awaitility.svg)](https://travis-ci.org/awaitility/awaitility)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.awaitility/awaitility/badge.svg)](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A"org.awaitility"%20AND%20a%3A"awaitility")
[![Javadoc](https://javadoc-badge.appspot.com/org.awaitility/awaitility.svg)](http://www.javadoc.io/doc/org.awaitility/awaitility)

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
* 2018-07-24: Awaitility `3.1.2` is released with a new `alias` method and many more [Kotlin](https://github.com/awaitility/awaitility/wiki/Kotlin) extension functions. See [changelog](https://raw.githubusercontent.com/awaitility/awaitility/master/changelog.txt) for details.
* 2018-06-29: Awaitility `3.1.1` is released with bug fixes, improvements and a [Kotlin](https://github.com/awaitility/awaitility/wiki/Kotlin) extension. See [changelog](https://raw.githubusercontent.com/awaitility/awaitility/master/changelog.txt) for details.
* 2018-03-02: Awaitility `3.1.0` is released with several improvements and changes. See [changelog](https://raw.githubusercontent.com/awaitility/awaitility/master/changelog.txt) for details.

[Older news](https://github.com/awaitility/awaitility/wiki/OldNews)

## Documentation

* [Getting started](https://github.com/awaitility/awaitility/wiki/Getting_started)
* [Usage Guide](https://github.com/awaitility/awaitility/wiki/Usage)
* [Downloads](https://github.com/awaitility/awaitility/wiki/Downloads)
* [Awaitility Javadoc](http://www.javadoc.io/doc/org.awaitility/awaitility/3.1.2)

## Links
* [Change log](https://github.com/awaitility/awaitility/raw/master/changelog.txt)
* Awaitility on [Ohloh](https://www.ohloh.net/p/awaitility)
* [Mailing list](http://groups.google.com/group/awaitility) for questions and support
