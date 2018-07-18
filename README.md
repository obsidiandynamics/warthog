Warthog
===
Cross-project dependency management for Gradle, at scale. Let's you make changes in one project, publish the build artifacts, then propagate new versions to dependent projects.

# What is Warthog?
A typical scenario: a bunch of projects with complex inter-dependencies. A team wants to make a release to a library that needs to filter through to other projects which might depend on it. What's involved? A heap of testing for starters. Then publishing build artifacts to a central repository, tagging releases in Git and rolling over to the next snapshot version. What about the teams that depend on this library? They need to discover the latest version, update build files and run tests. And if you have a deep dependency graph, then the same needs to be repeated.

**Warthog** is a simple command-line tool that assists with dependency propagation. It does two things: first — it helps you incorporate updated packages into your Gradle build; second — it lets you publish updates and tag releases in a standardised manner.

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

The `.hog.project` file is divided into two sections: `commands` and `modules`. The `build` command is invoked as part of the **update** workflow, after patching the build scripts and downloading new Maven packages — ensuring that the build still passes. The `publish` command is used as part of the **release** workflow — pushing the newly-built artifacts into a central repository (e.g. Bintray).

The `modules` section lists the relevant Gradle modules that are in the scope of the update workflow. The `path` attribute specifies the location of the module, relative the project's root directory. For example, `path: .` implies the root module, while `path: ledger-meteor` points to the module located in the `ledger-meteor` subdirectory within the project. A module has a list of `dependencies`. Each dependency is essentially a Maven package that is subject to updates. The `groupId` and `artifactId`

Search bintray for group and artefact:
https://bintray.com/api/v1/search/packages/maven?g=com.obsidiandynamics.zerolog&a=zerolog-core

Produces:
```json
[
    {
        "desc": "Ultra-low overhead logging façade for performance-sensitive applications",
        "latest_version": "0.16.0",
        "name": "zerolog-core",
        "owner": "obsidiandynamics",
        "repo": "zerolog",
        "system_ids": [
            "com.obsidiandynamics.zerolog:zerolog-core"
        ],
        "versions": [
            "0.16.0",
            "0.15.0",
            "0.14.0",
            "0.13.0",
            "0.12.0",
            "0.11.1",
            "0.11.0",
            "0.10.1",
            "0.10.0",
            "0.9.0",
            "0.8.0",
            "0.7.0",
            "0.6.0",
            "0.5.0",
            "0.4.0",
            "0.3.0",
            "0.2.0",
            "0.1.0"
        ]
    }
]
```

We need `latest_version`

**Usage**
    hog <command> [options]

**Commands**
update
    Locates newer packages and patches the build file(s).

release
    Updates release version, publishes artefacts, starts new snapshot, commits and pushes changes upstream.

workspace-refresh
    Scans subdirectories to locate Warthog projects and writes to .hog.workspace (or updates existing).

workspace-list
    Lists projects as a dependency tree.

workspace-upgrade
    Recursively performs a mass update+release operation on all projects in strict dependency order.

**Common options**
--help
    Shows either the overall, or command-specific help.
--interactive 
    Interactive mode, prompting between key steps.
--dry-run
    Simulates actions without performing them.


Example of `.hog.workspace`:
```yaml
projects:
- name: fulcrum
- name: katana
  path: kobayashi/katana
```
**Note:** The `path` attribute is optional; if not set, `path` is assumed to be the same as `name`.