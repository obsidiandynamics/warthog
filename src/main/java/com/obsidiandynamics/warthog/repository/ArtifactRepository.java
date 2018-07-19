package com.obsidiandynamics.warthog.repository;

import java.util.*;

import com.obsidiandynamics.warthog.*;
import com.obsidiandynamics.warthog.config.*;

@FunctionalInterface
public interface ArtifactRepository {
  String resolveLatestVersion(WarthogContext context, DependencyConfig dependency) throws Exception;
  
  default Map<String, String> bulkResolve(WarthogContext context, DependencyConfig[] dependencies) throws Exception {
    final var namesToVersions = new HashMap<String, String>(dependencies.length);
    for (var dependency : dependencies) {
      namesToVersions.put(dependency.getName(), resolveLatestVersion(context, dependency));
    }
    return namesToVersions;
  }
}
