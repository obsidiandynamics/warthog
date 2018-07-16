package com.obsidiandynamics.warthog.config;

import java.util.*;

import com.obsidiandynamics.yconf.*;

@Y
public final class ModuleConfig {
  private final String path;
  
  private final DependencyConfig[] dependencies;

  public ModuleConfig(@YInject(name="path") String path, 
                      @YInject(name="dependencies") DependencyConfig[] dependencies) {
    this.path = path;
    this.dependencies = dependencies;
  }

  public String getPath() {
    return path;
  }

  public DependencyConfig[] getDependencies() {
    return dependencies;
  }

  @Override
  public String toString() {
    return ModuleConfig.class + " [path=" + path + ", dependencies=" + Arrays.toString(dependencies) + "]";
  }
}
