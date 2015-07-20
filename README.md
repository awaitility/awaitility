![Awaitility](resources/Awaitility_logo_red_small.png) 

[![Build Status](https://travis-ci.org/jayway/awaitility.svg)](https://travis-ci.org/jayway/awaitility)

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
 * 2015-07-20: Awaitility has moved to GitHub. From now on the old <a href="https://code.google.com/p/awaitility/">Google Code page</a> should not be used anymore. All issues are reported here on GitHub and the documentation is migrated.
 * 2014-11-28: [Awaitility 1.6.3](http://dl.bintray.com/johanhaleby/generic/awaitility-1.6.3.zip) is released. See [change log](http://github.com/jayway/awaitility/raw/master/changelog.txt) for details. 
 * 2014-10-15: [Awaitility 1.6.2](http://dl.bintray.com/johanhaleby/generic/awaitility-1.6.2.zip) is released with
 * improved support for [Condition Evaluation Listeners](https://github.com/jayway/awaitility/wiki/Usage#condition-evaluation-listener), [Groovy 2.3 Trait support](https://github.com/jayway/awaitility/wiki/Groovy#groovy-23-and-above) and [deadlock detection](https://github.com/jayway/awaitility/wiki/Usage#deadlock-detection). See [change log](http://github.com/jayway/awaitility/raw/master/changelog.txt) for more details.
 
[Older news](https://github.com/jayway/awaitility/wiki/OldNews)

## Documentation

* [Getting started](https://github.com/jayway/awaitility/wiki/Getting_started)
* [User Guide](https://github.com/jayway/awaitility/wiki/Usage)
* [Downloads](https://github.com/jayway/awaitility/wiki/Downloads)
* [Awaitility Javadoc](http://www.javadoc.io/doc/com.jayway.awaitility/awaitility/1.6.3)

## Links
* [Change log](https://github.com/jayway/awaitility/raw/master/changelog.txt)
* Awaitility on [Ohloh](https://www.ohloh.net/p/awaitility)
* [Mailing list](http://groups.google.com/group/awaitility) for questions and support

## Sponsored by:

[![jayway](http://www.arctiquator.com/oppenkallkod/assets/images/jayway_logo.png)](http://www.jayway.com)

[![Analytics](https://ga-beacon.appspot.com/UA-17489061-2/jayway/awaitility)](https://github.com/jayway/awaitility)
