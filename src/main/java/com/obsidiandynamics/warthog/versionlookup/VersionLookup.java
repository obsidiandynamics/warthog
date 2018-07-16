package com.obsidiandynamics.warthog.versionlookup;

import java.util.*;

import com.obsidiandynamics.warthog.config.*;

@FunctionalInterface
public interface VersionLookup {
  String getLatestVersion(String groupId, String artefactId) throws Exception;
  
  default Map<String, String> bulkResolve(DependencyConfig[] dependencies) throws Exception {
    final var namesToVersions = new HashMap<String, String>(dependencies.length);
    for (var dependency : dependencies) {
      namesToVersions.put(dependency.getName(), getLatestVersion(dependency.getGroupId(), dependency.getArtefactId()));
    }
    return namesToVersions;
  }
}
