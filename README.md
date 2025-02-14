![Awaitility](resources/Awaitility_logo_red_small.png) 

[![Build Status](https://github.com/awaitility/awaitility/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/awaitility/awaitility/actions/workflows/ci.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.awaitility/awaitility/badge.svg)](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A"org.awaitility"%20AND%20a%3A"awaitility")

Testing asynchronous systems is hard. Not only does it require handling threads, timeouts, and concurrency issues, but the intent of the test code can be obscured by all these details. Awaitility is a DSL that allows you to express expectations of an asynchronous system in a concise and easy-to-read manner. For example:

```java
@Test
public void updatesCustomerStatus() {
    // Publish an asynchronous message to a broker (e.g. RabbitMQ):
    messageBroker.publishMessage(updateCustomerStatusMessage);
    // Awaitility lets you wait until the asynchronous operation completes:
    await().atMost(Duration.ofSeconds(5)).until(customerStatusIsUpdated());
    ...
}
```

## News
* 2024-08-07: Awaitility `4.2.2` is released with support for "ea" JVM versions. See [changelog](https://raw.githubusercontent.com/awaitility/awaitility/master/changelog.txt) for details.
* 2024-03-15: Awaitility `4.2.1` is released. It allows for easier use of logging and some bug fixes and improvements. See [changelog](https://raw.githubusercontent.com/awaitility/awaitility/master/changelog.txt) for details. 
* 2022-03-04: Awaitility `4.2.0` is released. It allows the use of assertion libraries such as Hamcrest or Assertj in [fail-fast conditions](https://github.com/awaitility/awaitility/wiki/Usage#fail-fast-conditions) as well as various improvements and bug fixes. See [changelog](https://raw.githubusercontent.com/awaitility/awaitility/master/changelog.txt) for details.

[Older news](https://github.com/awaitility/awaitility/wiki/OldNews)

## Documentation

* [Getting started](https://github.com/awaitility/awaitility/wiki/Getting_started)
* [Usage Guide](https://github.com/awaitility/awaitility/wiki/Usage)
* [Awaitility Javadoc](http://www.javadoc.io/doc/org.awaitility/awaitility/4.2.2)

## Links
* [Change log](https://github.com/awaitility/awaitility/raw/master/changelog.txt)
* Awaitility on [Open Hub](https://www.openhub.net/p/awaitility)
* [Mailing list](http://groups.google.com/group/awaitility) for questions and support

<a href="https://www.buymeacoffee.com/johanhaleby" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/arial-blue.png" alt="Buy Me A Coffee" style="height: 42px !important;width: 180px !important;" height="42px" width="180px"></a>
