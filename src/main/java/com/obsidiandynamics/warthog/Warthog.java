package com.obsidiandynamics.warthog;

import java.net.*;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.warthog.config.*;
import com.obsidiandynamics.warthog.params.*;
import com.obsidiandynamics.warthog.repository.*;
import com.obsidiandynamics.warthog.task.*;

public final class Warthog {
  private static final String PROJECT_FILE = ".hog.project";
  
  public static void main(String[] args) {
    final var params = Params.parse(args);
    System.out.format("Warthog %s\n", WarthogVersion.get());
    if (params.getCommon().isHelp()) {
      System.out.println(params.usage());
      System.exit(0);
    }
    
    final var projectDirectory = params.getCommon().getDirectory();
    System.out.format("Project directory: %s\n", projectDirectory);
    final var projectFileUri = URI.create("file://" + projectDirectory + "/" + PROJECT_FILE);
    
    try {
      final var project = doOrFail(() -> ProjectConfig.fromUri(projectFileUri), 
                                   "Project file " + PROJECT_FILE + " not found");
      
      switch (params.getCommand()) {
        case "update":
          UpdateTask.perform(params, project);
          break;
          
        case "release":
          ReleaseTask.perform(params, project);
          break;
          
        default:
          System.out.println("No command given. Exiting.");
          break;
      }

      // clean up
      Exceptions.wrap(() -> HttpClient.getInstance().close(),
                      TaskException.formatted("Error closing HTTP client: %s"));
    } catch (TaskException e) {
      exitWithError(e.getMessage());
    }
  }
  
  private static void exitWithError(String message) {
    System.out.println();
    System.out.flush();
    System.err.println(message);
    System.exit(1);
  }
  
  private static <T, X extends Throwable> T doOrFail(CheckedSupplier<T, X> supplier, String message) throws TaskException {
    return Exceptions.wrap(supplier, e -> new TaskException(message));
  }
}
