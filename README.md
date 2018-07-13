Warthog
===
Cross-project dependency management for Gradle, at scale. Let's you make changes in one project, publish the build artefacts, then recursively propagate new versions to all dependant projects.

Example of `.hog.project`:
```yaml
build: ./gradlew test cleanIntegrationTest integrationTest --info --stacktrace --no-daemon
modules:
- path: .
  dependencies:
  - name: fulcrum
    groupId: com.obsidiandynamics.fulcrum
    artefactId: fulcrum-func
  - name: yconf
    groupId: com.obsidiandynamics.yconf
    artefactId: yconf-core
  - name: zerolog
    groupId: com.obsidiandynamics.zerolog
    artefactId: zerolog-core
- path: ledger-meteor
  dependencies:
  - name: meteor
    groupId: com.obsidiandynamics.meteor
    artefactId: meteor-core
- path: ledger-kafka
  dependencies:
  - name: jackdaw
    groupId: com.obsidiandynamics.jackdaw
    artefactId: jackdaw-core
```

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