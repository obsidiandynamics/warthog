package com.obsidiandynamics.warthog.config;

import java.io.*;
import java.net.*;
import java.util.*;

import com.obsidiandynamics.io.*;
import com.obsidiandynamics.yconf.*;

@Y
public final class ProjectConfig {
  private final CommandsConfig commands;
  
  private final ModuleConfig[] modules;

  public ProjectConfig(@YInject(name="commands") CommandsConfig commands, 
                       @YInject(name="modules") ModuleConfig[] modules) {
    this.commands = commands;
    this.modules = modules;
  }

  public CommandsConfig getCommands() {
    return commands;
  }

  public ModuleConfig[] getModules() {
    return modules;
  }

  @Override
  public String toString() {
    return ProjectConfig.class.getSimpleName() + " [commands=" + commands + ", modules=" + Arrays.toString(modules) + "]";
  }
  
  public static ProjectConfig fromUri(URI uri) throws FileNotFoundException, IOException {
    return new MappingContext().withParser(new SnakeyamlParser()).fromStream(ResourceLoader.stream(uri)).map(ProjectConfig.class);
  }
}
