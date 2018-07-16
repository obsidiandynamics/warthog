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
    try {
      run();
    } catch (WarthogException e) {
      exitWithError(e.getMessage());
    }
  }
  
  private static void run() throws WarthogException {
    final var workingDirectory = "../bs";
    
    final var projectFileUri = URI.create("file://" + workingDirectory + "/" + PROJECT_FILE);
    final var project = doOrFail(() -> ProjectConfig.fromUri(projectFileUri), 
                                 "Project file " + PROJECT_FILE + " not found");

    final var commander = new Commander().withWorkingDirectory(workingDirectory);
    
    // ensure that the working copy is in sync with the remote
    trapCommandException(() -> {
      final var gitHasUncommitted = commander.gitHasUncommitted();
      if (gitHasUncommitted) {
        throw new WarthogException("Working copy has uncommitted or untracked changes; exiting.");
      }

      final var gitIsAhead = commander.gitIsAhead();
      if (gitIsAhead) {
        throw new WarthogException("Working copy has unpublished changes; exiting.");
      }

      commander.gitPull();
    });
    
    // we only support one lookup method for now; future versions may introduce more
    final var versionLookup = new BintrayVersionLookup();
    
    // step through the modules, upgrading the build files
    for (var module : project.getModules()) {
      System.out.format("Analysing module '%s'... ", module.getPath());
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
          System.out.format("Updated %s: %s -> %s\n", update.getDependencyName(), update.getOldVersion(), update.getNewVersion());
        }
      }
    }
    
    Exceptions.wrap(() -> HttpClient.getInstance().close(),
                    e -> new WarthogException("Error closing HTTP client: " + e));
  }
  
  private static void exitWithError(String message) {
    System.out.flush();
    System.err.println(message);
    System.exit(1);
  }

  private static <T, X extends Throwable> T doOrFail(CheckedSupplier<T, X> supplier, String message) throws WarthogException {
    return Exceptions.wrap(supplier, e -> new WarthogException(message));
  }

  private static void trapCommandException(CheckedRunnable<?> runnable) throws WarthogException {
    Exceptions.wrap(runnable, e -> new WarthogException(e.getMessage() + "\n" + "Error running command; exiting."));
  }
}
