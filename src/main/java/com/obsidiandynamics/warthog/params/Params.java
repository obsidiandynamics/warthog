package com.obsidiandynamics.warthog.params;

import com.beust.jcommander.*;

public final class Params {
  private final CommonParams common = new CommonParams();
  
  private final UpdateParams update = new UpdateParams();
  
  private final ReleaseParams release = new ReleaseParams();
  
  private String command;
  
  private Params() {}
  
  public CommonParams getCommon() {
    return common;
  }

  public UpdateParams getUpdate() {
    return update;
  }
  
  public ReleaseParams getRelease() {
    return release;
  }
  
  public String getCommand() {
    return command;
  }
  
  public String usage() {
    final var commander = createCommander(this);
    final var buffer = new StringBuilder();
    if (command != null) {
      commander.usage(command, buffer);
    } else {
      commander.usage(buffer);
    }
    return buffer.toString();
  }

  public static Params parse(String... args) {
    final var params = new Params();
    final var commander = createCommander(params);
    commander.parse(args);
    params.command = commander.getParsedCommand();
    return params;
  }
  
  private static JCommander createCommander(Params params) {
    return JCommander.newBuilder()
        .addObject(params.common)
        .addCommand("update", params.update)
        .addCommand("release", params.release)
        .acceptUnknownOptions(false)
        .programName("hog")
        .build();
  }
}
