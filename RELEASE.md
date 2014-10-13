
Release instructions
===============================================================================

1.  Update the change log indicating the date of the release
2.  Switch to Java 8
3.  Run `mvn release:prepare -Prelease`
4.  Run `mvn release:perform -Prelease`
5.  Log in to [Sonatype](https://oss.sonatype.org/).
6.  Follow the [Sonatype release directions](https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide) in bullet 8.
7.  Upload the artifacts to [bintray](http://bintray.com/).
8.  Upload the javadoc to googlecode:
    1.  Unzip the `awaitility-X-javadoc.jar` into `tags/X/apidocs`
    2.  Run `find . -name '*.html' | xargs svn propset svn:mime-type text/html`
    3.  Run `find . -name '*.css' | xargs svn propset svn:mime-type text/css`
    4.  `svn ci -m "Uploading javadocs for version X"`
9.  Update the front page and the getting started page.
10. Send a message to the mailing-list and twitter announcing the new release.

The release is automatically synced to Maven central (the sync process runs hourly).
