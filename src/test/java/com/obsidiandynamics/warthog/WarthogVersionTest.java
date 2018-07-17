package com.obsidiandynamics.warthog;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class WarthogVersionTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(WarthogVersion.class);
  }

  @Test
  public void testGet() {
    assertNotNull(WarthogVersion.get());
  }
}
