package com.obsidiandynamics.warthog.config;

import java.io.*;
import java.net.*;
import java.util.*;

import com.obsidiandynamics.io.*;
import com.obsidiandynamics.yconf.*;

@Y
public final class ProjectConfig {
  private final String build;
  
  private final ModuleConfig[] modules;

  public ProjectConfig(@YInject(name="build") String build, 
                       @YInject(name="modules") ModuleConfig[] modules) {
    this.build = build;
    this.modules = modules;
  }

  public String getBuild() {
    return build;
  }

  public ModuleConfig[] getModules() {
    return modules;
  }

  @Override
  public String toString() {
    return ProjectConfig.class.getSimpleName() + " [build=" + build + ", modules=" + Arrays.toString(modules) + "]";
  }
  
  public static ProjectConfig fromUri(URI uri) throws FileNotFoundException, IOException {
    return new MappingContext().withParser(new SnakeyamlParser()).fromStream(ResourceLoader.stream(uri)).map(ProjectConfig.class);
  }
}
