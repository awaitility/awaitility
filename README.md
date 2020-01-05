![Awaitility](resources/Awaitility_logo_red_small.png) 

[![Build Status](https://travis-ci.org/awaitility/awaitility.svg)](https://travis-ci.org/awaitility/awaitility)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.awaitility/awaitility/badge.svg)](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A"org.awaitility"%20AND%20a%3A"awaitility")
[![Javadoc](https://javadoc-badge.appspot.com/org.awaitility/awaitility.svg)](http://www.javadoc.io/doc/org.awaitility/awaitility)

Testing asynchronous systems is hard. Not only does it require handling threads, timeouts and concurrency issues, but the intent of the test code can be obscured by all these details. Awaitility is a DSL that allows you to express expectations of an asynchronous system in a concise and easy to read manner. For example:

```java
@Test
public void updatesCustomerStatus() {
    // Publish an asynchronous message to a broker (e.g. RabbitMQ):
    messageBroker.publishMessage(updateCustomerStatusMessage);
    // Awaitility lets you wait until the asynchronous operation completes:
    await().atMost(5, SECONDS).until(customerStatusIsUpdated());
    ...
}
```

## News
* 2020-01-03: Awaitility `4.0.2` is released. This release includes support for asserting that a condition is [maintained for specific duration](https://github.com/awaitility/awaitility/wiki/Usage#assert-that-a-value-is-maintained), improvments to [ConditionEvaluationListener](https://github.com/awaitility/awaitility/wiki/Usage#condition-evaluation-listener) as well as several bug fixes and other improvements. See [changelog](https://raw.githubusercontent.com/awaitility/awaitility/master/changelog.txt) for details.
* 2019-09-06: Awaitility `4.0.1` is released and it fixes a regression issue in which the condition evaluation duration could be evaluated to a negative number of nanoseconds on Windows. It also drops the dependency to [objenesis](http://objenesis.org/) since it's no longer used after moving to Java 8. See [changelog](https://raw.githubusercontent.com/awaitility/awaitility/master/changelog.txt) for details.
* 2019-08-30: Awaitility `4.0.0` is released! Java 8+ is now required which allows for both dependency and API updates. See [release notes](https://github.com/awaitility/awaitility/wiki/ReleaseNotes40) and [changelog](https://raw.githubusercontent.com/awaitility/awaitility/master/changelog.txt) for details.

[Older news](https://github.com/awaitility/awaitility/wiki/OldNews)

## Documentation

* [Getting started](https://github.com/awaitility/awaitility/wiki/Getting_started)
* [Usage Guide](https://github.com/awaitility/awaitility/wiki/Usage)
* [Downloads](https://github.com/awaitility/awaitility/wiki/Downloads)
* [Awaitility Javadoc](http://www.javadoc.io/doc/org.awaitility/awaitility/4.0.2)

## Links
* [Change log](https://github.com/awaitility/awaitility/raw/master/changelog.txt)
* Awaitility on [Ohloh](https://www.ohloh.net/p/awaitility)
* [Mailing list](http://groups.google.com/group/awaitility) for questions and support

<a href="https://www.buymeacoffee.com/johanhaleby" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/arial-blue.png" alt="Buy Me A Coffee" style="height: 42px !important;width: 180px !important;" height="42px" width="180px"></a>
