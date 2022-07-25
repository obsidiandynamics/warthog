package com.obsidiandynamics.warthog;

import static org.fusesource.jansi.Ansi.*;

import java.io.*;
import java.net.*;

import org.fusesource.jansi.*;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.warthog.args.*;
import com.obsidiandynamics.warthog.config.*;
import com.obsidiandynamics.warthog.task.*;

public final class Warthog {
  private static final String PROJECT_FILE = ".hog.project";
  
  public static void main(String[] argv) {
    final var out = AnsiConsole.out;
    WarthogBanner.print(out);

    final Args args;
    try {
      args = Args.parse(argv);
    } catch (Throwable e) {
      exitWithError(out, "Error parsing arguments: " + e.getMessage());
      return;
    }
    
    out.print(ansi().bold().fgCyan());
    out.println(ansi().bold().fgCyan().a("Warthog").boldOff().a(" version " + WarthogVersion.get()).reset());
    out.print(ansi().reset());
    if (args.getCommon().isHelp()) {
      out.println(new AnsiString(args.usage())
                  .retouchPattern("Options:", ansi().bold())
                  .retouchPattern("Commands:", ansi().bold())
                  .retouchPattern("\\s{4}update", ansi().bold().fgGreen())
                  .retouchPattern("\\s{4}release", ansi().bold().fgGreen()));
      return;
    }

    final var projectDirectory = args.getCommon().getDirectory();
    out.print(ansi().fgBrightBlack());
    out.format("Project directory: %s\n\n", projectDirectory);
    out.print(ansi().reset());
    final var projectFileUri = URI.create("file://" + projectDirectory + "/" + PROJECT_FILE);

    try {
      final var project = doOrFail(() -> ProjectConfig.fromUri(projectFileUri), 
                                   "Project file " + PROJECT_FILE + " not found");
      final var httpClient = Exceptions.wrap(HttpClient::create,
                                             TaskException.formatted("Error creating HTTP client: %s"));
      final var startTime = System.currentTimeMillis();
      try {
        final var context = new WarthogContext(out, args, project, httpClient);

        if (args.getCommand() != null) {
          switch (args.getCommand()) {
            case "update":
              UpdateTask.perform(context);
              break;

            case "release":
              ReleaseTask.perform(context);
              break;

            default:
              exitWithError(out, "Unsupported command " + args.getCommand());
              break;
          }
        } else {
          out.println("No command given; exiting.");
        }
      } finally {
        // clean up
        Exceptions.wrap(httpClient::close, TaskException.formatted("Error closing HTTP client: %s"));
        final var took = (int) ((System.currentTimeMillis() - startTime) / 1000d);
        out.println(ansi().fgBrightBlack().a("\nTook " + took + " s").reset());
      }
    } catch (TaskException e) {
      exitWithError(out, e.getMessage());
    }
  }

  private static void exitWithError(PrintStream out, String message) {
    out.println();
    out.println(ansi().fgRed().a("✘ ").a(message).reset());
    System.exit(1);
  }

  private static <T, X extends Throwable> T doOrFail(CheckedSupplier<T, X> supplier, String message) throws TaskException {
    return Exceptions.wrap(supplier, e -> new TaskException(message));
  }
}
