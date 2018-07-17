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

    final var args = Args.parse(argv);
    out.print(ansi().bold().fgCyan());
    out.format("Warthog %s\n", WarthogVersion.get());
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
      final long startTime = System.currentTimeMillis();
      try {
        final var context = new WarthogContext(out, args, project, httpClient);

        if (args.getCommand() != null) {
          switch (args.getCommand()) {
            case "update":
              UpdateTask.perform(context);
              return;

            case "release":
              ReleaseTask.perform(context);
              return;

            default:
              printError(out, "Unsupported command " + args.getCommand());
              return;
          }
        } else {
          out.println("No command given; exiting.");
          return;
        }
      } finally {
        // clean up
        Exceptions.wrap(httpClient::close, TaskException.formatted("Error closing HTTP client: %s"));
        final var took = (int) ((System.currentTimeMillis() - startTime) / 1000d);
        out.println(ansi().fgBrightBlack().a("Took " + took + " s").reset());
      }
    } catch (TaskException e) {
      printError(out, e.getMessage());
    }
  }

  private static void printError(PrintStream out, String message) {
    out.println();
    out.println(ansi().bold().fgRed().a("✘ ").a(message).reset());
  }

  private static <T, X extends Throwable> T doOrFail(CheckedSupplier<T, X> supplier, String message) throws TaskException {
    return Exceptions.wrap(supplier, e -> new TaskException(message));
  }
}
