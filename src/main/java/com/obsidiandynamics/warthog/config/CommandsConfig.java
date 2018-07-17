package com.obsidiandynamics.warthog.config;

import com.obsidiandynamics.yconf.*;

@Y
public final class CommandsConfig {
  private final String build;
  
  private final String publish;

  public CommandsConfig(@YInject(name="build") String build, 
                        @YInject(name="publish") String publish) {
    this.build = build;
    this.publish = publish;
  }

  public String getBuild() {
    return build;
  }

  public String getPublish() {
    return publish;
  }

  @Override
  public String toString() {
    return CommandsConfig.class.getSimpleName() + " [build=" + build + ", publish=" + publish + "]";
  }
}
