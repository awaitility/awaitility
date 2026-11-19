#!/bin/sh
# The release version is taken from the pom (the current -SNAPSHOT version without the suffix),
# so a typo at a prompt can't publish the wrong version.
snapshotVersion=$(mvn -q -N org.apache.maven.plugins:maven-help-plugin:3.5.1:evaluate -Dexpression=project.version -DforceStdout)
case "$snapshotVersion" in
  *-SNAPSHOT) releaseVersion="${snapshotVersion%-SNAPSHOT}" ;;
  *) echo "Expected a -SNAPSHOT version in pom.xml but found '${snapshotVersion}'. Aborting."; exit 1 ;;
esac
if [ -z "$releaseVersion" ]; then
  echo "Could not read the project version from pom.xml. Aborting."
  exit 1
fi
read -p "The pom says ${snapshotVersion}, so this will release Awaitility ${releaseVersion}. Continue? [y/N] " answer
case "$answer" in
  y|Y) ;;
  *) echo "Release aborted."; exit 1 ;;
esac
echo "Starting to release Awaitility $releaseVersion"
mvn versions:set -DnewVersion="${releaseVersion}" && mvn clean install -Prelease -DskipTests=true && \
git reset HEAD --hard && git clean -df && \
mvn release:prepare -Prelease -DautoVersionSubmodules=true -Dtag=awaitility-"${releaseVersion}" -DreleaseVersion="${releaseVersion}" release:perform -Prelease
echo "Maven release of Awaitility $releaseVersion completed successfully"
