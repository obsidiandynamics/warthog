package com.obsidiandynamics.warthog.task;

import java.util.function.*;

public final class TaskException extends Exception {
  private static final long serialVersionUID = 1L;
  
  public TaskException(String m) { super(m); }
  
  public static Function<Throwable, TaskException> formatted(String errorFormat) {
    return e -> new TaskException(String.format(errorFormat, e));
  }
}