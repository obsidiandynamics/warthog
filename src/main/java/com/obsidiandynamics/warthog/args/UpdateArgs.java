package com.obsidiandynamics.warthog.args;

import com.beust.jcommander.*;

@Parameters(commandDescription="Locates newer packages and patches the build file(s)")
public final class UpdateArgs {
  @Parameter(names="--skip-build", description="Skips the build verification step")
  private boolean skipBuild;

  public boolean isSkipBuild() {
    return skipBuild;
  }
}
