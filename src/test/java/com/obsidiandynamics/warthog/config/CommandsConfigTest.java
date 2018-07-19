package com.obsidiandynamics.warthog.config;

import org.junit.*;

import com.obsidiandynamics.verifier.*;

public final class CommandsConfigTest {
  @Test
  public void testPojo() {
    PojoVerifier.forClass(CommandsConfig.class).verify();
  }
}
