# ![Awaitility](resources/Awaitility_logo_red_small.png)

Testing asynchronous systems is hard. Not only does it require handling threads, timeouts and concurrency issues, but the intent of the test code can be obscured by all these details. Awaitility is a DSL that allows you to express expectations of an asynchronous system in a concise and easy to read manner. For example:

```java
@Test
public void updatesCustomerStatus() throws Exception {
    // Publish an asynchronous event:
    publishEvent(updateCustomerStatusEvent);
    // Awaitility lets you wait until the asynchronous operation completes:
    await().atMost(5, SECONDS).until(costumerStatusIsUpdated());
    ...
}
```


## News

* 2014-07-03: [Awaitility 1.6.1](http://awaitility.googlecode.com/files/awaitility-1.6.1.zip) is released with support for [Condition Evaluation Listeners](doc/usage.md#condition-evaluation-listener) as well as various bug fixes. See [change log](changelog.txt) for more details.

* 2014-04-23: [Awaitility 1.6.0](http://awaitility.googlecode.com/files/awaitility-1.6.0.zip) is released with [support](doc/usage.md#example-8---using-assertj-or-fest-assert) for [AssertJ](http://joel-costigliola.github.io/assertj/) and [Fest Assert](https://code.google.com/p/fest/) assertions as well as better support for [Java 8](doc/usage.md#example-7---java-8). See [this blog](http://www.jayway.com/2014/04/23/java-8-and-assertj-support-in-awaitility-1-6-0/) for some examples and have a look at the [change log](changelog.txt) for more details.

* 2014-01-27: [Awaitility 1.5.0](http://dl.bintray.com/johanhaleby/generic/awaitility-1.5.0.zip) is released. See [change log](changelog.txt) for details.


## Documentation

* [Getting started](doc/getting-started.md)
* [User Guide](doc/usage.md)
* [Awaitility Javadoc](http://awaitility.googlecode.com/svn/tags/1.6.1/apidocs/com/jayway/awaitility/Awaitility.html)


## Founded by:

[![jayway](http://www.arctiquator.com/oppenkallkod/assets/images/jayway_logo.png)](http://www.jayway.com)
