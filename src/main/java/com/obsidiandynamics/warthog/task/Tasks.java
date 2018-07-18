package com.obsidiandynamics.warthog.task;

import com.obsidiandynamics.func.*;

public final class Tasks {
  private Tasks() {}

  static void trapException(CheckedRunnable<?> runnable) throws TaskException {
    Exceptions.wrap(runnable, e -> new TaskException(e.getMessage()));
  }
}
