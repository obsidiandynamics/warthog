package com.obsidiandynamics.warthog.task;

import static com.obsidiandynamics.warthog.task.Tasks.*;
import static org.fusesource.jansi.Ansi.*;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.warthog.*;

public final class ReleaseTask {
  private ReleaseTask() {}

  public static void perform(WarthogContext context) throws TaskException {
    final var out = context.getOut();
    final var args = context.getArgs();
    final var project = context.getProject();
    final var projectDirectory = args.getCommon().getDirectory();
    final var versionist = project.getVersionist();

    out.println(ansi().fgGreen().a("Running release task").reset());
    final var commander = new Commander()
        .withSink(__ -> {})
        .withWorkingDirectory(projectDirectory);
    
    final var initialVersion = Exceptions.wrap(() -> versionist.getVersion(projectDirectory),
                                               TaskException.formatted("Error reading build file: %s"));
    if (initialVersion == null) {
      throw new TaskException("No project version found (" + versionist.describe() + ")");
    }
    if (! Versions.isSnapshot(initialVersion)) {
      throw new TaskException("Version '" + initialVersion + "' does not appear to be a snapshot");
    }
    
    // commit changes with the current (snapshot) version
    trapException(() -> {
      runConditional(out, "Updating local copy", "done", true, commander::gitPull);
      
      runConditional(out, "Committing changes", "done", commander.gitHasUncommitted(), () -> {
        if (commander.gitHasUntracked()) {
          throw new TaskException("Untracked changes found");
        }
        commander.gitCommitAll(args.getRelease().getCommitMessage());
      });
    });
    
    // update to release version
    final var releaseVersion = Versions.toRelease(initialVersion);
    out.format("Updating project version: %s -> %s\n", initialVersion, releaseVersion);
    Exceptions.wrap(() -> versionist.setVersion(projectDirectory, releaseVersion),
                    TaskException.formatted("Error patching build file: %s"));
    
    // commit, push and tag the release; then publish
    trapException(() -> {
      runConditional(out, "Committing release", "done", true, () -> {
        commander.gitCommitAll("[Warthog] Release " + releaseVersion);
      });

      runConditional(out, "Pushing to remote", "done", true, commander::gitPush);
      
      runConditional(out, "Tagging release", "done", ! args.getRelease().isSkipTag(), () -> {
        commander.gitTag(releaseVersion, "Release " + releaseVersion);
        commander.gitPushTag(releaseVersion);
      });

      runConditional(out, "Publishing artifacts", "done", ! args.getRelease().isSkipPublish(), () -> {
        commander.runCommand(project.getCommands().getPublish());
      });
    });
    
    // roll over to the next snapshot version, commit and push
    final var nextSnapshotVersion = Versions.toSnapshot(Versions.rollMinor(releaseVersion));
    out.format("Updating project version: %s -> %s\n", releaseVersion, nextSnapshotVersion);
    Exceptions.wrap(() -> versionist.setVersion(projectDirectory, nextSnapshotVersion),
                    TaskException.formatted("Error patching build file: %s"));
    trapException(() -> {
      runConditional(out, "Committing snapshot", "done", true, () -> {
        commander.gitCommitAll("[Warthog] Next snapshot");
      });

      runConditional(out, "Pushing to remote", "done", true, commander::gitPush);
    });
  }
}
