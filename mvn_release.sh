#!/bin/sh
read -p "Enter the version to release: " releaseVersion
echo "Starting to release Awaitility $releaseVersion"

mvn release:prepare -Prelease -DautoVersionSubmodules=true -Dtag=awaitility-${releaseVersion} -DreleaseVersion=${releaseVersion} && \
mvn release:perform -Prelease

echo "Maven release of Awaitility $releaseVersion completed successfully"