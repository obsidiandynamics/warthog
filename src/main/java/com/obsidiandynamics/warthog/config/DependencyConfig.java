package com.obsidiandynamics.warthog.config;

import static com.obsidiandynamics.func.Functions.*;

import com.obsidiandynamics.yconf.*;

@Y
public final class DependencyConfig {
  private static final String DEF_BASE_URL = "http://jcenter.bintray.com";
  
  private final String name;
  
  private final String groupId;

  private final String artifactId;
  
  private final String repoUrl;

  public DependencyConfig(@YInject(name="name") String name, 
                          @YInject(name="groupId") String groupId, 
                          @YInject(name="artifactId") String artifactId,
                          @YInject(name="repoUrl") String repoUrl) {
    this.name = name;
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.repoUrl = ifAbsent(repoUrl, give(DEF_BASE_URL));
  }

  public String getName() {
    return name;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }
  
  public String getRepoUrl() {
    return repoUrl;
  }

  @Override
  public String toString() {
    return DependencyConfig.class.getSimpleName() + " [name=" + name + ", groupId=" + groupId + 
        ", artifactId=" + artifactId + ", repoUrl=" + repoUrl + "]";
  }
}
