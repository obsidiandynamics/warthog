package com.obsidiandynamics.warthog.task;

import static com.obsidiandynamics.warthog.task.Tasks.*;
import static org.fusesource.jansi.Ansi.*;

import java.io.*;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.warthog.*;
import com.obsidiandynamics.warthog.repository.*;

public final class UpdateTask {
  private UpdateTask() {}
  
  public static boolean perform(WarthogContext context) throws TaskException {
    final var out = context.getOut();
    final var args = context.getArgs();
    final var project = context.getProject();
    final var projectDirectory = args.getCommon().getDirectory();
    
    out.println(ansi().bold().fgGreen().a("Running update task").reset());
    final var commander = new Commander()
        .withSink(__ -> {})
        .withWorkingDirectory(projectDirectory);
    
    // ensure that the working copy is in sync with the remote
    trapCommandException(() -> {
      out.format("Verifying working copy... ");
      final var gitHasUncommitted = commander.gitHasUncommitted();
      if (gitHasUncommitted) {
        throw new TaskException("Working copy has uncommitted or untracked changes");
      }
      out.println(ansi().bold().fgGreen().a("ready").reset());

      out.format("Verifying local repository... ");
      final var gitIsAhead = commander.gitIsAhead();
      if (gitIsAhead) {
        throw new TaskException("Local repository is ahead of remote");
      }
      out.println(ansi().bold().fgGreen().a("ready").reset());

      out.format("Updating local copy... ");
      commander.gitPull();
      out.println(ansi().bold().fgGreen().a("done").reset());
    });
    
    // we only support one repository interface for now; future versions may introduce more
    final var respository = new BintrayRepository();
    
    // step through the modules, upgrading the build files
    boolean updatedAnyModule = false;
    for (var module : project.getModules()) {
      out.format("Scanning module [%s] for updates... ", module.getPath());
      final var buildFile = projectDirectory + "/" + module.getPath() + "/build.gradle";
      final var namesToVersions = Exceptions.wrap(() -> respository.bulkResolve(context, module.getDependencies()),
                                                  TaskException.formatted("Error looking up version: %s"));
      final var updates = Exceptions.wrap(() -> GradleTransform.updateDependencies(new File(buildFile), namesToVersions),
                                          TaskException.formatted("Error patching build file: %s"));
      if (updates.isEmpty()) {
        out.println(ansi().bold().fgGreen().a("latest").reset());
      } else {
        updatedAnyModule = true;
        out.println();
        for (var update : updates) {
          out.print(ansi().bold().fgGreen().a("✓ ").reset());
          out.format("Updated %s: %s -> %s\n", update.getDependencyName(), update.getOldVersion(), update.getNewVersion());
        }
      }
    }
    
    if (! updatedAnyModule) {
      out.println(ansi().bold().fgYellow().a("All modules are up to date. Exiting.").reset());
      return false;
    }
    
    // verify that the build passes
    out.format("Building project... ");
    if (! args.getUpdate().isSkipBuild()) {
      trapCommandException(() -> {
        commander.runCommand(project.getCommands().getBuild());
      });
      out.println(ansi().bold().fgGreen().a("passed").reset());
    } else {
      out.println(ansi().bold().fgYellow().a("skipped").reset());
    }
    
    return true;
  }
}
