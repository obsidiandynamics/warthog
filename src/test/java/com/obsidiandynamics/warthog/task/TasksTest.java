package com.obsidiandynamics.warthog.task;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class TasksTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Tasks.class);
  }
  
  @Test
  public void testTrapCommandException() {
    //TODO
  }
}
