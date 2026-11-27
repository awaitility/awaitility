#!/usr/bin/env bash
# Releases Awaitility to Maven Central. See https://github.com/awaitility/awaitility/wiki/How_to_release
#
# It checks everything it can before it changes anything, stops at the first failure,
# and only says the release succeeded when every step did.
set -euo pipefail

cd "$(dirname "$0")"

fail() {
  echo >&2
  echo "ERROR: $*" >&2
  exit 1
}

confirm() {
  local answer
  read -r -p "$1 [y/N] " answer
  [[ "$answer" == [yY] ]]
}

# ---------------------------------------------------------------------------
# Checks. Nothing is changed until all of these pass.
# ---------------------------------------------------------------------------

if [[ -f release.properties ]]; then
  fail "release.properties exists, so an earlier release didn't finish.
  If the tag was already pushed, finish that release with: mvn release:perform -Prelease
  Otherwise clean up with: mvn release:clean && git checkout -- . and run this script again."
fi

branch=$(git rev-parse --abbrev-ref HEAD)
[[ "$branch" == "master" ]] || fail "You're on branch '$branch'. Releases are made from master."

if [[ -n "$(git status --porcelain)" ]]; then
  git status --short
  fail "The working tree isn't clean (see above). Commit or stash your changes first.
  This script resets the poms after its test build, so uncommitted changes could be lost."
fi

echo "Fetching origin/master..."
git fetch --quiet origin master
if [[ "$(git rev-parse HEAD)" != "$(git rev-parse origin/master)" ]]; then
  fail "Your master isn't the same as origin/master. Pull (or push) first, so the release commits can be pushed."
fi

# The release version is taken from the pom (the current -SNAPSHOT version without the suffix),
# so a typo at a prompt can't publish the wrong version.
snapshotVersion=$(mvn -q -N org.apache.maven.plugins:maven-help-plugin:3.5.1:evaluate -Dexpression=project.version -DforceStdout || true)
case "$snapshotVersion" in
  *-SNAPSHOT) releaseVersion="${snapshotVersion%-SNAPSHOT}" ;;
  "") fail "Could not read the project version from pom.xml." ;;
  *) fail "Expected a -SNAPSHOT version in pom.xml but found '${snapshotVersion}'." ;;
esac
tag="awaitility-${releaseVersion}"

if git rev-parse -q --verify "refs/tags/${tag}" >/dev/null; then
  fail "Tag ${tag} already exists locally."
fi
if git ls-remote --exit-code --tags origin "refs/tags/${tag}" >/dev/null 2>&1; then
  fail "Tag ${tag} already exists on GitHub."
fi

changelogHeader=$(head -n 1 changelog.txt)
changelogPattern="^Changelog ${releaseVersion//./\\.} \\(([0-9]{4}-[0-9]{2}-[0-9]{2})\\)$"
if [[ ! "$changelogHeader" =~ $changelogPattern ]]; then
  fail "The first line of changelog.txt is '${changelogHeader}'.
  Change it to 'Changelog ${releaseVersion} ($(date +%Y-%m-%d))', commit and push, then run this script again."
fi
changelogDate="${BASH_REMATCH[1]}"
if [[ "$changelogDate" != "$(date +%Y-%m-%d)" ]]; then
  confirm "changelog.txt dates ${releaseVersion} ${changelogDate}, not today. Continue anyway?" || fail "Release aborted."
fi

javaVersion=$(java -version 2>&1 | head -n 1 || true)
if [[ "$javaVersion" != *'"1.8'* ]]; then
  echo "java -version says: ${javaVersion}"
  confirm "Awaitility is normally released with Java 8. Continue with this JDK?" || fail "Release aborted. Switch to Java 8 and run the script again."
fi

# release:perform signs the artifacts with gpg (the sonatype-oss-release profile in oss-parent).
command -v gpg >/dev/null 2>&1 || fail "gpg isn't on the PATH. It's needed to sign the artifacts for Maven Central."

serverId="sonatype-awaitility-nexus-staging"
if ! grep -q "<id>${serverId}</id>" "${HOME}/.m2/settings.xml" 2>/dev/null; then
  confirm "Found no <server> with id ${serverId} in ~/.m2/settings.xml, so the upload will likely fail. Continue anyway?" || fail "Release aborted."
fi

echo
confirm "The pom says ${snapshotVersion}, so this will release Awaitility ${releaseVersion} and tag it ${tag}. Continue?" || fail "Release aborted."

# ---------------------------------------------------------------------------
# Test build of the release version, so a broken build fails before anything is tagged.
# It also installs the release version locally, which the OSGi tests resolve from the
# local repository during release:prepare.
# ---------------------------------------------------------------------------

restorePoms() {
  git checkout --quiet -- .
}

onTestBuildExit() {
  local rc=$?
  restorePoms
  if [[ $rc -ne 0 ]]; then
    echo >&2
    echo "ERROR: the test build of ${releaseVersion} failed, see the Maven output above." >&2
    echo "The poms are back at ${snapshotVersion}, and nothing was committed or tagged." >&2
  fi
}

echo
echo "Test build of ${releaseVersion}..."
trap onTestBuildExit EXIT
mvn org.codehaus.mojo:versions-maven-plugin:2.18.0:set -Prelease -DnewVersion="${releaseVersion}" -DgenerateBackupPoms=false
mvn clean install -Prelease -DskipTests=true
restorePoms
trap - EXIT

# ---------------------------------------------------------------------------
# The release. release:prepare commits, tags and pushes; release:perform builds the tag,
# signs and uploads to Sonatype, which releases to Maven Central.
# ---------------------------------------------------------------------------

echo
echo "Releasing ${releaseVersion}..."
if ! mvn release:prepare release:perform -Prelease -DautoVersionSubmodules=true -Dtag="${tag}" -DreleaseVersion="${releaseVersion}"; then
  echo >&2
  echo "ERROR: the Maven release of ${releaseVersion} failed. See the Maven output above." >&2
  if git ls-remote --exit-code --tags origin "refs/tags/${tag}" >/dev/null 2>&1; then
    echo "The tag ${tag} is already on GitHub, so release:prepare finished. Fix the problem and finish with:" >&2
    echo "  mvn release:perform -Prelease" >&2
  else
    echo "Nothing was tagged on GitHub. To start again:" >&2
    echo "  mvn release:rollback   (or mvn release:clean && git checkout -- . if rollback has nothing to undo)" >&2
    echo "  ./mvn_release.sh" >&2
  fi
  exit 1
fi

# release:perform leaves release.properties and backup poms behind; remove them so the next run starts clean.
mvn -q release:clean || echo "Note: mvn release:clean failed, so remove release.properties and the *.releaseBackup files by hand."

previousVersion=$(git tag --list 'awaitility-[0-9]*' --sort=-v:refname | sed -n 2p | sed 's/^awaitility-//')

echo
echo "Maven release of Awaitility ${releaseVersion} completed successfully."
echo
echo "Next steps:"
echo "  1. Check that ${releaseVersion} reaches Maven Central (it can take a while):"
echo "       https://repo1.maven.org/maven2/org/awaitility/awaitility/${releaseVersion}/"
echo "  2. When it's there, generate the Javadoc: ./generate_javadoc.sh (version ${releaseVersion})"
echo "  3. Update the wiki: ./update_wiki.sh (old version ${previousVersion:-?}, new version ${releaseVersion})"
echo "  4. Add a News entry to README.md and update its Javadoc link to ${releaseVersion}."
echo "  5. Announce the release."
