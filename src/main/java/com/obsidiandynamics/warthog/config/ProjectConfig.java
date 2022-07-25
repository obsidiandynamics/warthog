package com.obsidiandynamics.warthog.config;

import java.io.*;
import java.net.*;
import java.util.*;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.io.*;
import com.obsidiandynamics.warthog.versionist.*;
import com.obsidiandynamics.yconf.*;

@Y
public final class ProjectConfig {
  private final CommandsConfig commands;
  
  private final ModuleConfig[] modules;
  
  private final Versionist versionist;

  public ProjectConfig(@YInject(name="commands") CommandsConfig commands, 
                       @YInject(name="modules") ModuleConfig[] modules, 
                       @YInject(name="versionist") Versionist versionist) {
    this.commands = commands;
    this.modules = modules;
    this.versionist = Functions.ifAbsent(versionist, GradleVersionist::new);
  }

  public CommandsConfig getCommands() {
    return commands;
  }

  public ModuleConfig[] getModules() {
    return modules;
  }
  
  public Versionist getVersionist() {
    return versionist;
  }

  @Override
  public String toString() {
    return ProjectConfig.class.getSimpleName() + " [commands=" + commands + ", modules=" + Arrays.toString(modules) + 
        ", versionist=" + versionist + "]";
  }
  
  public static ProjectConfig fromUri(URI uri) throws IOException {
    return new MappingContext().withParser(new SnakeyamlParser()).fromStream(ResourceLoader.stream(uri)).map(ProjectConfig.class);
  }
}
