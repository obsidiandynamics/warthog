package com.obsidiandynamics.warthog.task;

import static com.obsidiandynamics.warthog.task.Tasks.*;

import java.io.*;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.warthog.*;
import com.obsidiandynamics.warthog.config.*;
import com.obsidiandynamics.warthog.params.*;
import com.obsidiandynamics.warthog.repository.*;

public final class UpdateTask {
  private UpdateTask() {}
  
  public static boolean perform(Params params, ProjectConfig project) throws TaskException {
    final var projectDirectory = params.getCommon().getDirectory();
    
    System.out.println("Running update task");
    final var commander = new Commander()
        .withSink(__ -> {})
        .withWorkingDirectory(projectDirectory);
    
    // ensure that the working copy is in sync with the remote
    trapCommandException(() -> {
      System.out.format("Verifying working copy... ");
      final var gitHasUncommitted = commander.gitHasUncommitted();
      if (gitHasUncommitted) {
        throw new TaskException("Working copy has uncommitted or untracked changes");
      }
      System.out.println("ready");

      System.out.format("Verifying local repository... ");
      final var gitIsAhead = commander.gitIsAhead();
      if (gitIsAhead) {
        throw new TaskException("Local repository is ahead of remote");
      }
      System.out.println("ready");

      System.out.format("Updating local copy... ");
      commander.gitPull();
      System.out.println("done");
    });
    
    // we only support one repository interface for now; future versions may introduce more
    final var respository = new BintrayRepository();
    
    // step through the modules, upgrading the build files
    boolean updatedAnyModule = false;
    for (var module : project.getModules()) {
      System.out.format("Scanning module [%s] for updates... ", module.getPath());
      final var buildFile = projectDirectory + "/" + module.getPath() + "/build.gradle";
      final var namesToVersions = Exceptions.wrap(() -> respository.bulkResolve(module.getDependencies()),
                                                  TaskException.formatted("Error looking up version: %s"));
      final var updates = Exceptions.wrap(() -> GradleTransform.updateDependencies(new File(buildFile), namesToVersions),
                                          TaskException.formatted("Error patching build file: %s"));
      if (updates.isEmpty()) {
        System.out.println("latest");
      } else {
        updatedAnyModule = true;
        System.out.println();
        for (var update : updates) {
          System.out.format("➤ Updated %s: %s -> %s\n", update.getDependencyName(), update.getOldVersion(), update.getNewVersion());
        }
      }
    }
    
    if (! updatedAnyModule) {
      System.out.format("All modules are up to date. Exiting.\n");
      return false;
    }
    
    // verify that the build passes
    System.out.format("Building project... ");
    if (! params.getUpdate().isSkipBuild()) {
      trapCommandException(() -> {
        commander.runCommand(project.getCommands().getBuild());
      });
      System.out.print("passed");
    } else {
      System.out.println("skipped");
    }
    
    return true;
  }
}
