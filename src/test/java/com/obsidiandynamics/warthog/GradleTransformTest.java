package com.obsidiandynamics.warthog;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

public final class GradleTransformTest {
  @Test
  public void testUpdateSingleLineWithTrailing() {
    final var namesToVersions = Collections.singletonMap("fulcrum", "0.17.0");
    final var patterns = GradleTransform.buildPatterns(namesToVersions.keySet());
    final var updated = GradleTransform.updateSingleLine("    fulcrumVersion = \"0.15.1\" // fulcrum version",
                                                         namesToVersions, patterns);
    assertEquals("    fulcrumVersion = \"0.17.0\" // fulcrum version", updated.getLine());
    assertEquals("fulcrum", updated.getDependencyName());
    assertEquals("0.15.1", updated.getOldVersion());
    assertEquals("0.17.0", updated.getNewVersion());
    assertTrue(updated.isChanged());
  }
  
  @Test
  public void testUpdateSingleLineWithoutTrailing() {
    final var namesToVersions = Collections.singletonMap("fulcrum", "0.17.0");
    final var patterns = GradleTransform.buildPatterns(namesToVersions.keySet());
    final var updated = GradleTransform.updateSingleLine("    fulcrumVersion = \"0.15.1\"",
                                                         namesToVersions, patterns);
    assertEquals("    fulcrumVersion = \"0.17.0\"", updated.getLine());
    assertEquals("fulcrum", updated.getDependencyName());
    assertEquals("0.15.1", updated.getOldVersion());
    assertEquals("0.17.0", updated.getNewVersion());
    assertTrue(updated.isChanged());
  }
  
  @Test
  public void testUpdateSingleLineNoChange() {
    final var namesToVersions = Collections.singletonMap("fulcrum", "0.17.0");
    final var patterns = GradleTransform.buildPatterns(namesToVersions.keySet());
    final var updated = GradleTransform.updateSingleLine("    yconfVersion = \"0.15.1\"",
                                                         namesToVersions, patterns);
    assertEquals("    yconfVersion = \"0.15.1\"", updated.getLine());
    assertNull(updated.getDependencyName());
    assertNull(updated.getOldVersion());
    assertNull(updated.getNewVersion());
    assertFalse(updated.isChanged());
  }
  
  @Test
  public void testUpdateSingleLineSameVersion() {
    final var namesToVersions = Collections.singletonMap("fulcrum", "0.17.0");
    final var patterns = GradleTransform.buildPatterns(namesToVersions.keySet());
    final var updated = GradleTransform.updateSingleLine("    fulcrumVersion = \"0.17.0\"",
                                                         namesToVersions, patterns);
    assertEquals("    fulcrumVersion = \"0.17.0\"", updated.getLine());
    assertNull(updated.getDependencyName());
    assertNull(updated.getOldVersion());
    assertNull(updated.getNewVersion());
    assertFalse(updated.isChanged());
  }
  
  @Test
  public void testUpdateSingleLineMultipleVersions() {
    final var namesToVersions = new HashMap<String, String>();
    namesToVersions.put("fulcrum", "0.17.0");
    namesToVersions.put("yconf", "0.15.0");
    final var patterns = GradleTransform.buildPatterns(namesToVersions.keySet());
    final var updated = GradleTransform.updateSingleLine("    fulcrumVersion = \"0.15.1\"",
                                                         namesToVersions, patterns);
    assertEquals("    fulcrumVersion = \"0.17.0\"", updated.getLine());
    assertEquals("fulcrum", updated.getDependencyName());
    assertEquals("0.15.1", updated.getOldVersion());
    assertEquals("0.17.0", updated.getNewVersion());
    assertTrue(updated.isChanged());
  }
}
