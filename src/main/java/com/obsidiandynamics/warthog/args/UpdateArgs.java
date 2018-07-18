package com.obsidiandynamics.warthog.args;

import com.beust.jcommander.*;

@Parameters(commandDescription="Locates newer packages and patches the build file(s).")
public final class UpdateArgs {
  @Parameter(names="--skip-build", description="Skips the build verification step")
  private boolean skipBuild;
  
  @Parameter(names="--skip-prep", description="Skips the local repo and working copy preparation steps")
  private boolean skipPrep;

  public boolean isSkipBuild() {
    return skipBuild;
  }
  
  public boolean isSkipPrep() {
    return skipPrep;
  }
}
