<img src="https://raw.githubusercontent.com/wiki/obsidiandynamics/warthog/images/warthog-logo.png" width="90px" alt="logo"/> Warthog
===
Cross-project release orchestration for Gradle, at scale. Make changes in one project, cut a release, publish the build artifacts, then adopt new packages in dependent projects — using just one command.

[![Build](https://travis-ci.org/obsidiandynamics/warthog.svg?branch=master) ](https://travis-ci.org/obsidiandynamics/warthog#)

# What is Warthog?
A typical scenario: a bunch of projects with complex inter-dependencies. A team wants to make a release to a library that needs to filter through to other projects which might depend on it. What's involved? A heap of testing for starters. Then publishing build artifacts to a central repository, tagging releases in Git and rolling over to the next snapshot version. What about the teams that depend on this library? They need to discover the latest version, update build files and run tests. And if you have a deep dependency graph, then the same needs to be repeated.

**Warthog** is a simple command-line tool that assists with dependency propagation. It does two things: first — it helps you incorporate updated packages into your Gradle build; second — it lets you publish updates and tag releases in a standardised and repeatable manner.

# How it works
Warthog relies on a simple config file named `.hog.project` in the root directory of your Gradle project. A real-world example of `.hog.project` is shown below.
```yaml
commands:
  build: ./gradlew test cleanIntegrationTest integrationTest --info --stacktrace --no-daemon
  publish: ./gradlew -x test bintrayUpload --no-daemon
modules:
- path: .
  dependencies:
  - name: fulcrum
    groupId: com.obsidiandynamics.fulcrum
    artifactId: fulcrum-func
  - name: yconf
    groupId: com.obsidiandynamics.yconf
    artifactId: yconf-core
  - name: zerolog
    groupId: com.obsidiandynamics.zerolog
    artifactId: zerolog-core
- path: ledger-meteor
  dependencies:
  - name: meteor
    groupId: com.obsidiandynamics.meteor
    artifactId: meteor-core
- path: ledger-kafka
  dependencies:
  - name: jackdaw
    groupId: com.obsidiandynamics.jackdaw
    artifactId: jackdaw-core
```

The `.hog.project` file is divided into two sections: `commands` and `modules`. The `build` command is invoked as part of the **update** workflow, after patching the build scripts and downloading new Maven packages — ensuring that the build still passes. The `publish` command is used as part of the **release** workflow — uploading the newly-built artifacts into a central repository (e.g. Bintray).

The `modules` section lists the relevant Gradle modules that are in the scope of the update workflow. The `path` attribute specifies the location of the module, relative the project's root directory. For example, `path: .` implies the root module, while `path: ledger-meteor` points to the module located in the `ledger-meteor` subdirectory within the project. A module has a list of `dependencies`. Each dependency is essentially a Maven package that is subject to updates. The `groupId` and `artifactId` are self-explanatory, and relate to the package itself. The `name` attribute is a special name that is given to a package (or a related group of packages) and **must** appear in your `build.gradle` file(s), in the form `nameVersion = "x.y.z"`. 

The example below illustrates how the packages `fulcrum`, `yconf` and `zerolog` are referenced and versioned in a Gradle build, following a convention that Warthog understands.
```groovy
ext {
  fulcrumVersion = "0.16.0"   // version of the 'fulcrum' library
  yconfVersion = "0.6.0"      // version of the 'yconf' library
  zerologVersion = "0.17.0"   // version of the 'zerolog' library
}

dependencies {
  compile "com.obsidiandynamics.fulcrum:fulcrum-concat:${fulcrumVersion}"        // a 'fulcrum' package
  compile "com.obsidiandynamics.fulcrum:fulcrum-flow:${fulcrumVersion}"          // a 'fulcrum' package
  compile "com.obsidiandynamics.fulcrum:fulcrum-func:${fulcrumVersion}"          // a 'fulcrum' package
  compile "com.obsidiandynamics.yconf:yconf-core:${yconfVersion}"                // a 'yconf' package
  compile "com.obsidiandynamics.zerolog:zerolog-core:${zerologVersion}"          // a 'zerolog' package
  compile "org.apache.commons:commons-lang3:3.7"                                 // another package that Warthog doesn't care about
}
```

The real magic happens when Warthog parses the build file, looking for that coveted `nameVersion = ...` string. As soon as it finds a `nameVersion` assignment matching one of the dependencies listed in `.hog.project`, it queries the repository for the latest version and then patches the source `build.gradle` file, replacing the original version with what's in the repository. For example, if the latest version of `fulcrum` is actually `0.17.0`, then the string `fulcrumVersion = "0.16.0"` is patched to `fulcrumVersion = "0.17.0"`. 

In the above example, the `fulcrum` library actually includes three artifacts: `fulcrum-concat`, `fulcrum-flow` and `fulcrum-func`; however, we got lazy and only declared one in `.hog.project`. That's fine — Warthog just needs one artifact to establish the latest version; it doesn't matter which one or whether it is even in the build file — as long as all artifacts in the group share the same version. Where this isn't the case (for example, [Apache HttpComponents](https://hc.apache.org), which uses non-uniform versioning within the same group), we'll have to make do with a dedicated `nameVersion` string per artifact, as shown below.
```groovy
ext {
  httpclientVersion = "4.5.5"
  httpasyncclientVersion = "4.1.3"
}

dependencies {
  compile "org.apache.httpcomponents:httpclient:${httpclientVersion}"
  compile "org.apache.httpcomponents:httpasyncclient:${httpasyncclientVersion}"
}
```

# Getting Started
## Requirements
Warthog works on *NIX and macOS and requires a local installation of [Java 10 JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk10-downloads-4416644.html). We also need Git. Windows-based environments are not supported.

JDK 10 is only a requirement to build and run Warthog; the actual projects may still be built with an appropriate version of the JDK.

## Installation
Paste the following into a terminal.
```sh
git clone https://github.com/obsidiandynamics/warthog
cd warthog
./gradlew build
sudo run/install 
```

This checks out a fresh copy of Warthog, runs the build (including tests), installs a symbolic link from `/usr/local/bin/hog` to `run/hog`. From there on, if you need to update Warthog, simply do a `git pull`, followed by `./gradlew build`.

## Running Warthog
### Update workflow
In the root directory of the project that needs updating, run the following:
```sh
hog update
```

That's it. Assuming that a valid `.hog.project` exists, Warthog will —

1. Verify that the working copy has no uncommitted or untracked changes;
2. Verify that the local repository is in sync with the remote;
3. Pull any changes from the remote;
4. Scan all declared modules, looking for packages that have fallen behind their latest versions;
5. Patch the build files, pointing to the latest dependency versions; and
6. Run the build (using the `build` command specified in `.hog.project`), verifying that it's still sane.

Steps 1–3 are recommended to avoid conflicts when the changes are committed back in. You can skip them by passing the `--skip-prep` flag to `hog`.

Step 6 can be skipped with `--skip-build`.

### Release workflow
To cut a release of our project and publish the relevant artifacts, run the following:
```sh
hog release
```

Warthog will —

1. Pull any changes into the working copy;
2. Commit the patched build files from the previous step (and any other tracked pending changes that happen to be in your working copy, so beware);
3. Change the project version from a snapshot to a release by patching the root `build.gradle` file (i.e. `x.y.z-SNAPSHOT` becomes `x.y.z`);
4. Commit the patched `build.gradle` with the message `[Warthog] Release x.y.z` (replacing `x.y.z` with the release version);
5. Tag the last commit (tag is named `x.y.z` and the message is `Release x.y.z`);
6. Push the tag to remote;
7. Publish the build artifacts (using the `publish` command specified in `.hog.project`);
8. Roll the project minor version in the root `build.gradle` and mark it as a snapshot (i.e. `x.y.z` becomes `x.(y+1).z-SNAPSHOT`);
9. Commit `build.gradle` with the new snapshot version; and
10. Push `build.gradle` to remote.

The commit message in step 2 can be overridden with the `--message` argument. The default is `[Warthog] Updated dependencies`.

Steps 5–6 can be skipped with `--skip-tag`. 

Step 7 can be skipped with `--skip-publish`.

**Note:** Warthog expects the project version to appear in the root `build.gradle` file, in the form `version = "x.y.z[-qualifier]"`. The version must be conform to [Maven conventions](https://docs.oracle.com/middleware/1212/core/MAVEN/maven_version.htm#MAVEN400) and may have optional qualifiers. The second segment from the left (the minor version) will be incremented in the release workflow as part of staging the next snapshot, and therefore must be numeric.

# Custom Repositories
By default, Warthog queries the JCenter repository (`http://jcenter.bintray.com`) for package versions, which also conveniently acts as a CDN for Maven Central — giving you access to virtually all publicly available Maven packages.

Warthog can be instructed to use any Maven-compliant repository (for example, your organisation's internal repo) by overriding the `repoUrl` attribute in the dependency configuration, as shown in the example below.
```yaml
commands:
modules:
- path: .
  dependencies:
  - name: private-dependency
    groupId: com.private
    artifactId: private-thing
    repoUrl: https://repo.company.com/packages
```

# Gotchas
* Warthog workflows take care in ensuring that they operate on clean working copies and synced local repos. The likelihood of collisions is low, but not zero. Warthog will terminate if any of the orchestrated operations fail, leaving you to resolve conflicts manually.
* If running `update` and `release` with a time gap in-between, it's recommended that you commit any work that you may have done separately, so it doesn't get bundled into Warthog's commits.
* Tagging does not automatically include release notes, as this is outside the scope of plain Git. (GitHub, GitLab, BitBucket, _et al._ have their own concepts of releases.) You'll need to manually write up a release and link it to a prior tag. (Future Warthog could offer deeper integration into hosted repositories.)
* Occasionally the latest package version reported by a repository mightn't be what you want. Sometimes package publishers do not set the right version as the latest; this has nothing to do with Warthog.

# Limitations
* Only Gradle is supported, with and without the wrapper. Other build tools, such as Maven, SBT, Ant/Ivy, etc. are not supported.
* For the release workflow, the project version must be in the root `build.gradle` file and follow the strict `version = "x.y.z[-qualifier]"` form described earlier.
* Project versions stored outside of the root `build.gradle` aren't presently supported for releases.
* For the update workflow, the dependency versions must appear in `build.gradle` files in the strict `nameVersion = "x.y.z[-qualifier]"` form described earlier.
* Custom auth for private repos isn't supported at this stage. The assumption is that the repo is reachable solely via a private/trusted network and that the machine running `hog` has implicit access.

# FAQ
## Is Warthog orchestrated using Warthog?
An obligatory question. To dispel hypocrisy — yes, it is.

## Where did the name Warthog come from?
The answer could have been along the lines of _"it was the only name that wasn't taken"_ or _"because warthogs are particularly good at such and such"_ or _"it's an exotic acronym"_ or even _"the word 'hog' is particularly easy to type"_. The truth is that any answer is as good (or bad) as the next; there is no rationale behind the naming.

## How is this different from [Gradle Versions Plugin](https://github.com/ben-manes/gradle-versions-plugin)?
There is lots of overlap for identifying prospective package updates, and the two can be used together effectively. Warthog is still in its infancy, and is mostly concerned with orchestrating updates and standardising releases. (Automation, in one word.) 

Gradle Versions Plugin (GVP) integrates tightly with Gradle and provides a comprehensive report on both plugin and dependency versions, requiring nothing more than your existing Gradle setup. But GVP cannot orchestrate version updates, verify builds, perform tagged releases or publish artifacts. And although GVP is only a reporting tool, it doesn't make it any less useful — just a bit different to Warthog.

