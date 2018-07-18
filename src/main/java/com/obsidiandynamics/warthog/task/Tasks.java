package com.obsidiandynamics.warthog.task;

import static org.fusesource.jansi.Ansi.*;

import java.io.*;

import com.obsidiandynamics.func.*;

public final class Tasks {
  private Tasks() {}

  static void trapException(CheckedRunnable<?> runnable) throws TaskException {
    Exceptions.wrap(runnable, e -> new TaskException(e.getMessage()));
  }
  
  static void runConditional(PrintStream out, 
                             String actionTitle, 
                             String passLabel,
                             boolean shouldRun,
                             CheckedRunnable<?> action) throws Throwable {
    runConditional(out, actionTitle, passLabel, "skipped", "failed", shouldRun, action);
  }
  
  static void runConditional(PrintStream out, 
                             String actionTitle, 
                             String passLabel, 
                             String skipLabel, 
                             String failLabel,
                             boolean shouldRun,
                             CheckedRunnable<?> action) throws Throwable {
    out.print(actionTitle + "... ");
    if (shouldRun) {
      boolean success = false;
      try {
        action.run();
        success = true;
      } finally {
        if (success) {
          out.println(ansi().bold().fgGreen().a(passLabel).reset());
        } else {
          out.println(ansi().bold().fgRed().a(failLabel).reset());
        }
      }
    } else {
      out.println(ansi().bold().fgYellow().a(skipLabel).reset());
    }
  }
}
