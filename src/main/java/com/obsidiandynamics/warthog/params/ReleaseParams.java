package com.obsidiandynamics.warthog.params;

import com.beust.jcommander.*;

@Parameters(commandDescription="Updates release version, publishes artifacts, starts new snapshot, commits and pushes changes upstream")
public final class ReleaseParams {
  @Parameter(names="--skip-tag", description="Skips the release tagging step")
  private boolean skipTag;
  
  @Parameter(names="--skip-publish", description="Skips the artifact publishing step")
  private boolean skipPublish;
  
  @Parameter(names={"-m", "--message"}, description="Commit message")
  private String commitMessage = "[Warthog] Updated dependencies";
  
  public boolean isSkipTag() {
    return skipTag;
  }

  public boolean isSkipPublish() {
    return skipPublish;
  }
  
  public String getCommitMessage() {
    return commitMessage;
  }
}
