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
    //TODO configurable working dir
    final var workingDirectory = "../bs";
    final var projectFileUri = URI.create("file://" + workingDirectory + "/" + PROJECT_FILE);
    
    try {
      final var project = doOrFail(() -> ProjectConfig.fromUri(projectFileUri), 
                                   "Project file " + PROJECT_FILE + " not found");
      
      //TODO reinstate
      update(workingDirectory, project);
//      publish(workingDirectory, project);

      // clean up
      Exceptions.wrap(() -> HttpClient.getInstance().close(),
                      e -> new WarthogException("Error closing HTTP client: " + e));
    } catch (WarthogException e) {
      exitWithError(e.getMessage());
    }
  }
  
  private static void update(String workingDirectory, ProjectConfig project) throws WarthogException {
    final var commander = new Commander()
        .withSink(__ -> {})
        .withWorkingDirectory(workingDirectory);
    
    // ensure that the working copy is in sync with the remote
    System.out.format("Verifying working copy... ");
    trapCommandException(() -> {
      final var gitHasUncommitted = commander.gitHasUncommitted();
      if (gitHasUncommitted) {
        throw new WarthogException("Working copy has uncommitted or untracked changes");
      }

      final var gitIsAhead = commander.gitIsAhead();
      if (gitIsAhead) {
        throw new WarthogException("Local repository is ahead of remote");
      }

      commander.gitPull();
    });
    System.out.println("ready");
    
    // we only support one lookup method for now; future versions may introduce more
    final var versionLookup = new BintrayVersionLookup();
    
    // step through the modules, upgrading the build files
    for (var module : project.getModules()) {
      System.out.format("Analysing module [%s]... ", module.getPath());
      final var buildFile = workingDirectory + "/" + module.getPath() + "/build.gradle";
      final var namesToVersions = Exceptions.wrap(() -> versionLookup.bulkResolve(module.getDependencies()),
                                                  e -> new WarthogException("Error looking up version: " + e));
      final var updates = Exceptions.wrap(() -> GradleTransform.updateDependencies(new File(buildFile), namesToVersions),
                                          e -> new WarthogException("Error transforming build file: " + e));
      if (updates.isEmpty()) {
        System.out.println("no changes");
      } else {
        System.out.println();
        for (var update : updates) {
          System.out.format("➤ Updated %s: %s -> %s\n", update.getDependencyName(), update.getOldVersion(), update.getNewVersion());
        }
      }
    }
    
    // verify that the build passes
    System.out.format("Running build... ");
    trapCommandException(() -> {
      commander.runCommand(project.getBuild());
    });
    System.out.print("passed");
  }
  
  private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";
  
  private static void publish(String workingDirectory, ProjectConfig project) throws WarthogException {
    final var rootBuildFile = new File(workingDirectory + "/build.gradle");
    final var initialVersion = Exceptions.wrap(() -> GradleTransform.getProjectVersion(rootBuildFile),
                                               e -> new WarthogException("Error reading build file: " + e));
    if (initialVersion == null) {
      throw new WarthogException("No project version in build file " + rootBuildFile);
    }
    if (! initialVersion.endsWith(SNAPSHOT_SUFFIX)) {
      throw new WarthogException("Version '" + initialVersion + "' does not appear to be a snapshot");
    }

    // commit changes with the current (snapshot) version
    //TODO
    
    final var releaseVersion = initialVersion.substring(0, initialVersion.length() - SNAPSHOT_SUFFIX.length());
    System.out.format("Updating version: %s -> %s\n", initialVersion, releaseVersion);
    
    // update to release version, commit, push and tag
    //TODO
    
    // publish artifacts
    //TODO
    
    // roll over to the next snapshot version, commit and push
    //TODO
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
