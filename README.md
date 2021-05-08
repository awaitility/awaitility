![Awaitility](resources/Awaitility_logo_red_small.png) 

[![Build Status](https://travis-ci.com/awaitility/awaitility.svg?branch=master)](https://travis-ci.com/awaitility/awaitility)
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
* 2021-05-08: Awaitility `4.1.0` is released. This release includes [fail-fast conditions](https://github.com/awaitility/awaitility/wiki/Usage#fail-fast-conditions) as well as some bug fixes and dependency updates. See [changelog](https://raw.githubusercontent.com/awaitility/awaitility/master/changelog.txt) for details.
* 2020-05-19: Awaitility `4.0.3` is released. This release includes updates to [ConditionEvaluationLogger](https://github.com/awaitility/awaitility/wiki/Usage#condition-evaluation-listener) as well as several depdency updates.
If you're using the Groovy DSL beaware that Groovy has been upgraded from 2.x to 3.x. See [changelog](https://raw.githubusercontent.com/awaitility/awaitility/master/changelog.txt) for details.
* 2020-01-03: Awaitility `4.0.2` is released. This release includes support for asserting that a condition is [maintained for specific duration](https://github.com/awaitility/awaitility/wiki/Usage#assert-that-a-value-is-maintained), improvments to [ConditionEvaluationListener](https://github.com/awaitility/awaitility/wiki/Usage#condition-evaluation-listener) as well as several bug fixes and other improvements. See [changelog](https://raw.githubusercontent.com/awaitility/awaitility/master/changelog.txt) for details.

[Older news](https://github.com/awaitility/awaitility/wiki/OldNews)

## Documentation

* [Getting started](https://github.com/awaitility/awaitility/wiki/Getting_started)
* [Usage Guide](https://github.com/awaitility/awaitility/wiki/Usage)
* [Downloads](https://github.com/awaitility/awaitility/wiki/Downloads)
* [Awaitility Javadoc](http://www.javadoc.io/doc/org.awaitility/awaitility/4.1.0)

## Links
* [Change log](https://github.com/awaitility/awaitility/raw/master/changelog.txt)
* Awaitility on [Ohloh](https://www.ohloh.net/p/awaitility)
* [Mailing list](http://groups.google.com/group/awaitility) for questions and support

<a href="https://www.buymeacoffee.com/johanhaleby" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/arial-blue.png" alt="Buy Me A Coffee" style="height: 42px !important;width: 180px !important;" height="42px" width="180px"></a>
