package com.obsidiandynamics.warthog.config;

import com.obsidiandynamics.yconf.*;

@Y
public final class DependencyConfig {
  private final String name;
  
  private final String groupId;

  private final String artefactId;

  public DependencyConfig(@YInject(name="name") String name, 
                          @YInject(name="groupId") String groupId, 
                          @YInject(name="artefactId") String artefactId) {
    this.name = name;
    this.groupId = groupId;
    this.artefactId = artefactId;
  }

  public String getName() {
    return name;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtefactId() {
    return artefactId;
  }

  @Override
  public String toString() {
    return DependencyConfig.class.getSimpleName() + " [name=" + name + ", groupId=" + groupId + 
        ", artefactId=" + artefactId + "]";
  }
}
