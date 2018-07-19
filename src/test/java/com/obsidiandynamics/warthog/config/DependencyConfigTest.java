package com.obsidiandynamics.warthog.config;

import org.junit.*;

import com.obsidiandynamics.verifier.*;

public final class DependencyConfigTest {
  @Test
  public void testPojo() {
    PojoVerifier.forClass(DependencyConfig.class).verify();
  }
}
