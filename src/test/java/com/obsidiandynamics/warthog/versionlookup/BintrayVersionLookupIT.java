package com.obsidiandynamics.warthog.versionlookup;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.warthog.versionlookup.BintrayVersionLookup.*;

public final class BintrayVersionLookupIT {
  @Test
  public void testExistingPackage() throws Exception {
    final var version = new BintrayVersionLookup().getLatestVersion("com.obsidiandynamics.zerolog", "zerolog-core");
    assertTrue("version=" + version, version.matches("^\\d+\\.\\d+\\.\\d+$"));
  }
  
  @Test(expected=BintrayResponseException.class)
  public void testNonExistentPackage() throws Exception {
    final var version = new BintrayVersionLookup().getLatestVersion("com.obsidiandynamics.zerolog", "foo");
    assertTrue("version=" + version, version.matches("^\\d+\\.\\d+\\.\\d+$"));
  }
}
