package com.obsidiandynamics.warthog.task;

import static com.obsidiandynamics.warthog.task.Tasks.*;

import java.io.*;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.warthog.*;
import com.obsidiandynamics.warthog.config.*;
import com.obsidiandynamics.warthog.params.*;

public final class ReleaseTask {
  private ReleaseTask() {}

  public static void perform(Params params, ProjectConfig project) throws TaskException {
    final var projectDirectory = params.getCommon().getDirectory();

    System.out.println("Running publish task");
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
      System.out.format("Updating local copy... ");
      commander.gitPull();
      System.out.println("done");
      
      System.out.format("Committing changes... ");
      final var gitHasUncommitted = commander.gitHasUncommitted();
      if (gitHasUncommitted) {
        if (commander.gitHasUntracked()) {
          throw new TaskException("Untracked changes found");
        }
        commander.gitCommitAll(params.getRelease().getCommitMessage());
        System.out.println("done");
      } else {
        System.out.println("skipped");
      }
    });
    
    // update to release version
    final var releaseVersion = Versions.toRelease(initialVersion);
    System.out.format("Updating project version: %s -> %s\n", initialVersion, releaseVersion);
    Exceptions.wrap(() -> GradleTransform.updateProjectVersion(rootBuildFile, releaseVersion),
                    TaskException.formatted("Error patching build file: %s"));
    
    // commit, push and tag the release; then publish
    trapCommandException(() -> {
      System.out.format("Committing release... ");
      commander.gitCommitAll("[Warthog] Release " + releaseVersion);
      System.out.println("done");

      System.out.format("Pushing to remote... ");
      commander.gitPush();
      System.out.println("done");
      
      System.out.format("Tagging release... ");
      if (! params.getRelease().isSkipTag()) {
        commander.gitTag(releaseVersion, "Release " + releaseVersion);
        commander.gitPushTag(releaseVersion);
        System.out.println("done");
      } else {
        System.out.println("skipped");
      }

      System.out.format("Publishing artifacts... ");
      if (! params.getRelease().isSkipPublish()) {
        commander.runCommand(project.getCommands().getPublish());
        System.out.println("done");
      } else {
        System.out.println("skipped");
      }
    });
    
    // roll over to the next snapshot version, commit and push
    final var nextSnapshotVersion = Versions.toSnapshot(Versions.rollSegment(releaseVersion, 1));
    System.out.format("Updating project version: %s -> %s\n", releaseVersion, nextSnapshotVersion);
    Exceptions.wrap(() -> GradleTransform.updateProjectVersion(rootBuildFile, nextSnapshotVersion),
                    TaskException.formatted("Error patching build file: %s"));
    trapCommandException(() -> {
      System.out.format("Committing snapshot... ");
      commander.gitCommitAll("[Warthog] Next snapshot");
      System.out.println("done");

      System.out.format("Pushing to remote... ");
      commander.gitPush();
      System.out.println("done");
    });
  }
}
