package com.obsidiandynamics.warthog.args;

import com.beust.jcommander.*;

public final class Args {
  private final CommonArgs common = new CommonArgs();
  
  private final UpdateArgs update = new UpdateArgs();
  
  private final ReleaseArgs release = new ReleaseArgs();
  
  private String command;
  
  private Args() {}
  
  public CommonArgs getCommon() {
    return common;
  }

  public UpdateArgs getUpdate() {
    return update;
  }
  
  public ReleaseArgs getRelease() {
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

  public static Args parse(String... argv) {
    final var args = new Args();
    final var commander = createCommander(args);
    commander.parse(argv);
    args.command = commander.getParsedCommand();
    return args;
  }
  
  private static JCommander createCommander(Args params) {
    return JCommander.newBuilder()
        .addObject(params.common)
        .addCommand("update", params.update)
        .addCommand("release", params.release)
        .acceptUnknownOptions(false)
        .programName("hog")
        .build();
  }
}
