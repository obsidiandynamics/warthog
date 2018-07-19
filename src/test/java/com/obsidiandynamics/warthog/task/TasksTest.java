package com.obsidiandynamics.warthog.task;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class TasksTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Tasks.class);
  }

  @Test(expected=TaskException.class)
  public void testTrapException() throws TaskException {
    Tasks.trapException(() -> {
      throw new Exception("Simulated error");
    });
  }

  @Test
  public void testTrapExceptionNoError() throws TaskException {
    Tasks.trapException(() -> {});
  }
}
