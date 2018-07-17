package com.obsidiandynamics.warthog.task;

import static com.obsidiandynamics.warthog.task.Tasks.*;
import static org.fusesource.jansi.Ansi.*;

import java.io.*;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.warthog.*;

public final class ReleaseTask {
  private ReleaseTask() {}

  public static void perform(WarthogContext context) throws TaskException {
    final var out = context.getOut();
    final var args = context.getArgs();
    final var project = context.getProject();
    final var projectDirectory = args.getCommon().getDirectory();

    out.println(ansi().bold().fgGreen().a("Running release task").reset());
    final var commander = new Commander()
        .withSink(__ -> {})
        .withWorkingDirectory(projectDirectory);
    
    final var rootBuildFile = new File(projectDirectory + "/build.gradle");
    final var initialVersion = Exceptions.wrap(() -> GradleTransform.getProjectVersion(rootBuildFile),
                                               TaskException.formatted("Error reading build file: %s"));
    if (initialVersion == null) {
      throw new TaskException("No project version in build file " + rootBuildFile);
    }
    if (! Versions.isSnapshot(initialVersion)) {
      throw new TaskException("Version '" + initialVersion + "' does not appear to be a snapshot");
    }
    
    // commit changes with the current (snapshot) version
    trapCommandException(() -> {
      out.format("Updating local copy... ");
      commander.gitPull();
      out.println(ansi().bold().fgGreen().a("done").reset());
      
      out.format("Committing changes... ");
      final var gitHasUncommitted = commander.gitHasUncommitted();
      if (gitHasUncommitted) {
        if (commander.gitHasUntracked()) {
          throw new TaskException("Untracked changes found");
        }
        commander.gitCommitAll(args.getRelease().getCommitMessage());
        out.println(ansi().bold().fgGreen().a("done").reset());
      } else {
        out.println(ansi().bold().fgYellow().a("skipped").reset());
      }
    });
    
    // update to release version
    final var releaseVersion = Versions.toRelease(initialVersion);
    out.format("Updating project version: %s -> %s\n", initialVersion, releaseVersion);
    Exceptions.wrap(() -> GradleTransform.updateProjectVersion(rootBuildFile, releaseVersion),
                    TaskException.formatted("Error patching build file: %s"));
    
    // commit, push and tag the release; then publish
    trapCommandException(() -> {
      out.format("Committing release... ");
      commander.gitCommitAll("[Warthog] Release " + releaseVersion);
      out.println(ansi().bold().fgGreen().a("done").reset());

      out.format("Pushing to remote... ");
      commander.gitPush();
      out.println(ansi().bold().fgGreen().a("done").reset());
      
      out.format("Tagging release... ");
      if (! args.getRelease().isSkipTag()) {
        commander.gitTag(releaseVersion, "Release " + releaseVersion);
        commander.gitPushTag(releaseVersion);
        out.println(ansi().bold().fgGreen().a("done").reset());
      } else {
        out.println(ansi().bold().fgYellow().a("skipped").reset());
      }

      out.format("Publishing artifacts... ");
      if (! args.getRelease().isSkipPublish()) {
        commander.runCommand(project.getCommands().getPublish());
        out.println(ansi().bold().fgGreen().a("done").reset());
      } else {
        out.println(ansi().bold().fgYellow().a("skipped").reset());
      }
    });
    
    // roll over to the next snapshot version, commit and push
    final var nextSnapshotVersion = Versions.toSnapshot(Versions.rollSegment(releaseVersion, 1));
    out.format("Updating project version: %s -> %s\n", releaseVersion, nextSnapshotVersion);
    Exceptions.wrap(() -> GradleTransform.updateProjectVersion(rootBuildFile, nextSnapshotVersion),
                    TaskException.formatted("Error patching build file: %s"));
    trapCommandException(() -> {
      out.format("Committing snapshot... ");
      commander.gitCommitAll("[Warthog] Next snapshot");
      out.println(ansi().bold().fgGreen().a("done").reset());

      out.format("Pushing to remote... ");
      commander.gitPush();
      out.println(ansi().bold().fgGreen().a("done").reset());
    });
  }
}
