package com.obsidiandynamics.warthog;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class VersionsTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Versions.class);
  }
  
  @Test
  public void testIsSnapshot() {
    assertTrue(Versions.isSnapshot("0.1.2-SNAPSHOT"));
    assertFalse(Versions.isSnapshot("0.1.2"));
  }
 
  @Test
  public void testToSnapshot() {
    assertEquals("0.1.2-SNAPSHOT", Versions.toSnapshot("0.1.2"));
  }
 
  @Test
  public void testToRelease() {
    assertEquals("0.1.2", Versions.toRelease("0.1.2-SNAPSHOT"));
  }
  
  @Test
  public void testParse() {
    assertEquals(asList(0, 1, 2), Versions.parse("0.1.2"));
  }
  
  @Test
  public void testFormat() {
    assertEquals("0.1.2", Versions.format(asList(0, 1, 2)));
  }
  
  @Test
  public void testRollSegment() {
    assertEquals("0.2.2", Versions.rollMinor("0.1.2"));
    assertEquals("0.2-alpha", Versions.rollMinor("0.1-alpha"));
    assertEquals("0.2.RC1", Versions.rollMinor("0.1.RC1"));
    assertEquals("0.2.2-SNAPSHOT", Versions.rollMinor("0.1.2-SNAPSHOT"));
    assertEquals("0.2.2-beta2-SNAPSHOT", Versions.rollMinor("0.1.2-beta2-SNAPSHOT"));
    assertEquals("0.2", Versions.rollMinor("0.1"));
  }
}
