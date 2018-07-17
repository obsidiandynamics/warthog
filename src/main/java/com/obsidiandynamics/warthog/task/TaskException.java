package com.obsidiandynamics.warthog.task;

import com.obsidiandynamics.func.Exceptions.*;

public final class TaskException extends Exception {
  private static final long serialVersionUID = 1L;
  
  public TaskException(String m) { super(m); }
  
  public static ExceptionWrapper<TaskException> formatted(String errorFormat) {
    return e -> new TaskException(String.format(errorFormat, e));
  }
}