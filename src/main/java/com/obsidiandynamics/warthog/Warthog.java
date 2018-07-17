package com.obsidiandynamics.warthog;

import java.io.*;
import java.net.*;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.warthog.config.*;
import com.obsidiandynamics.warthog.versionlookup.*;

public final class Warthog {
  private static final String PROJECT_FILE = ".hog.project";
  
  private static final class WarthogException extends Exception {
    private static final long serialVersionUID = 1L;
    WarthogException(String m) { super(m); }
  }

  public static void main(String[] args) {
    //TODO configurable project dir
    final var projectDirectory = "../bs";
    System.out.format("Project directory: %s\n", projectDirectory);
    final var projectFileUri = URI.create("file://" + projectDirectory + "/" + PROJECT_FILE);
    
    try {
      final var project = doOrFail(() -> ProjectConfig.fromUri(projectFileUri), 
                                   "Project file " + PROJECT_FILE + " not found");
      
      //TODO reinstate
      update(projectDirectory, project);
      publish(projectDirectory, project);

      // clean up
      Exceptions.wrap(() -> HttpClient.getInstance().close(),
                      e -> new WarthogException("Error closing HTTP client: " + e));
    } catch (WarthogException e) {
      exitWithError(e.getMessage());
    }
  }
  
  private static void update(String projectDirectory, ProjectConfig project) throws WarthogException {
    final var skipBuild = true;
    
    System.out.println("Running update task");
    final var commander = new Commander()
        .withSink(__ -> {})
        .withWorkingDirectory(projectDirectory);
    
    // ensure that the working copy is in sync with the remote
    trapCommandException(() -> {
      System.out.format("Verifying working copy... ");
      final var gitHasUncommitted = commander.gitHasUncommitted();
      if (gitHasUncommitted) {
        throw new WarthogException("Working copy has uncommitted or untracked changes");
      }
      System.out.println("ready");

      System.out.format("Verifying local repository... ");
      final var gitIsAhead = commander.gitIsAhead();
      if (gitIsAhead) {
        throw new WarthogException("Local repository is ahead of remote");
      }
      System.out.println("ready");

      System.out.format("Updating local copy... ");
      commander.gitPull();
      System.out.println("done");
    });
    
    // we only support one lookup method for now; future versions may introduce more
    final var versionLookup = new BintrayVersionLookup();
    
    // step through the modules, upgrading the build files
    for (var module : project.getModules()) {
      System.out.format("Scanning module [%s] for updates... ", module.getPath());
      final var buildFile = projectDirectory + "/" + module.getPath() + "/build.gradle";
      final var namesToVersions = Exceptions.wrap(() -> versionLookup.bulkResolve(module.getDependencies()),
                                                  e -> new WarthogException("Error looking up version: " + e));
      final var updates = Exceptions.wrap(() -> GradleTransform.updateDependencies(new File(buildFile), namesToVersions),
                                          e -> new WarthogException("Error patching build file: " + e));
      if (updates.isEmpty()) {
        System.out.println("latest");
      } else {
        System.out.println();
        for (var update : updates) {
          System.out.format("➤ Updated %s: %s -> %s\n", update.getDependencyName(), update.getOldVersion(), update.getNewVersion());
        }
      }
    }
    
    // verify that the build passes
    System.out.format("Building project... ");
    if (! skipBuild) {
      trapCommandException(() -> {
        commander.runCommand(project.getCommands().getBuild());
      });
      System.out.print("passed");
    } else {
      System.out.println("skipped");
    }
  }
  
  private static void publish(String projectDirectory, ProjectConfig project) throws WarthogException {
    final var skipTag = false;
    final var skipPublish = true;
    final var commitMessage = "[Warthog] Updated dependencies";

    System.out.println("Running publish task");
    final var commander = new Commander()
        .withSink(__ -> {})
        .withWorkingDirectory(projectDirectory);
    
    final var rootBuildFile = new File(projectDirectory + "/build.gradle");
    final var initialVersion = Exceptions.wrap(() -> GradleTransform.getProjectVersion(rootBuildFile),
                                               e -> new WarthogException("Error reading build file: " + e));
    if (initialVersion == null) {
      throw new WarthogException("No project version in build file " + rootBuildFile);
    }
    if (! Versions.isSnapshot(initialVersion)) {
      throw new WarthogException("Version '" + initialVersion + "' does not appear to be a snapshot");
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
          throw new WarthogException("Untracked changes found");
        }
        commander.gitCommitAll(commitMessage);
        System.out.println("done");
      } else {
        System.out.println("skipped");
      }
    });
    
    // update to release version
    final var releaseVersion = Versions.toRelease(initialVersion);
    System.out.format("Updating project version: %s -> %s\n", initialVersion, releaseVersion);
    Exceptions.wrap(() -> GradleTransform.updateProjectVersion(rootBuildFile, releaseVersion),
                    e -> new WarthogException("Error patching build file: " + e));
    
    // commit, push and tag the release; then publish
    trapCommandException(() -> {
      System.out.format("Committing release... ");
      commander.gitCommitAll(commitMessage);
      System.out.println("done");

      System.out.format("Pushing to remote... ");
      commander.gitPush();
      System.out.println("done");
      
      System.out.format("Tagging release... ");
      if (! skipTag) {
        commander.gitTag(releaseVersion, "Release " + releaseVersion);
        commander.gitPushTag(releaseVersion);
        System.out.println("done");
      } else {
        System.out.println("skipped");
      }

      System.out.format("Publishing artifacts... ");
      if (! skipPublish) {
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
                    e -> new WarthogException("Error patching build file: " + e));
    trapCommandException(() -> {
      System.out.format("Committing snapshot... ");
      commander.gitCommitAll("[Warthog] Next snapshot");
      System.out.println("done");

      System.out.format("Pushing to remote... ");
      commander.gitPush();
      System.out.println("done");
    });
  }
  
  private static void exitWithError(String message) {
    System.out.println();
    System.out.flush();
    System.err.println(message);
    System.exit(1);
  }

  private static <T, X extends Throwable> T doOrFail(CheckedSupplier<T, X> supplier, String message) throws WarthogException {
    return Exceptions.wrap(supplier, e -> new WarthogException(message));
  }

  private static void trapCommandException(CheckedRunnable<?> runnable) throws WarthogException {
    Exceptions.wrap(runnable, e -> new WarthogException(e.getMessage()));
  }
}
