package com.obsidiandynamics.warthog;

import org.junit.*;

import com.obsidiandynamics.verifier.*;

public final class WarthogContextTest {
  @Test
  public void testPojo() {
    PojoVerifier.forClass(WarthogContext.class).verify();
  }
}
