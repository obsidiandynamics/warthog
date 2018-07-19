package com.obsidiandynamics.warthog.config;

import org.junit.*;

import com.obsidiandynamics.verifier.*;

public final class ModuleConfigTest {
  @Test
  public void testPojo() {
    PojoVerifier.forClass(ModuleConfig.class).excludeToStringField("dependencies").verify();
  }
}
