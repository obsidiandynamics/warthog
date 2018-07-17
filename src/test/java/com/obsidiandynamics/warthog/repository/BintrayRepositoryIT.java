package com.obsidiandynamics.warthog.repository;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.warthog.repository.*;
import com.obsidiandynamics.warthog.repository.BintrayRepository.*;

public final class BintrayRepositoryIT {
  @Test
  public void testExistingPackage() throws Exception {
    final var version = new BintrayRepository().resolveLatestVersion("com.obsidiandynamics.zerolog", "zerolog-core");
    assertTrue("version=" + version, version.matches("^\\d+\\.\\d+\\.\\d+$"));
  }
  
  @Test(expected=BintrayResponseException.class)
  public void testNonExistentPackage() throws Exception {
    final var version = new BintrayRepository().resolveLatestVersion("com.obsidiandynamics.zerolog", "foo");
    assertTrue("version=" + version, version.matches("^\\d+\\.\\d+\\.\\d+$"));
  }
}
